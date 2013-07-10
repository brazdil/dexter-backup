package uk.ac.cam.db538.dexter.dex.code.insn.invoke;

import lombok.val;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.elem.DexLabel;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_IfTestZero;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_IfTestZero;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_Invoke;
import uk.ac.cam.db538.dexter.dex.code.insn.macro.DexMacro_PrintInteger;
import uk.ac.cam.db538.dexter.dex.code.insn.macro.DexMacro_PrintStringConst;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexPrototype;
import uk.ac.cam.db538.dexter.utils.InstructionList;
import uk.ac.cam.db538.dexter.utils.Pair;

public class Sink_IPC extends FallbackInstrumentor {

  private boolean hasIntentParam(DexPrototype prototype) {
    for (val paramType : prototype.getParameterTypes())
      if (paramType.getDescriptor().equals("Landroid/content/Intent;") || paramType.getDescriptor().equals("[Landroid/content/Intent;"))
        return true;
    return false;
  }

  @Override
  public boolean canBeApplied(DexPseudoinstruction_Invoke insn) {
    val classHierarchy = insn.getParentFile().getHierarchy();
    val parsingCache = insn.getParentFile().getTypeCache();

    val insnInvoke = insn.getInstructionInvoke();
    val defInvokedClass = classHierarchy.getBaseClassDefinition(insnInvoke.getClassType());
    val defContext = classHierarchy.getBaseClassDefinition(DexClassType.parse("Landroid/content/Context;", parsingCache));

    return (insnInvoke.getCallType() == Opcode_Invoke.Virtual) &&
    	   defInvokedClass.isChildOf(defContext) &&
           hasIntentParam(insnInvoke.getMethodPrototype());
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
    preCode.add(new DexMacro_PrintStringConst(methodCode, "$! tainted data passed to Context." + methodName + " => T=", false));
    preCode.add(new DexMacro_PrintInteger(methodCode, regCombinedTaint, true));
    preCode.add(labelAfter);

    return new Pair<InstructionList, InstructionList>(preCode, fallback.getValB());
  }

}
