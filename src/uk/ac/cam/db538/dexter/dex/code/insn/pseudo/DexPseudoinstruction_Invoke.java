package uk.ac.cam.db538.dexter.dex.code.insn.pseudo;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import lombok.Getter;
import lombok.val;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.elem.DexLabel;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ArrayPut;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Const;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Goto;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_IfTestZero;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_InstanceOf;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Invoke;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Move;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_MoveResult;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_MoveResultWide;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_StaticGet;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_GetPut;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_IfTestZero;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_Invoke;
import uk.ac.cam.db538.dexter.dex.method.DexPrototype;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexPrimitiveType;
import uk.ac.cam.db538.dexter.dex.type.DexVoid;

public class DexPseudoinstruction_Invoke extends DexPseudoinstruction {

  @Getter private final DexInstruction_Invoke instructionInvoke;
  @Getter private final DexInstruction instructionMoveResult;

  public DexPseudoinstruction_Invoke(DexCode methodCode, DexInstruction_Invoke insnInvoke, DexInstruction insnMoveResult) {
    super(methodCode);

    this.instructionInvoke = insnInvoke;
    this.instructionMoveResult = insnMoveResult;

    if (instructionMoveResult != null &&
        (! (instructionMoveResult instanceof DexInstruction_MoveResult)) &&
        (! (instructionMoveResult instanceof DexInstruction_MoveResultWide)))
      throw new RuntimeException("DexPseudoinstruction_Invoke only accepts MoveResult* instructions");
  }

