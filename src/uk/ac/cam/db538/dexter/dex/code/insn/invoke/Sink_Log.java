package uk.ac.cam.db538.dexter.dex.code.insn.invoke;

import lombok.val;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.InstructionList;
import uk.ac.cam.db538.dexter.dex.code.elem.DexLabel;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_IfTestZero;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_IfTestZero;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_Invoke;
import uk.ac.cam.db538.dexter.dex.code.insn.macro.DexMacro_PrintInteger;
import uk.ac.cam.db538.dexter.dex.code.insn.macro.DexMacro_PrintStringConst;
import uk.ac.cam.db538.dexter.utils.Pair;

public class Sink_Log extends FallbackInstrumentor {

  @Override
  public boolean canBeApplied(DexPseudoinstruction_Invoke insn) {
    val insnInvoke = insn.getInstructionInvoke();

    if (insnInvoke.getCallType() != Opcode_Invoke.Static)
      return false;

    val methodName = insnInvoke.getMethodName();
    if (!methodName.equals("d") &&
        !methodName.equals("e") &&
        !methodName.equals("i") &&
        !methodName.equals("v") &&
        !methodName.equals("w") &&
        !methodName.equals("wtf") &&
        !methodName.equals("println"))
      return false;

    if (!insnInvoke.getClassType().getDescriptor().equals("Landroid/util/Log;"))
      return false;

    return true;
  }

  @Override
  public Pair<InstructionList, InstructionList> generateInstrumentation(DexPseudoinstruction_Invoke insn, DexCode_InstrumentationState state) {
    val fallback = super.generateInstrumentation(insn, state);
    val regCombinedTaint = this.regCombinedTaint;

    val methodCode = insn.getMethodCode();
    val preCode = new InstructionList(fallback.getValA().size() + 20);

    val methodName = insn.getInstructionInvoke().getMethodName();

    // check the combined taint
    val labelAfter = new DexLabel(methodCode);
    preCode.addAll(fallback.getValA());
    preCode.add(new DexInstruction_IfTestZero(methodCode, regCombinedTaint, labelAfter, Opcode_IfTestZero.eqz));
    preCode.add(new DexMacro_PrintStringConst(methodCode, "$! tainted data passed to Log." + methodName + " => T=", false));
    preCode.add(new DexMacro_PrintInteger(methodCode, regCombinedTaint, true));
    preCode.add(labelAfter);

    return new Pair<InstructionList, InstructionList>(preCode, fallback.getValB());
  }

}
