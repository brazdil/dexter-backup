package uk.ac.cam.db538.dexter.dex.code.insn.pseudo;

import java.util.Arrays;
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
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_BinaryOp;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Const;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Goto;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_IfTestZero;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Invoke;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Move;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_MoveResult;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_MoveResultWide;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_StaticGet;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_BinaryOp;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_GetPut;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_IfTestZero;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_Invoke;
import uk.ac.cam.db538.dexter.dex.method.DexPrototype;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexPrimitiveType;
import uk.ac.cam.db538.dexter.dex.type.DexReferenceType;
import uk.ac.cam.db538.dexter.dex.type.DexType;
import uk.ac.cam.db538.dexter.dex.type.DexVoid;
import uk.ac.cam.db538.dexter.utils.NoDuplicatesList;

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
    val dex = getParentFile();
    val parsingCache = dex.getParsingCache();
    val semaphoreClass = DexClassType.parse("Ljava/util/concurrent/Semaphore;", parsingCache);
    val callPrototype = instructionInvoke.getMethodPrototype();

    val hasPrimitiveArgument = callPrototype.hasPrimitiveArgument();

    val codePreInternalCall = new NoDuplicatesList<DexCodeElement>();

    val regArgSemaphore = new DexRegister();
    val regArray = new DexRegister();
    val regIndex = new DexRegister();

    codePreInternalCall.add(new DexPseudoinstruction_PrintStringConst(
                              methodCode,
                              "$$$ INTERNAL CALL: " + instructionInvoke.getClassType().getPrettyName() + "..." + instructionInvoke.getMethodName(),
                              true));

    if (hasPrimitiveArgument) {
      codePreInternalCall.add(new DexInstruction_StaticGet(
                                methodCode,
                                regArgSemaphore,
                                dex.getMethodCallHelper_SArg()));
      codePreInternalCall.add(new DexInstruction_Invoke(
                                methodCode,
                                semaphoreClass,
                                "acquire",
                                new DexPrototype(DexType.parse("V", null), null),
                                Arrays.asList(new DexRegister[] { regArgSemaphore }),
                                Opcode_Invoke.Virtual));

      codePreInternalCall.add(new DexInstruction_StaticGet(methodCode, regArray, dex.getMethodCallHelper_Arg()));
      int arrayIndex = 0;
      int paramIndex = instructionInvoke.isStaticCall() ? 0 : 1;
      for (val paramType : callPrototype.getParameterTypes()) {
        if (paramType instanceof DexPrimitiveType) {
          codePreInternalCall.add(new DexInstruction_Const(methodCode, regIndex, arrayIndex));
          codePreInternalCall.add(new DexInstruction_ArrayPut(
                                    methodCode,
                                    state.getTaintRegister(instructionInvoke.getArgumentRegisters().get(paramIndex)),
                                    regArray,
                                    regIndex,
                                    Opcode_GetPut.IntFloat));
          arrayIndex++;
        }
        paramIndex += paramType.getRegisters();
      }
    }

    return codePreInternalCall;
  }

  private List<DexCodeElement> generatePostInternalCallCode(DexCode_InstrumentationState state) {
    val methodCode = getMethodCode();
    val dex = getParentFile();
    val callPrototype = instructionInvoke.getMethodPrototype();

    val codePostInternalCall = new NoDuplicatesList<DexCodeElement>();

    if (callPrototype.getReturnType() instanceof DexPrimitiveType) {
      val regResSemaphore = new DexRegister();

      if (movesResult()) {
        codePostInternalCall.add(new DexPseudoinstruction_PrintStringConst(
                                   methodCode,
                                   "$$$ INTERNAL RESULT: " + instructionInvoke.getClassType().getPrettyName() + "..." + instructionInvoke.getMethodName(),
                                   true));
        codePostInternalCall.add(new DexPseudoinstruction_PrintStringConst(
                                   methodCode,
                                   "$$$  RES = ",
                                   false));

        DexRegister regTo = null;
        if (instructionMoveResult instanceof DexInstruction_MoveResult)
          regTo = state.getTaintRegister(((DexInstruction_MoveResult) instructionMoveResult).getRegTo());
        else if (instructionMoveResult instanceof DexInstruction_MoveResultWide)
          regTo = state.getTaintRegister(((DexInstruction_MoveResultWide) instructionMoveResult).getRegTo1());
        codePostInternalCall.add(new DexInstruction_StaticGet(methodCode, regTo, dex.getMethodCallHelper_Res()));
        codePostInternalCall.add(new DexPseudoinstruction_PrintInteger(methodCode, regTo, true));
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

  private List<DexCodeElement> generatePreExternalCallCode(DexRegister regCombinedTaint, DexCode_InstrumentationState state) {
    val codePreExternalCall = new NoDuplicatesList<DexCodeElement>();
    val methodCode = getMethodCode();
    val isStaticCall = (instructionInvoke.getCallType() == Opcode_Invoke.Static);

    val methodPrototype = instructionInvoke.getMethodPrototype();
    val methodParameterRegs = instructionInvoke.getArgumentRegisters();

    codePreExternalCall.add(new DexPseudoinstruction_PrintStringConst(
                              methodCode,
                              "$$$ EXTERNAL CALL: " + instructionInvoke.getClassType().getPrettyName() + "..." + instructionInvoke.getMethodName(),
                              true));

    // if there are any parameters...
    if (!methodPrototype.getParameterTypes().isEmpty()) {
      // combine the taint of the object (if not static call) and all the parameters

      val regObjectArgTaint = new DexRegister();
      if (isStaticCall)
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

      if (!isStaticCall)
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

  private void instrumentDirectExternal(DexCode_InstrumentationState state) {
    val instrumentedCode = new NoDuplicatesList<DexCodeElement>();
    val regCombinedTaint = new DexRegister();

    instrumentedCode.addAll(generatePreExternalCallCode(regCombinedTaint, state));
    instrumentedCode.add(this);

    getMethodCode().replace(this, instrumentedCode);
  }

  private void instrumentDirectInternal(DexCode_InstrumentationState state) {
    val instrumentedCode = new NoDuplicatesList<DexCodeElement>();

    instrumentedCode.addAll(generatePreInternalCallCode(state));
    instrumentedCode.add(this);
    instrumentedCode.addAll(generatePostInternalCallCode(state));

    getMethodCode().replace(this, instrumentedCode);
  }

  private void instrumentVirtual(DexCode_InstrumentationState state) {
    val instrumentedCode = new NoDuplicatesList<DexCodeElement>();
    val methodCode = getMethodCode();

    val destAnalysis = getParentFile().getClassHierarchy().decideMethodCallDestination(
                         instructionInvoke.getCallType(),
                         instructionInvoke.getClassType(),
                         instructionInvoke.getMethodName(),
                         instructionInvoke.getMethodPrototype());
    val canBeInternalCall = destAnalysis.getValA();
    val canBeExternalCall = destAnalysis.getValB();
    val canBeAnyCall = canBeInternalCall && canBeExternalCall;

    DexLabel labelExternal = null;
    DexLabel labelEnd = null;

    if (canBeAnyCall) {
      labelExternal = new DexLabel(methodCode);
      labelEnd = new DexLabel(methodCode);

      val regInternalAnnotationInstance = new DexRegister();
      val regDestObjectInstance = instructionInvoke.getArgumentRegisters().get(0);

      // test if the method has our annotation
      instrumentedCode.add(new DexPseudoinstruction_GetInternalMethodAnnotation(
                             methodCode,
                             regInternalAnnotationInstance,
                             regDestObjectInstance,
                             instructionInvoke.getMethodName(),
                             instructionInvoke.getMethodPrototype()));

      // jump to external if the result above is null
      instrumentedCode.add(new DexInstruction_IfTestZero(methodCode, regInternalAnnotationInstance, labelExternal, Opcode_IfTestZero.eqz));
    }

    if (canBeInternalCall) {
      instrumentedCode.addAll(generatePreInternalCallCode(state));
      instrumentedCode.add(cloneThisInstruction());
      instrumentedCode.addAll(generatePostInternalCallCode(state));
    }

    if (canBeAnyCall) {
      instrumentedCode.add(new DexInstruction_Goto(methodCode, labelEnd));
      instrumentedCode.add(labelExternal);
    }

    if (canBeExternalCall) {
      val regCombinedTaint = new DexRegister();
      instrumentedCode.addAll(generatePreExternalCallCode(regCombinedTaint, state));
      instrumentedCode.add(this);
    }

    if (canBeAnyCall) {
      instrumentedCode.add(labelEnd);
    }

    methodCode.replace(this, instrumentedCode);
  }

  @Override
  public void instrument(DexCode_InstrumentationState state) {
    switch (instructionInvoke.getCallType()) {
    case Direct:
    case Static:
      if (instructionInvoke.getClassType().isDefinedInternally())
        instrumentDirectInternal(state);
      else
        instrumentDirectExternal(state);
      break;
    case Interface:
    case Super:
    case Virtual:
    default:
      instrumentVirtual(state);
      break;
    }
  }
}