  public DexPseudoinstruction_Invoke(DexCode methodCode, DexInstruction_Invoke insnInvoke) {
    this(methodCode, insnInvoke, null);
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

  private DexPseudoinstruction_Invoke cloneThisInstruction() {
    DexInstruction_Invoke clonedInvoke = new DexInstruction_Invoke(instructionInvoke);
    DexInstruction clonedMove = null;
    if (movesResult()) {
      if (instructionMoveResult instanceof DexInstruction_MoveResult)
        clonedMove = new DexInstruction_MoveResult((DexInstruction_MoveResult) instructionMoveResult);
      else if (instructionMoveResult instanceof DexInstruction_MoveResultWide)
        clonedMove = new DexInstruction_MoveResultWide((DexInstruction_MoveResultWide) instructionMoveResult);
    }

    return new DexPseudoinstruction_Invoke(getMethodCode(), clonedInvoke, clonedMove);
  }

  private List<DexCodeElement> generatePreInternalCallCode(DexCode_InstrumentationState state) {
    val methodCode = getMethodCode();
    val dex = methodCode.getParentMethod().getParentClass().getParentFile();
    val callPrototype = instructionInvoke.getMethodPrototype();

    val codePreInternalCall = new LinkedList<DexCodeElement>();

    if (callPrototype.hasPrimitiveArgument()) {
      val regArray = new DexRegister();
      val regIndex = new DexRegister();

      val argTaintRegs = callPrototype.generateArgumentTaintStoringRegisters(
                           instructionInvoke.getArgumentRegisters(),
                           instructionInvoke.isStaticCall(),
                           state);

      codePreInternalCall.add(new DexInstruction_StaticGet(methodCode, regArray, dex.getMethodCallHelper_Arg()));

      codePreInternalCall.add(new DexInstruction_Invoke(methodCode, dex.getMethodCallHelper_SArgAcquire(), null));

      int arrayIndex = 0;
      for (val argTaintReg : argTaintRegs) {
        codePreInternalCall.add(new DexInstruction_Const(methodCode, regIndex, arrayIndex++));
        codePreInternalCall.add(new DexInstruction_ArrayPut(methodCode, argTaintReg, regArray, regIndex, Opcode_GetPut.IntFloat));
      }
    }

    return codePreInternalCall;
  }

  private List<DexCodeElement> generatePostInternalCallCode(DexCode_InstrumentationState state) {
    val methodCode = getMethodCode();
    val dex = methodCode.getParentMethod().getParentClass().getParentFile();
    val callPrototype = instructionInvoke.getMethodPrototype();

    val codePostInternalCall = new LinkedList<DexCodeElement>();

    if (callPrototype.getReturnType() instanceof DexPrimitiveType) {
      val regResSemaphore = new DexRegister();

      if (movesResult()) {
        if (instructionMoveResult instanceof DexInstruction_MoveResult) {
          val regTo = state.getTaintRegister(((DexInstruction_MoveResult) instructionMoveResult).getRegTo());
          codePostInternalCall.add(new DexInstruction_StaticGet(
                                     methodCode,
                                     regTo,
                                     dex.getMethodCallHelper_Res()));

        } else if (instructionMoveResult instanceof DexInstruction_MoveResultWide) {
          val regTo1 = state.getTaintRegister(((DexInstruction_MoveResultWide) instructionMoveResult).getRegTo1());
          val regTo2 = state.getTaintRegister(((DexInstruction_MoveResultWide) instructionMoveResult).getRegTo2());

          codePostInternalCall.add(new DexInstruction_StaticGet(
                                     methodCode,
                                     regTo1,
                                     dex.getMethodCallHelper_Res()));
          codePostInternalCall.add(new DexInstruction_Move(methodCode, regTo2, regTo1, false));
        }
      }

      codePostInternalCall.add(new DexInstruction_StaticGet(methodCode, regResSemaphore, dex.getMethodCallHelper_SRes()));
      codePostInternalCall.add(new DexInstruction_Invoke(
                                 methodCode,
                                 (DexClassType) dex.getMethodCallHelper_SRes().getType(),
                                 "release",
                                 new DexPrototype(DexVoid.parse("V", null), null),
                                 Arrays.asList(regResSemaphore),
                                 Opcode_Invoke.Virtual));
    }

    return codePostInternalCall;
  }

  private DexCodeElement[] instrumentDirectExternal(DexCode_InstrumentationState state) {
    return new DexCodeElement[] { this };
  }

  private DexCodeElement[] instrumentDirectInternal(DexCode_InstrumentationState state) {
    val instrumentedCode = new LinkedList<DexCodeElement>();

    instrumentedCode.addAll(generatePreInternalCallCode(state));
    instrumentedCode.add(this);
    instrumentedCode.addAll(generatePostInternalCallCode(state));

    return instrumentedCode.toArray(new DexCodeElement[instrumentedCode.size()]);
  }

  private DexCodeElement[] instrumentVirtual(DexCode_InstrumentationState state) {
    val methodCode = getMethodCode();
    val dex = methodCode.getParentMethod().getParentClass().getParentFile();

    val instrumentedCode = new LinkedList<DexCodeElement>();

    // try to find an internal class that extends the invoked class type
    // if there isn't one, the call will always be external
    val invokedClassType = instructionInvoke.getClassType();
    boolean canBeInternalCall = false;
    for (val clazz : dex.getClasses())
      if (dex.getClassHierarchy().isAncestor(clazz.getType(), invokedClassType)) {
        canBeInternalCall = true;
        break;
      }

    if (canBeInternalCall) {
      val regInternalInstance = new DexRegister();
      val labelExternal = new DexLabel(methodCode);
      val labelEnd = new DexLabel(methodCode);

      // TEST IF INSTANCEOF InternalClassInterface

      instrumentedCode.add(
        new DexInstruction_InstanceOf(
          methodCode,
          regInternalInstance,
          instructionInvoke.getArgumentRegisters().get(0),
          dex.getInternalClassInterface_Type()));
      instrumentedCode.add(
        new DexInstruction_IfTestZero(
          methodCode,
          regInternalInstance,
          labelExternal,
          Opcode_IfTestZero.eqz));

      // INTERNAL CALL

      instrumentedCode.addAll(generatePreInternalCallCode(state));
      instrumentedCode.add(cloneThisInstruction());
      instrumentedCode.addAll(generatePostInternalCallCode(state));

      instrumentedCode.add(new DexInstruction_Goto(methodCode, labelEnd));

      // EXTERNAL CALL

      instrumentedCode.add(labelExternal);
      instrumentedCode.add(this);

      instrumentedCode.add(labelEnd);
    } else {
      instrumentedCode.add(this);
    }

    return instrumentedCode.toArray(new DexCodeElement[instrumentedCode.size()]);
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
