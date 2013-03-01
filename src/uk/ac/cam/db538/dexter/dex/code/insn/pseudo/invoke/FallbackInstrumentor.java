package uk.ac.cam.db538.dexter.dex.code.insn.pseudo.invoke;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import lombok.val;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCatchAll;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.elem.DexLabel;
import uk.ac.cam.db538.dexter.dex.code.elem.DexTryBlockEnd;
import uk.ac.cam.db538.dexter.dex.code.elem.DexTryBlockStart;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_BinaryOp;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Const;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Goto;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Move;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_MoveException;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_MoveResult;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_MoveResultWide;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Throw;
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

public class FallbackInstrumentor extends ExternalCallInstrumentor {

  protected DexRegister regCombinedTaint;

  @Override
  public boolean canBeApplied(DexPseudoinstruction_Invoke insn) {
    return true;
  }

  private List<DexCodeElement> generatePreExternalCallCode(DexPseudoinstruction_Invoke insn, DexRegister regCombinedTaint, DexCode_InstrumentationState state,
      Collection<Integer> excludeFromTaintAcquirement, Collection<Integer> excludeFromTaintAssignment) {
    val printDebug = state.getCache().isInsertDebugLogging();

    val codePreExternalCall = new NoDuplicatesList<DexCodeElement>();
    val methodCode = insn.getMethodCode();
    val instructionInvoke = insn.getInstructionInvoke();
    val isStaticCall = (instructionInvoke.getCallType() == Opcode_Invoke.Static);
    val isConstructorCall = instructionInvoke.getMethodName().equals("<init>");

    val methodPrototype = instructionInvoke.getMethodPrototype();
    val methodParameterRegs = instructionInvoke.getArgumentRegisters();

    if (isStaticCall || isConstructorCall || excludeFromTaintAcquirement.contains(0))
      codePreExternalCall.add(new DexInstruction_Const(methodCode, regCombinedTaint, 0));
    else
      codePreExternalCall.add(new DexPseudoinstruction_GetObjectTaint(methodCode, regCombinedTaint, methodParameterRegs.get(0)));

    // if there are any parameters or result is moved
    if (!methodPrototype.getParameterTypes().isEmpty() || insn.movesResult() || isConstructorCall) {
      // combine the taint of the object (if not static call) and all the parameters

      val regObjectArgTaint = new DexRegister();
      int paramRegIndex = isStaticCall ? 0 : 1;
      int paramIndex = paramRegIndex;
      for (val paramType : methodPrototype.getParameterTypes()) {
        if (!excludeFromTaintAcquirement.contains(paramIndex)) {
          DexRegister regArgTaint;
          if (paramType instanceof DexPrimitiveType)
            regArgTaint = state.getTaintRegister(methodParameterRegs.get(paramRegIndex));
          else {
            codePreExternalCall.add(new DexPseudoinstruction_GetObjectTaint(methodCode, regObjectArgTaint, methodParameterRegs.get(paramRegIndex)));
            regArgTaint = regObjectArgTaint;
          }
          codePreExternalCall.add(new DexInstruction_BinaryOp(methodCode, regCombinedTaint, regCombinedTaint, regArgTaint, Opcode_BinaryOp.OrInt));
        }

        paramRegIndex += paramType.getRegisters();
        paramIndex++;
      }

      // assign the combined taint to the object and all its non-primitive arguments

      if (!isStaticCall && !isConstructorCall && !excludeFromTaintAssignment.contains(0))
        codePreExternalCall.add(new DexPseudoinstruction_SetObjectTaint(methodCode, methodParameterRegs.get(0), regCombinedTaint));

      paramRegIndex = isStaticCall ? 0 : 1;
      paramIndex = paramRegIndex;
      for (val paramType : methodPrototype.getParameterTypes()) {
        if (!excludeFromTaintAssignment.contains(paramIndex))
          if (paramType instanceof DexReferenceType && !((DexReferenceType) paramType).isImmutable())
            codePreExternalCall.add(new DexPseudoinstruction_SetObjectTaint(methodCode, methodParameterRegs.get(paramRegIndex), regCombinedTaint));

        paramRegIndex += paramType.getRegisters();
        paramIndex++;
      }
    }

    if (printDebug) {
      codePreExternalCall.add(new DexPseudoinstruction_PrintStringConst(
                                methodCode,
                                "$ " + methodCode.getParentClass().getType().getShortName() + "->" + methodCode.getParentMethod().getName() + ": " +
                                "external call to " + instructionInvoke.getClassType().getPrettyName() + "->" + instructionInvoke.getMethodName() +
                                " => T=",
                                false));
      codePreExternalCall.add(new DexPseudoinstruction_PrintInteger(methodCode, regCombinedTaint, true));
    }

    codePreExternalCall.add(new DexTryBlockStart(methodCode));

    return codePreExternalCall;
  }

