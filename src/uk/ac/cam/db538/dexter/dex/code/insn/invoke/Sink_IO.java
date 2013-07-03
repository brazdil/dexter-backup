package uk.ac.cam.db538.dexter.dex.code.insn.invoke;

import java.util.Arrays;
import java.util.List;

import lombok.val;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.elem.DexLabel;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_IfTestZero;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Invoke;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_MoveResult;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_IfTestZero;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_Invoke;
import uk.ac.cam.db538.dexter.dex.code.insn.macro.DexMacro_PrintStringConst;
import uk.ac.cam.db538.dexter.dex.code.insn.macro.DexMacro_PrintInteger;
import uk.ac.cam.db538.dexter.dex.type.DexType_Class;
import uk.ac.cam.db538.dexter.utils.NoDuplicatesList;
import uk.ac.cam.db538.dexter.utils.Pair;

public class Sink_IO extends FallbackInstrumentor {

  @Override
  public boolean canBeApplied(DexPseudoinstruction_Invoke insn) {
    val classHierarchy = insn.getParentFile().getClassHierarchy();
    val parsingCache = insn.getParentFile().getParsingCache();

    val insnInvoke = insn.getInstructionInvoke();
    val invokedClass = insnInvoke.getClassType();

    if ((insnInvoke.getCallType() != Opcode_Invoke.Virtual) && (insnInvoke.getCallType() != Opcode_Invoke.Interface))
      return false;

    if (!classHierarchy.isAncestor(invokedClass, DexType_Class.parse("Ljava/io/Writer;", parsingCache)) &&
        !classHierarchy.isAncestor(invokedClass, DexType_Class.parse("Ljava/io/OutputStream;", parsingCache)))
      return false;

    if (insnInvoke.getMethodPrototype().getParameterCount(false) < 2)
      return false;

    return true;
  }

  @Override
  public Pair<List<DexCodeElement>, List<DexCodeElement>> generateInstrumentation(DexPseudoinstruction_Invoke insn, DexCode_InstrumentationState state) {
    val fallback = super.generateInstrumentation(insn, state);
    val regCombinedTaint = this.regCombinedTaint;

    val dex = insn.getParentFile();
    val methodCode = insn.getMethodCode();
    val preCode = new NoDuplicatesList<DexCodeElement>(fallback.getValA().size() + 20);

    // check the combined taint
    val labelAfter = new DexLabel(methodCode);
    val regTaintAnalysis = new DexRegister();
    preCode.addAll(fallback.getValA());
    preCode.add(new DexInstruction_Invoke(methodCode, dex.getTaintConstants_HasSourceAndSinkTaint(), Arrays.asList(regCombinedTaint)));
    preCode.add(new DexInstruction_MoveResult(methodCode, regTaintAnalysis, false));
    preCode.add(new DexInstruction_IfTestZero(methodCode, regTaintAnalysis, labelAfter, Opcode_IfTestZero.eqz));
    preCode.add(new DexMacro_PrintStringConst(methodCode, "$! tainted I/O request executed => T=", false));
    preCode.add(new DexMacro_PrintInteger(methodCode, regCombinedTaint, true));
    preCode.add(labelAfter);

    return new Pair<List<DexCodeElement>, List<DexCodeElement>>(preCode, fallback.getValB());
  }

}
