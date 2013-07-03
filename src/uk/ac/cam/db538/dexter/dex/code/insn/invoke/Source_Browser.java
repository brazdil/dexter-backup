package uk.ac.cam.db538.dexter.dex.code.insn.invoke;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import lombok.val;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_BinaryOpLiteral;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_BinaryOpLiteral;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_Invoke;
import uk.ac.cam.db538.dexter.dex.code.insn.macro.DexMacro_PrintStringConst;
import uk.ac.cam.db538.dexter.merge.TaintConstants;
import uk.ac.cam.db538.dexter.utils.Pair;

public class Source_Browser extends FallbackInstrumentor {

  /*
   *  Apply instrumentation only if the instruction calls:
   *  - Cursor android.provider.Browser.getAllBookmarks(ContentResolver cr), or
   *  - Cursor android.provider.Browser.getAllVisitedUrls(ContentResolver cr)
   *  Both of these are declared 'public static final'.
   */
  @Override
  public boolean canBeApplied(DexPseudoinstruction_Invoke insn) {
    val insnInvoke = insn.getInstructionInvoke();

    val callType = insnInvoke.getCallType();
    val className = insnInvoke.getClassType().getDescriptor();
    val methodName = insnInvoke.getMethodName();
    val methodPrototype = insnInvoke.getMethodPrototype();
    val methodReturnType = methodPrototype.getReturnType().getDescriptor();
    val methodParameters = methodPrototype.getParameterTypes();

    return insn.movesResult() && // only instrument if result is used
           callType == Opcode_Invoke.Static &&
           className.equals("Landroid/provider/Browser;") &&
           (methodName.equals("getAllBookmarks") || methodName.equals("getAllVisitedUrls")) &&
           methodReturnType.equals("Landroid/database/Cursor;") &&
           methodParameters.size() == 1 &&
           methodParameters.get(0).getDescriptor().equals("Landroid/content/ContentResolver;");
  }

  /*
   * Generate Browser source instrumentation.
   */
  @Override
  public Pair<List<DexCodeElement>, List<DexCodeElement>> generateInstrumentation(DexPseudoinstruction_Invoke insn, DexCode_InstrumentationState state) {
    val code = insn.getMethodCode();

    // generate default external call instrumentation (automatically handles exceptions)
    val defaultInstrumentation = super.generateDefaultInstrumentation(
                                   insn, state,
                                   Collections.<Integer> emptySet(),   // collect taint of the argument
                                   Arrays.asList(0),                   // do not propagate taint into the argument
                                   false);                             // assign taint to the result
    val codeBefore = defaultInstrumentation.getValA();
    val codeAfter = defaultInstrumentation.getValB();

    // set the Browser flag in the combined taint of the method call
    val instrumentation = new ArrayList<DexCodeElement>(2);
    instrumentation.add(new DexInstruction_BinaryOpLiteral(code, regCombinedTaint, regCombinedTaint, TaintConstants.TAINT_SOURCE_BROWSER, Opcode_BinaryOpLiteral.Or));
    instrumentation.add(new DexMacro_PrintStringConst(code, "browser data => " + TaintConstants.TAINT_SOURCE_BROWSER, true));

    // insert instrumentation right after the method call
    codeAfter.addAll(0, instrumentation);

    return new Pair<List<DexCodeElement>, List<DexCodeElement>>(codeBefore, codeAfter);
  }

}
