package uk.ac.cam.db538.dexter.dex.code.insn.pseudo;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ConstClass;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ConstString;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Goto;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_IfTestZero;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Invoke;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Move;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_MoveResult;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_MoveResultWide;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_NewArray;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_StaticGet;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_GetPut;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_IfTestZero;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_Invoke;
import uk.ac.cam.db538.dexter.dex.method.DexPrototype;
import uk.ac.cam.db538.dexter.dex.type.DexArrayType;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexPrimitiveType;
import uk.ac.cam.db538.dexter.dex.type.DexReferenceType;
import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;
import uk.ac.cam.db538.dexter.dex.type.DexType;
import uk.ac.cam.db538.dexter.dex.type.DexVoid;
import uk.ac.cam.db538.dexter.dex.type.hierarchy.ClassHierarchyException;
import uk.ac.cam.db538.dexter.utils.Pair;

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
    val parsingCache = state.getCache().getParsingCache();
    val semaphoreClass = DexClassType.parse("Ljava/util/concurrent/Semaphore;", parsingCache);
    val callPrototype = instructionInvoke.getMethodPrototype();

    val codePreInternalCall = new LinkedList<DexCodeElement>();

    if (callPrototype.hasPrimitiveArgument()) {
      val regArgSemaphore = new DexRegister();
      val regArray = new DexRegister();
      val regIndex = new DexRegister();

      val argTaintRegs = callPrototype.generateArgumentTaintStoringRegisters(
                           instructionInvoke.getArgumentRegisters(),
                           instructionInvoke.isStaticCall(),
                           state);

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

  private Pair<Boolean, Boolean> decideMethodCallDestination() {
    val dex = getMethodCode().getParentMethod().getParentClass().getParentFile();
    val classHierarchy = dex.getClassHierarchy();

    val invokedCallType = instructionInvoke.getCallType();
    val invokedClassType = instructionInvoke.getClassType();
    val invokedMethodName = instructionInvoke.getMethodName();
    val invokedMethodPrototype = instructionInvoke.getMethodPrototype();

    if (invokedCallType == Opcode_Invoke.Super) {

      // with super call we can always deduce the destination
      // by going through the parents (DexClassHierarchy will
      // return them ordered from the closest parent
      // to Object) and deciding based on the first implementation
      // we encounter

      // need to put TRUE here, because invokedClassType is already a parent
      for (val parentClass : classHierarchy.getAllParents(invokedClassType, true))
        if (classHierarchy.implementsMethod(parentClass, invokedMethodName, invokedMethodPrototype)) {
          if (parentClass.isDefinedInternally())
            return new Pair<Boolean, Boolean>(true, false); // will always be internal
          else
            return new Pair<Boolean, Boolean>(false, true); // will always be external
        }

      throw new ClassHierarchyException("Cannot determine the destination of super method call: " + invokedClassType.getPrettyName() + "." + invokedMethodName);

    } else {

      Set<DexClassType> potentialDestinationClasses;

      if (invokedCallType == Opcode_Invoke.Virtual) {

        // call destination class can be a child which implements the given method,
        // or it can be a parent which implements the given method

        potentialDestinationClasses = new HashSet<DexClassType>();
        potentialDestinationClasses.addAll(classHierarchy.getAllChildren(invokedClassType));
        potentialDestinationClasses.addAll(classHierarchy.getAllParents(invokedClassType, true));

      } else {

        // in the case of an interface, we need to look at all the classes
        // that implement it; class hierarchy will automatically return
        // all the ancestors of such classes as well

        potentialDestinationClasses = classHierarchy.getAllClassesImplementingInterface(invokedClassType);

      }

      boolean canBeInternal = false;
      boolean canBeExternal = false;

      for (val destClass : potentialDestinationClasses) {
        if (classHierarchy.implementsMethod(destClass, invokedMethodName, invokedMethodPrototype)) {
          if (destClass.isDefinedInternally())
            canBeInternal = true;
          else
            canBeExternal = true;
        }
      }

      if (!canBeInternal && !canBeExternal)
        throw new ClassHierarchyException("Cannot determine the destination of virtual/interface method call: " + invokedClassType.getPrettyName() + "." + invokedMethodName);

      return new Pair<Boolean, Boolean>(canBeInternal, canBeExternal);
    }
  }

  private DexCodeElement[] instrumentVirtual(DexCode_InstrumentationState state) {
    val instrumentedCode = new LinkedList<DexCodeElement>();
    val dex = getMethodCode().getParentMethod().getParentClass().getParentFile();
    val methodCode = getMethodCode();
    val parsingCache = state.getCache().getParsingCache();

    val destAnalysis = decideMethodCallDestination();
    boolean canBeInternalCall = destAnalysis.getValA();
    boolean canBeExternalCall = destAnalysis.getValB();
    boolean canBeAnyCall = canBeInternalCall && canBeExternalCall;

    val invokedClassType = instructionInvoke.getClassType();
    val invokedMethodName = instructionInvoke.getMethodName();
    val invokedMethodPrototype = instructionInvoke.getMethodPrototype();

    System.out.println(invokedClassType.getPrettyName() + " ... " + invokedMethodName);
    System.out.println("  internal: " + canBeInternalCall);
    System.out.println("  external: " + canBeExternalCall);

    DexLabel labelExternal = null;
    DexLabel labelEnd = null;

    if (canBeAnyCall) {
      labelExternal = new DexLabel(methodCode);
      labelEnd = new DexLabel(methodCode);

      val regDestObjectInstance = instructionInvoke.getArgumentRegisters().get(0);
      val regDestObjectClass = new DexRegister();
      val regMethodName = new DexRegister();
      val regMethodArgumentsArray = new DexRegister();
      val regMethodArgumentsCount = new DexRegister();
      val regMethodArgumentsIndex = new DexRegister();
      val regMethodParamType = new DexRegister();
      val regMethodObject = new DexRegister();
      val regInternalAnnotationClass = new DexRegister();
      val regInternalAnnotationInstance = new DexRegister();

      val paramTypes = invokedMethodPrototype.getParameterTypes();

      // test if the method has our annotation

      // get Class instance of the invoked object
      instrumentedCode.add(
        new DexInstruction_Invoke(
          methodCode,
          invokedClassType,
          "getClass",
          new DexPrototype(
            DexClassType.parse("Ljava/lang/Class;", parsingCache),
            null),
          Arrays.asList(new DexRegister[] { regDestObjectInstance } ),
          Opcode_Invoke.Virtual));
      instrumentedCode.add(new DexInstruction_MoveResult(methodCode, regDestObjectClass, true));
      // load the method name
      instrumentedCode.add(new DexInstruction_ConstString(methodCode, regMethodName, invokedMethodName));
      // load the method-argument count
      instrumentedCode.add(new DexInstruction_Const(methodCode, regMethodArgumentsCount, paramTypes.size()));
      // create method-argument array
      instrumentedCode.add(
        new DexInstruction_NewArray(methodCode,
                                    regMethodArgumentsArray,
                                    regMethodArgumentsCount,
                                    DexArrayType.parse("[Ljava/lang/Class;", parsingCache)));
      // load all the params
      int i = 0;
      for (val paramType : paramTypes) {
        // load the index
        instrumentedCode.add(new DexInstruction_Const(methodCode, regMethodArgumentsIndex, i++));
        // load the param type Class object
        if (paramType instanceof DexPrimitiveType) {
          val primitiveTypeClassField = ((DexPrimitiveType) paramType).getPrimitiveClassConstantField(parsingCache);
          instrumentedCode.add(
            new DexInstruction_StaticGet(
              methodCode,
              regMethodParamType,
              primitiveTypeClassField.getValA(),
              DexClassType.parse("Ljava/lang/Class;", parsingCache),
              primitiveTypeClassField.getValB(),
              Opcode_GetPut.Object));
        } else
          instrumentedCode.add(new DexInstruction_ConstClass(methodCode, regMethodParamType, (DexReferenceType) paramType));
        // store it in the array
        instrumentedCode.add(new DexInstruction_ArrayPut(methodCode, regMethodParamType, regMethodArgumentsArray, regMethodArgumentsIndex, Opcode_GetPut.Object));
      }
      // find the method
      instrumentedCode.add(
        new DexInstruction_Invoke(
          methodCode,
          DexClassType.parse("Ljava/lang/Class;", parsingCache),
          "getMethod",
          new DexPrototype(
            DexClassType.parse("Ljava/lang/reflect/Method;", parsingCache),
            Arrays.asList(new DexRegisterType[] {
                            DexClassType.parse("Ljava/lang/String;", parsingCache),
                            DexArrayType.parse("[Ljava/lang/Class;", parsingCache)
                          })),
          Arrays.asList(new DexRegister[] { regDestObjectClass, regMethodName, regMethodArgumentsArray } ),
          Opcode_Invoke.Virtual));
      instrumentedCode.add(new DexInstruction_MoveResult(methodCode, regMethodObject, true));
      // ask if it implements the Internal annotation
      instrumentedCode.add(new DexInstruction_ConstClass(methodCode, regInternalAnnotationClass, dex.getInternalMethodAnnotation_Type()));
      instrumentedCode.add(
        new DexInstruction_Invoke(
          methodCode,
          DexClassType.parse("Ljava/lang/reflect/Method;", parsingCache),
          "getAnnotation",
          new DexPrototype(
            DexClassType.parse("Ljava/lang/annotation/Annotation;", parsingCache),
            Arrays.asList(new DexRegisterType[] {
                            DexClassType.parse("Ljava/lang/Class;", parsingCache)
                          })),
          Arrays.asList(new DexRegister[] { regMethodObject, regInternalAnnotationClass } ),
          Opcode_Invoke.Virtual));
      instrumentedCode.add(new DexInstruction_MoveResult(methodCode, regInternalAnnotationInstance, true));
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
      instrumentedCode.add(this);
    }

    if (canBeAnyCall) {
      instrumentedCode.add(labelEnd);
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
