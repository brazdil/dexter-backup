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
import uk.ac.cam.db538.dexter.hierarchy.ClassDefinition;
import uk.ac.cam.db538.dexter.utils.InstructionList;
import uk.ac.cam.db538.dexter.utils.Pair;

public class Sink_HttpClient extends FallbackInstrumentor {

  @Override
  public boolean canBeApplied(DexPseudoinstruction_Invoke insn) {
    val classHierarchy = insn.getParentFile().getHierarchy();
    val parsingCache = insn.getParentFile().getTypeCache();

    val insnInvoke = insn.getInstructionInvoke();
    val callType = insnInvoke.getCallType();
    val invokedClass = insnInvoke.getClassType();
    val typeHttpClient = DexClassType.parse("Lorg/apache/http/client/HttpClient;", parsingCache);
    
    val defInvokedClass = classHierarchy.getBaseClassDefinition(invokedClass);
    val defHttpClient = classHierarchy.getInterfaceDefinition(typeHttpClient); 

    return insnInvoke.getMethodName().equals("execute") &&
           (
             (
               callType == Opcode_Invoke.Virtual &&
               defInvokedClass instanceof ClassDefinition &&
               ((ClassDefinition) defInvokedClass).implementsInterface(defHttpClient)
             ) || (
               (callType == Opcode_Invoke.Virtual || callType == Opcode_Invoke.Super) &&
               defInvokedClass.equals(defHttpClient)
             )
           );
  }

  @Override
  public Pair<InstructionList, InstructionList> generateInstrumentation(DexPseudoinstruction_Invoke insn, DexCode_InstrumentationState state) {
    val fallback = super.generateInstrumentation(insn, state);
    val regCombinedTaint = this.regCombinedTaint;

    val methodCode = insn.getMethodCode();
    val preCode = new InstructionList(fallback.getValA().size() + 20);

    // check the combined taint
    val labelAfter = new DexLabel(methodCode);
    preCode.addAll(fallback.getValA());
    preCode.add(new DexInstruction_IfTestZero(methodCode, regCombinedTaint, labelAfter, Opcode_IfTestZero.eqz));
    preCode.add(new DexMacro_PrintStringConst(methodCode, "$! tainted Apache HTTPClient request executed => T=", false));
    preCode.add(new DexMacro_PrintInteger(methodCode, regCombinedTaint, true));
    preCode.add(labelAfter);

    return new Pair<InstructionList, InstructionList>(preCode, fallback.getValB());
  }

}