  private List<DexCodeElement> generatePostExternalCallCode(DexPseudoinstruction_Invoke insn, DexRegister regCombinedTaint, DexCode_InstrumentationState state,
      Collection<Integer> excludeFromTaintAssignment, boolean excludeResultFromTaintAssignment, DexTryBlockStart tryBlockStart) {
    val codePostExternalCall = new NoDuplicatesList<DexCodeElement>();
    val methodCode = insn.getMethodCode();
    val instructionInvoke = insn.getInstructionInvoke();
    val instructionMoveResult = insn.getInstructionMoveResult();
    val methodPrototype = instructionInvoke.getMethodPrototype();
    val isConstructorCall = instructionInvoke.getMethodName().equals("<init>");

    val labelEnd = new DexLabel(methodCode);
    val catchAll = new DexCatchAll(methodCode);

    tryBlockStart.setCatchAllHandler(catchAll);
    codePostExternalCall.add(new DexTryBlockEnd(methodCode, tryBlockStart));

    if (insn.movesResult()) {
      if (methodPrototype.getReturnType() instanceof DexPrimitiveType) {
        DexRegister regResult;
        if (instructionMoveResult instanceof DexInstruction_MoveResult)
          regResult = ((DexInstruction_MoveResult) instructionMoveResult).getRegTo();
        else
          regResult = ((DexInstruction_MoveResultWide) instructionMoveResult).getRegTo1();
        codePostExternalCall.add(new DexInstruction_Move(methodCode, state.getTaintRegister(regResult), regCombinedTaint, false));

      } else {
        val regResult = ((DexInstruction_MoveResult) instructionMoveResult).getRegTo();
        codePostExternalCall.add(new DexPseudoinstruction_SetObjectTaint(methodCode, regResult, regCombinedTaint));
      }
    }
    else if (isConstructorCall) {
      val regInitializedObject = instructionInvoke.getArgumentRegisters().get(0);
      codePostExternalCall.add(new DexPseudoinstruction_SetObjectTaint(methodCode, regInitializedObject, regCombinedTaint));
    }

    codePostExternalCall.add(new DexInstruction_Goto(methodCode, labelEnd));

    val regException = new DexRegister();
    codePostExternalCall.add(catchAll);
    codePostExternalCall.add(new DexInstruction_MoveException(methodCode, regException));
    codePostExternalCall.add(new DexPseudoinstruction_SetObjectTaint(methodCode, regException, regCombinedTaint));
    codePostExternalCall.add(new DexInstruction_Throw(methodCode, regException));

    codePostExternalCall.add(labelEnd);

    return codePostExternalCall;
  }

  protected Pair<List<DexCodeElement>, List<DexCodeElement>> generateExternalCallCode(DexPseudoinstruction_Invoke insn,
      DexCode_InstrumentationState state,
      Collection<Integer> excludeFromTaintAcquirement,
      Collection<Integer> excludeFromTaintAssignment,
      boolean excludeResultFromTaintAssignment) {
    regCombinedTaint = new DexRegister();
    val preCode = generatePreExternalCallCode(insn, regCombinedTaint, state, excludeFromTaintAcquirement, excludeFromTaintAssignment);
    val postCode = generatePostExternalCallCode(insn, regCombinedTaint, state, excludeFromTaintAssignment, excludeResultFromTaintAssignment, (DexTryBlockStart) preCode.get(preCode.size() - 1));
    return new Pair<List<DexCodeElement>, List<DexCodeElement>>(preCode, postCode);
  }

  @Override
  public Pair<List<DexCodeElement>, List<DexCodeElement>> generateInstrumentation(DexPseudoinstruction_Invoke insn, DexCode_InstrumentationState state) {
    return generateExternalCallCode(insn, state, Collections.<Integer> emptySet(), Collections.<Integer> emptySet(), false);
  }

}
