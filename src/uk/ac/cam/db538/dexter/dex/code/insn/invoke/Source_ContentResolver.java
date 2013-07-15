package uk.ac.cam.db538.dexter.dex.code.insn.invoke;

import lombok.val;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.InstructionList;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_MoveResult;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_MoveResultWide;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_Invoke;
import uk.ac.cam.db538.dexter.dex.code.insn.macro.DexMacro_GetQueryTaint;
import uk.ac.cam.db538.dexter.dex.code.insn.macro.DexMacro_SetObjectTaint;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexPrototype;
import uk.ac.cam.db538.dexter.utils.Pair;

public class Source_ContentResolver extends FallbackInstrumentor {

  private boolean fitsAPI1(DexPrototype methodPrototype) {
    val methodParamTypes = methodPrototype.getParameterTypes();
    return methodParamTypes.size() == 5 &&
           methodParamTypes.get(0).getDescriptor().equals("Landroid/net/Uri;") && // uri
           methodParamTypes.get(1).getDescriptor().equals("[Ljava/lang/String;") && // projection
           methodParamTypes.get(2).getDescriptor().equals("Ljava/lang/String;") && // selection
           methodParamTypes.get(3).getDescriptor().equals("[Ljava/lang/String;") && // selectionArgs
           methodParamTypes.get(4).getDescriptor().equals("Ljava/lang/String;"); // sortOrder
  }

  private boolean fitsAPI16(DexPrototype methodPrototype) {
    val methodParamTypes = methodPrototype.getParameterTypes();
    return methodParamTypes.size() == 6 &&
           methodParamTypes.get(0).getDescriptor().equals("Landroid/net/Uri;") && // uri
           methodParamTypes.get(1).getDescriptor().equals("[Ljava/lang/String;") && // projection
           methodParamTypes.get(2).getDescriptor().equals("Ljava/lang/String;") && // selection
           methodParamTypes.get(3).getDescriptor().equals("[Ljava/lang/String;") && // selectionArgs
           methodParamTypes.get(4).getDescriptor().equals("Ljava/lang/String;") && // sortOrder
           methodParamTypes.get(5).getDescriptor().equals("Landroid/os/CancellationSignal;"); // cancellationSignal
  }

  @Override
  public boolean canBeApplied(DexPseudoinstruction_Invoke insn) {
    val classHierarchy = insn.getParentFile().getHierarchy();
    val parsingCache = insn.getParentFile().getTypeCache();

    val insnInvoke = insn.getInstructionInvoke();
    val defInvokedClass = classHierarchy.getBaseClassDefinition(insnInvoke.getClassType());
    val defContentResolver = classHierarchy.getBaseClassDefinition(DexClassType.parse("Landroid/content/ContentResolver;", parsingCache));

    if (insnInvoke.getCallType() != Opcode_Invoke.Virtual)
      return false;

    if (!insnInvoke.getMethodName().equals("query"))
      return false;

    if (!insn.movesResult()) // only care about assigning taint to the result
      return false;

    if (!defInvokedClass.isChildOf(defContentResolver))
      return false;

    if (!fitsAPI1(insnInvoke.getMethodPrototype()) && !fitsAPI16(insnInvoke.getMethodPrototype()))
      return false;

    return true;
  }

  @Override
  public Pair<InstructionList, InstructionList> generateInstrumentation(DexPseudoinstruction_Invoke insn, DexCode_InstrumentationState state) {
    val fallback = super.generateInstrumentation(insn, state);

    val insnInvoke = insn.getInstructionInvoke();
    val insnMoveResult = insn.getInstructionMoveResult();

    val methodCode = insn.getMethodCode();
    val preCode = new InstructionList(fallback.getValA().size() + 20);
    val postCode = new InstructionList(fallback.getValB().size() + 20);

    DexRegister regResult = null;
    if (insnMoveResult instanceof DexInstruction_MoveResult)
      regResult = ((DexInstruction_MoveResult) insnMoveResult).getRegTo();
    else if (insnMoveResult instanceof DexInstruction_MoveResultWide)
      regResult = ((DexInstruction_MoveResultWide) insnMoveResult).getRegTo1();

    val regResultTaint = state.getTaintRegister(regResult);

    // add taint to the result
    preCode.addAll(fallback.getValA());
    preCode.add(new DexMacro_GetQueryTaint(methodCode, regResultTaint, insnInvoke.getArgumentRegisters().get(1)));

    postCode.add(new DexMacro_SetObjectTaint(methodCode, regResult, regResultTaint));
    postCode.addAll(fallback.getValB());

    return new Pair<InstructionList, InstructionList>(preCode, postCode);
  }

}
