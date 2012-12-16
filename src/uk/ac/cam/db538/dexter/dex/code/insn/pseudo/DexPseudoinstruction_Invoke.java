package uk.ac.cam.db538.dexter.dex.code.insn.pseudo;

import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import lombok.val;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Invoke;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Move;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_MoveResult;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_MoveResultWide;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_Invoke;
import uk.ac.cam.db538.dexter.dex.method.DexPrototype;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexPrimitiveType;
import uk.ac.cam.db538.dexter.dex.type.DexPrimitiveType.DexLong;

public class DexPseudoinstruction_Invoke extends DexPseudoinstruction {

  @Getter private final DexInstruction_Invoke instructionInvoke;
  @Getter private final DexInstruction instructionMoveResult;

  public DexPseudoinstruction_Invoke(DexInstruction_Invoke insnInvoke, DexInstruction insnMoveResult) {
    super(insnInvoke.getMethodCode());

    this.instructionInvoke = insnInvoke;
    this.instructionMoveResult = insnMoveResult;

    if (instructionMoveResult != null &&
        (! (instructionMoveResult instanceof DexInstruction_MoveResult)) &&
        (! (instructionMoveResult instanceof DexInstruction_MoveResultWide)))
      throw new RuntimeException("DexPseudoinstruction_Invoke only accepts MoveResult* instructions");
  }

  public DexPseudoinstruction_Invoke(DexInstruction_Invoke insnInvoke) {
    this(insnInvoke, null);
  }

  private boolean movesResult() {
    return instructionMoveResult != null;
  }

  @Override
  public List<DexCodeElement> unwrap() {
    if (movesResult())
      return createList(
               (DexCodeElement) instructionInvoke,
               (DexCodeElement) instructionMoveResult);
    else
      return createList((DexCodeElement) instructionInvoke);
  }

  private DexCodeElement[] instrumentDirectExternal(DexCode_InstrumentationState state) {
    return new DexCodeElement[] { this };
  }

  private DexCodeElement[] instrumentDirectInternal(DexCode_InstrumentationState state) {
    val code = instructionInvoke.getMethodCode();

    val originalPrototype = instructionInvoke.getMethodPrototype();
    val newPrototype = originalPrototype.getInstrumentedPrototype(state.getCache());

    val originalReturnType = originalPrototype.getReturnType();
    val newReturnType = newPrototype.getReturnType();

    val newArgumentRegisters = originalPrototype.instrumentInvokeCallArgumentRegisters(instructionInvoke.getArgumentRegisters(), instructionInvoke.isStaticCall(), state);

    val newInstructionInvoke = new DexInstruction_Invoke(
      instructionInvoke.getMethodCode(),
      instructionInvoke.getClassType(),
      instructionInvoke.getMethodName(),
      newPrototype,
      newArgumentRegisters,
      instructionInvoke.getCallType());

    if (originalReturnType instanceof DexPrimitiveType && movesResult()) {
      if (newReturnType instanceof DexLong) {
        val originalMoveResult = (DexInstruction_MoveResult) instructionMoveResult;

        val regPrimitive = originalMoveResult.getRegTo();
        val regTaint = state.getTaintRegister(regPrimitive);

        val regTempResult1 = new DexRegister();
        val regTempResult2 = new DexRegister();

        // result of the method is a combination of the original value and its taint tag
        return new DexCodeElement[] {
                 newInstructionInvoke,
                 new DexInstruction_MoveResultWide(code, regTempResult1, regTempResult2),
                 new DexInstruction_Move(code, regPrimitive, regTempResult1, false),
                 new DexInstruction_Move(code, regTaint, regTempResult2, false)
               };
      } else {
        val regObj = new DexRegister();
        val regTo1 = ((DexInstruction_MoveResultWide) instructionMoveResult).getRegTo1();
        val regTo2 = ((DexInstruction_MoveResultWide) instructionMoveResult).getRegTo2();
        val regTaint1 = state.getTaintRegister(regTo1);
        val regTaint2 = state.getTaintRegister(regTo2);

        String convertFnName = null;
        String convertFnReturnType = null;
        if (newReturnType.getDescriptor().equals("Ljava/lang/Long;")) {
          convertFnName = "longValue";
          convertFnReturnType = "J";
        } else if (newReturnType.getDescriptor().equals("Ljava/lang/Double;")) {
          convertFnName = "doubleValue";
          convertFnReturnType = "D";
        }

        return new DexCodeElement[] {
                 newInstructionInvoke,
                 new DexInstruction_MoveResult(code, regObj, true),
                 new DexInstruction_Invoke(
                   code,
                   (DexClassType) newReturnType,
                   convertFnName,
                   new DexPrototype(DexPrimitiveType.parse(convertFnReturnType), null),
                   Arrays.asList(new DexRegister[] { regObj }),
                   Opcode_Invoke.Virtual),
                 new DexInstruction_MoveResultWide(
                   code,
                   regTo1,
                   regTo2),
                 new DexPseudoinstruction_GetObjectTaint(
                   code,
                   regTaint1,
                   regObj),
                 new DexInstruction_Move(code, regTaint2, regTaint1, false)
               };
      }
    } else
      // handles cases:
      //  - object result, with moving
      //  - object result, without moving
      //  - primitive result, without moving
      return new DexCodeElement[] { new DexPseudoinstruction_Invoke(newInstructionInvoke, instructionMoveResult) };
  }

  private DexCodeElement[] instrumentVirtual(DexCode_InstrumentationState state) {
    return new DexCodeElement[] { this };
  }

  @Override
  public DexCodeElement[] instrument(DexCode_InstrumentationState state) {
    switch (instructionInvoke.getCallType()) {
    case Direct:
    case Static:
      if (instructionInvoke.getClassType().isDefinedInternally())
        return instrumentDirectInternal(state);
      else
        return instrumentDirectExternal(state);
    case Interface:
    case Super:
    case Virtual:
    default:
      return instrumentVirtual(state);
    }
  }
}
