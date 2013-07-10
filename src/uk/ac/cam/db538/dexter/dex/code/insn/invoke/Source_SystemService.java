package uk.ac.cam.db538.dexter.dex.code.insn.invoke;

import lombok.val;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_BinaryOp;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_MoveResult;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_MoveResultWide;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_BinaryOp;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_Invoke;
import uk.ac.cam.db538.dexter.dex.code.insn.macro.DexMacro_GetServiceTaint;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.utils.InstructionList;
import uk.ac.cam.db538.dexter.utils.Pair;

public class Source_SystemService extends FallbackInstrumentor {

  @Override
  public boolean canBeApplied(DexPseudoinstruction_Invoke insn) {
    val classHierarchy = insn.getParentFile().getHierarchy();
    val parsingCache = insn.getParentFile().getTypeCache();

    val insnInvoke = insn.getInstructionInvoke();
    val defInvokedClass = classHierarchy.getBaseClassDefinition(insnInvoke.getClassType());
    val defContext = classHierarchy.getBaseClassDefinition(DexClassType.parse("Landroid/content/Context;", parsingCache));

    if (insnInvoke.getCallType() != Opcode_Invoke.Virtual)
      return false;

    if (!insnInvoke.getMethodName().equals("getSystemService"))
      return false;

    if (!insn.movesResult()) // only care about assigning taint to the result
      return false;

    if (!defInvokedClass.isChildOf(defContext))
      return false;

    val methodParamTypes = insnInvoke.getMethodPrototype().getParameterTypes();
    if (methodParamTypes.size() != 1 ||
        !methodParamTypes.get(0).getDescriptor().equals("Ljava/lang/String;"))
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
    preCode.add(new DexMacro_GetServiceTaint(methodCode, regResultTaint, insnInvoke.getArgumentRegisters().get(1)));

    postCode.add(new DexInstruction_BinaryOp(methodCode, regCombinedTaint, regCombinedTaint, regResultTaint, Opcode_BinaryOp.OrInt));
    postCode.addAll(fallback.getValB());

    return new Pair<InstructionList, InstructionList>(preCode, postCode);
  }

}
