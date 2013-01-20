package uk.ac.cam.db538.dexter.dex.code.insn.pseudo.invoke;

import java.util.List;

import lombok.val;

import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_BinaryOp;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Const;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Move;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_MoveResult;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_MoveResultWide;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_BinaryOp;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_Invoke;
import uk.ac.cam.db538.dexter.dex.code.insn.pseudo.DexPseudoinstruction_GetObjectTaint;
import uk.ac.cam.db538.dexter.dex.code.insn.pseudo.DexPseudoinstruction_PrintInteger;
import uk.ac.cam.db538.dexter.dex.code.insn.pseudo.DexPseudoinstruction_PrintStringConst;
import uk.ac.cam.db538.dexter.dex.code.insn.pseudo.DexPseudoinstruction_SetObjectTaint;
import uk.ac.cam.db538.dexter.dex.type.DexPrimitiveType;
import uk.ac.cam.db538.dexter.dex.type.DexReferenceType;
import uk.ac.cam.db538.dexter.utils.NoDuplicatesList;
import uk.ac.cam.db538.dexter.utils.Pair;

public class FallbackInstrumentor implements ExternalCallInstrumentor {

  @Override
  public boolean canBeApplied(DexPseudoinstruction_Invoke insn) {
    return true;
  }

  private List<DexCodeElement> generatePreExternalCallCode(DexPseudoinstruction_Invoke insn, DexRegister regCombinedTaint, DexCode_InstrumentationState state) {
    val codePreExternalCall = new NoDuplicatesList<DexCodeElement>();
    val methodCode = insn.getMethodCode();
    val instructionInvoke = insn.getInstructionInvoke();
    val isStaticCall = (instructionInvoke.getCallType() == Opcode_Invoke.Static);
    val isConstructorCall = instructionInvoke.getMethodName().equals("<init>");

    val methodPrototype = instructionInvoke.getMethodPrototype();
    val methodParameterRegs = instructionInvoke.getArgumentRegisters();

    codePreExternalCall.add(new DexPseudoinstruction_PrintStringConst(
                              methodCode,
                              "$$$ EXTERNAL CALL: " + instructionInvoke.getClassType().getPrettyName() + "..." + instructionInvoke.getMethodName(),
                              true));

    // if there are any parameters or result is moved
    if (!methodPrototype.getParameterTypes().isEmpty() || insn.movesResult()) {
      // combine the taint of the object (if not static call) and all the parameters

      val regObjectArgTaint = new DexRegister();
      if (isStaticCall || isConstructorCall)
        codePreExternalCall.add(new DexInstruction_Const(methodCode, regCombinedTaint, 0));
      else
        codePreExternalCall.add(new DexPseudoinstruction_GetObjectTaint(methodCode, regCombinedTaint, methodParameterRegs.get(0)));

      int paramIndex = isStaticCall ? 0 : 1;
      for (val paramType : methodPrototype.getParameterTypes()) {
        DexRegister regArgTaint;
        if (paramType instanceof DexPrimitiveType)
          regArgTaint = state.getTaintRegister(methodParameterRegs.get(paramIndex));
        else {
          codePreExternalCall.add(new DexPseudoinstruction_GetObjectTaint(methodCode, regObjectArgTaint, methodParameterRegs.get(paramIndex)));
          regArgTaint = regObjectArgTaint;
        }
        codePreExternalCall.add(new DexInstruction_BinaryOp(methodCode, regCombinedTaint, regCombinedTaint, regArgTaint, Opcode_BinaryOp.OrInt));
        paramIndex += paramType.getRegisters();
      }

      codePreExternalCall.add(new DexPseudoinstruction_PrintStringConst(methodCode, "$$$  TAINT = ", false));
      codePreExternalCall.add(new DexPseudoinstruction_PrintInteger(methodCode, regCombinedTaint, true));

      // assign the combined taint to the object and all its non-primitive arguments

      if (!isStaticCall && !isConstructorCall)
        codePreExternalCall.add(new DexPseudoinstruction_SetObjectTaint(methodCode, methodParameterRegs.get(0), regCombinedTaint));

      paramIndex = isStaticCall ? 0 : 1;
      for (val paramType : methodPrototype.getParameterTypes()) {
        if (paramType instanceof DexReferenceType)
          codePreExternalCall.add(new DexPseudoinstruction_SetObjectTaint(methodCode, methodParameterRegs.get(paramIndex), regCombinedTaint));
        paramIndex += paramType.getRegisters();
      }
    }

    return codePreExternalCall;
  }

  private List<DexCodeElement> generatePostExternalCallCode(DexPseudoinstruction_Invoke insn, DexRegister regCombinedTaint, DexCode_InstrumentationState state) {
    val codePostExternalCall = new NoDuplicatesList<DexCodeElement>();
    val methodCode = insn.getMethodCode();
    val instructionInvoke = insn.getInstructionInvoke();
    val instructionMoveResult = insn.getInstructionMoveResult();
    val methodPrototype = instructionInvoke.getMethodPrototype();

    if (insn.movesResult()) {
      if (methodPrototype.getReturnType() instanceof DexPrimitiveType) {
        DexRegister regResult;
        if (instructionMoveResult instanceof DexInstruction_MoveResult)
          regResult = ((DexInstruction_MoveResult) instructionMoveResult).getRegTo();
        else
          regResult = ((DexInstruction_MoveResultWide) instructionMoveResult).getRegTo1();
        codePostExternalCall.add(
          new DexInstruction_Move(methodCode, state.getTaintRegister(regResult), regCombinedTaint, false));

      } else
        codePostExternalCall.add(
          new DexPseudoinstruction_SetObjectTaint(methodCode, ((DexInstruction_MoveResult) instructionMoveResult).getRegTo(), regCombinedTaint));
    }

    return codePostExternalCall;
  }

  @Override
  public Pair<List<DexCodeElement>, List<DexCodeElement>> generateInstrumentation(DexPseudoinstruction_Invoke insn, DexCode_InstrumentationState state) {
    val regCombinedTaint = new DexRegister();
    return new Pair<List<DexCodeElement>, List<DexCodeElement>>(
             generatePreExternalCallCode(insn, regCombinedTaint, state),
             generatePostExternalCallCode(insn, regCombinedTaint, state));
  }

}
