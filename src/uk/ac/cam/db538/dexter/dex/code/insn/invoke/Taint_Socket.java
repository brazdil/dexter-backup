package uk.ac.cam.db538.dexter.dex.code.insn.invoke;

import lombok.val;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.InstructionList;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Const;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_MoveResult;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_Invoke;
import uk.ac.cam.db538.dexter.dex.code.insn.macro.DexMacro_SetObjectTaint;
import uk.ac.cam.db538.dexter.merge.TaintConstants;
import uk.ac.cam.db538.dexter.utils.Pair;

public class Taint_Socket extends FallbackInstrumentor {

  @Override
  public boolean canBeApplied(DexPseudoinstruction_Invoke insn) {
    val insnInvoke = insn.getInstructionInvoke();

    if (insnInvoke.getCallType() != Opcode_Invoke.Virtual)
      return false;

    if (!insnInvoke.getClassType().getDescriptor().equals("Ljava/net/Socket;"))
      return false;

    if (!insnInvoke.getMethodName().equals("getOutputStream"))
      return false;

    return true;
  }

  @Override
  public Pair<InstructionList, InstructionList> generateInstrumentation(DexPseudoinstruction_Invoke insn, DexCode_InstrumentationState state) {
    val fallback = super.generateInstrumentation(insn, state);

    val methodCode = insn.getMethodCode();
    val postCode = new InstructionList(fallback.getValB().size() + 20);

    val regResult = ((DexInstruction_MoveResult) insn.getInstructionMoveResult()).getRegTo();
    val regResultTaint = new DexRegister();
    postCode.add(new DexInstruction_Const(methodCode, regResultTaint, TaintConstants.TAINT_SINK_SOCKET));
    postCode.add(new DexMacro_SetObjectTaint(methodCode, regResult, regResultTaint));
    postCode.addAll(fallback.getValB());

    return new Pair<InstructionList, InstructionList>(fallback.getValA(), postCode);
  }

}
