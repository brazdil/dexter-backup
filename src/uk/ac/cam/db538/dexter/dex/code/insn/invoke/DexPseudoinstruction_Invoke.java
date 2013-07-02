package uk.ac.cam.db538.dexter.dex.code.insn.invoke;

import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import lombok.val;
import uk.ac.cam.db538.dexter.dex.DexInstrumentationCache.InstrumentationWarning;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.elem.DexLabel;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstructionVisitor;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ArrayPut;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Const;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Goto;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_IfTestZero;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Invoke;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_MoveResult;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_MoveResultWide;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_StaticGet;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_GetPut;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_IfTestZero;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_Invoke;
import uk.ac.cam.db538.dexter.dex.code.insn.macro.DexMacro_PrintStringConst;
import uk.ac.cam.db538.dexter.dex.code.insn.macro.DexMacro;
import uk.ac.cam.db538.dexter.dex.code.insn.macro.DexMacro_GetInternalMethodAnnotation;
import uk.ac.cam.db538.dexter.dex.code.insn.macro.DexMacro_GetObjectTaint;
import uk.ac.cam.db538.dexter.dex.code.insn.macro.DexMacro_PrintInteger;
import uk.ac.cam.db538.dexter.dex.method.DexPrototype;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexPrimitiveType;
import uk.ac.cam.db538.dexter.dex.type.DexType;
import uk.ac.cam.db538.dexter.dex.type.DexVoid;
import uk.ac.cam.db538.dexter.utils.NoDuplicatesList;

public class DexPseudoinstruction_Invoke extends DexMacro {

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

  public boolean movesResult() {
    return instructionMoveResult != null;
  }

