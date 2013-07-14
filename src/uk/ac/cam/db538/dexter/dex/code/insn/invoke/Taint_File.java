package uk.ac.cam.db538.dexter.dex.code.insn.invoke;

import lombok.val;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.InstructionList;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Const;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_Invoke;
import uk.ac.cam.db538.dexter.dex.code.insn.macro.DexMacro_SetObjectTaint;
import uk.ac.cam.db538.dexter.merge.TaintConstants;
import uk.ac.cam.db538.dexter.utils.Pair;

public class Taint_File extends FallbackInstrumentor {

  @Override
  public boolean canBeApplied(DexPseudoinstruction_Invoke insn) {
    val insnInvoke = insn.getInstructionInvoke();

    if (insnInvoke.getCallType() != Opcode_Invoke.Direct)
      return false;

    if (!insnInvoke.getClassType().getDescriptor().equals("Ljava/io/FileOutputStream;"))
      return false;

    if (!insnInvoke.getMethodName().equals("<init>"))
      return false;

    return true;
  }

  @Override
  public Pair<InstructionList, InstructionList> generateInstrumentation(DexPseudoinstruction_Invoke insn, DexCode_InstrumentationState state) {
    val fallback = super.generateInstrumentation(insn, state);

    val methodCode = insn.getMethodCode();
    val postCode = new InstructionList(fallback.getValB().size() + 20);

    val regFile = insn.getInstructionInvoke().getArgumentRegisters().get(0);
    val regFileTaint = new DexRegister();
    postCode.add(new DexInstruction_Const(methodCode, regFileTaint, TaintConstants.TAINT_SINK_FILE));
    postCode.add(new DexMacro_SetObjectTaint(methodCode, regFile, regFileTaint));
    postCode.addAll(fallback.getValB());

    return new Pair<InstructionList, InstructionList>(fallback.getValA(), postCode);
  }

}