  @Override
  public List<? extends DexCodeElement> unwrap() {
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
    val printDebug = state.getCache().isInsertDebugLogging();

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

    if (printDebug) {
      codePreInternalCall.add(new DexMacro_PrintStringConst(
                                methodCode,
                                "$ " + methodCode.getParentClass().getType().getShortName() + "->" + methodCode.getParentMethod().getName() + ": " +
                                "internal call to " + instructionInvoke.getClassType().getPrettyName() + "->" + instructionInvoke.getMethodName(),
                                true));
    }

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
    val printDebug = state.getCache().isInsertDebugLogging();

    val methodCode = getMethodCode();
    val dex = getParentFile();
    val callPrototype = instructionInvoke.getMethodPrototype();

    val codePostInternalCall = new NoDuplicatesList<DexCodeElement>();

    if (callPrototype.getReturnType() instanceof DexPrimitiveType) {
      val regResSemaphore = new DexRegister();

      if (movesResult()) {
        if (printDebug) {
          codePostInternalCall.add(new DexMacro_PrintStringConst(
                                     methodCode,
                                     "$ " + methodCode.getParentClass().getType().getShortName() + "->" + methodCode.getParentMethod().getName() + ": " +
                                     "internal result from " + instructionInvoke.getClassType().getPrettyName() + "->" + instructionInvoke.getMethodName() +
                                     " => ",
                                     false));
        }

        DexRegister regToTaint = null;
        if (instructionMoveResult instanceof DexInstruction_MoveResult)
          regToTaint = state.getTaintRegister(((DexInstruction_MoveResult) instructionMoveResult).getRegTo());
        else if (instructionMoveResult instanceof DexInstruction_MoveResultWide)
          regToTaint = state.getTaintRegister(((DexInstruction_MoveResultWide) instructionMoveResult).getRegTo1());
        codePostInternalCall.add(new DexInstruction_StaticGet(methodCode, regToTaint, dex.getMethodCallHelper_Res()));

        if (printDebug) {
          codePostInternalCall.add(new DexMacro_PrintInteger(methodCode, regToTaint, true));
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
    } else {
      if (movesResult()) {
        val regResult = ((DexInstruction_MoveResult) instructionMoveResult).getRegTo();
        val regResultTaint = state.getTaintRegister(regResult);
        codePostInternalCall.add(new DexMacro_GetObjectTaint(methodCode, regResultTaint, regResult));

        if (printDebug) {
          codePostInternalCall.add(new DexMacro_PrintStringConst(
                                     methodCode,
                                     "$ " + methodCode.getParentClass().getType().getShortName() + "->" + methodCode.getParentMethod().getName() + ": " +
                                     "internal result from " + instructionInvoke.getClassType().getPrettyName() + "->" + instructionInvoke.getMethodName() +
                                     " => ",
                                     false));
          codePostInternalCall.add(new DexMacro_PrintInteger(methodCode, regResultTaint, true));
        }
      }
    }

    return codePostInternalCall;
  }

  private List<DexCodeElement> generateExternalCallCode(DexCode_InstrumentationState state) {
    ExternalCallInstrumentor instrumentor = null;

    for (val inst : ExternalCallInstrumentor.getInstrumentors()) {
      if (inst.canBeApplied(this)) {
        if (instrumentor == null)
          instrumentor = inst;
        else
          throw new Error("Multiple instrumentors can be applied to external call: " +
                          instructionInvoke.getClassType().getPrettyName() + "." + instructionInvoke.getMethodName());
      }
    }

    if (instrumentor == null)
      instrumentor = new FallbackInstrumentor();

    val instrumentation = instrumentor.generateInstrumentation(this, state);

    val instrumentedCode = new NoDuplicatesList<DexCodeElement>();
    instrumentedCode.addAll(instrumentation.getValA());
    instrumentedCode.add(this);
    instrumentedCode.addAll(instrumentation.getValB());

    return instrumentedCode;
  }

  private void instrumentDirectStatic(DexCode_InstrumentationState state) {
    val destAnalysis = getParentFile().getClassHierarchy().decideMethodCallDestination(
                         instructionInvoke.getCallType(),
                         instructionInvoke.getClassType(),
                         instructionInvoke.getMethodName(),
                         instructionInvoke.getMethodPrototype());
    if (destAnalysis.getValA())
      instrumentDirectInternal(state);
    else if (destAnalysis.getValB())
      instrumentDirectExternal(state);
  }

  private void instrumentDirectExternal(DexCode_InstrumentationState state) {
    getMethodCode().replace(this, generateExternalCallCode(state));
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
    boolean canBeInternalCall = destAnalysis.getValA();
    boolean canBeExternalCall = destAnalysis.getValB();
    boolean canBeAnyCall = canBeInternalCall && canBeExternalCall;
    boolean canBeNeitherCall = !canBeInternalCall && !canBeExternalCall;

    if (canBeNeitherCall) {
      val callType = instructionInvoke.getCallType();
      val callClass = instructionInvoke.getClassType();
      val methodName = instructionInvoke.getMethodName();
      state.getCache().getWarnings().add(new InstrumentationWarning("Invoke destination not found: calling " + callType.name().toLowerCase() + " " + callClass.getPrettyName() + "." + methodName));

      canBeExternalCall = true;
    }

    DexLabel labelExternal = null;
    DexLabel labelEnd = null;

    if (canBeAnyCall) {
      labelExternal = new DexLabel(methodCode);
      labelEnd = new DexLabel(methodCode);

      val regInternalAnnotationInstance = new DexRegister();
      val regDestObjectInstance = instructionInvoke.getArgumentRegisters().get(0);

      // test if the method has our annotation
      instrumentedCode.add(new DexMacro_GetInternalMethodAnnotation(
                             methodCode,
                             regInternalAnnotationInstance,
                             regDestObjectInstance,
                             instructionInvoke.getClassType(),
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
      instrumentedCode.addAll(generateExternalCallCode(state));
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
      instrumentDirectStatic(state);
      break;
    case Interface:
    case Super:
    case Virtual:
    default:
      instrumentVirtual(state);
      break;
    }
  }

  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }
}
