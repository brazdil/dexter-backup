package uk.ac.cam.db538.dexter.dex.code.insn.macro;

import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import lombok.val;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCatch;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.elem.DexLabel;
import uk.ac.cam.db538.dexter.dex.code.elem.DexTryBlockEnd;
import uk.ac.cam.db538.dexter.dex.code.elem.DexTryBlockStart;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstructionVisitor;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ArrayPut;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Const;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ConstClass;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ConstString;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Goto;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Invoke;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Move;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_MoveResult;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_NewArray;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_StaticGet;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_GetPut;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_Invoke;
import uk.ac.cam.db538.dexter.dex.type.DexArrayType;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexPrimitiveType;
import uk.ac.cam.db538.dexter.dex.type.DexPrototype;
import uk.ac.cam.db538.dexter.dex.type.DexReferenceType;
import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;
import uk.ac.cam.db538.dexter.utils.NoDuplicatesList;

public class DexMacro_GetInternalMethodAnnotation extends DexMacro {

  // need to test the result for null (returns Annotation object)

  @Getter private final DexRegister regTo;
  @Getter private final DexRegister regDestObjectInstance;
  @Getter private final DexReferenceType invokedClass;
  @Getter private final String invokedMethodName;
  @Getter private final DexPrototype invokedMethodPrototype;

  public DexMacro_GetInternalMethodAnnotation(DexCode methodCode, DexRegister regTo, DexRegister regDestObjectInstance,
      DexReferenceType invokedClass, String methodName, DexPrototype methodPrototype) {
    super(methodCode);
    this.regTo = regTo;
    this.regDestObjectInstance = regDestObjectInstance;
    this.invokedClass = invokedClass;
    this.invokedMethodName = methodName;
    this.invokedMethodPrototype = methodPrototype;
  }

  @Override
  public List<? extends DexCodeElement> unwrap() {
    val methodCode = getMethodCode();
    val dex = getParentFile();
    val parsingCache = dex.getParsingCache();
    val classHierarchy = dex.getClassHierarchy();

    val instrumentedCode = new NoDuplicatesList<DexCodeElement>();

    val regDestObjectClass = new DexRegister();
    val regMethodName = new DexRegister();
    val regMethodArgumentsArray = new DexRegister();
    val regMethodArgumentsCount = new DexRegister();
    val regMethodArgumentsIndex = new DexRegister();
    val regMethodParamType = new DexRegister();
    val regMethodObject = new DexRegister();
    val regInternalAnnotationClass = new DexRegister();

    val paramTypes = invokedMethodPrototype.getParameterTypes();

    // get Class instance of the invoked object
    instrumentedCode.add(
      new DexInstruction_Invoke(
        methodCode,
        DexClassType.parse("Ljava/lang/Object;", parsingCache),
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
    val classType = DexClassType.parse("Ljava/lang/Class;", parsingCache);
    val getMethodPrototype = new DexPrototype(
      DexClassType.parse("Ljava/lang/reflect/Method;", parsingCache),
      Arrays.asList(new DexRegisterType[] {
                      DexClassType.parse("Ljava/lang/String;", parsingCache),
                      DexArrayType.parse("[Ljava/lang/Class;", parsingCache)
                    }));
    if (classHierarchy.isMethodPublic(invokedClass, invokedMethodName, invokedMethodPrototype)) {
      val getMethodParams = Arrays.asList(new DexRegister[] { regDestObjectClass, regMethodName, regMethodArgumentsArray } );
      instrumentedCode.add(new DexInstruction_Invoke(methodCode, classType, "getMethod", getMethodPrototype, getMethodParams, Opcode_Invoke.Virtual));
      instrumentedCode.add(new DexInstruction_MoveResult(methodCode, regMethodObject, true));
    } else {
      val catchBlock = new DexCatch(methodCode, DexClassType.parse("Ljava/lang/NoSuchMethodException;", parsingCache));
      val tryStart = new DexTryBlockStart(methodCode);
      val tryEnd = new DexTryBlockEnd(methodCode, tryStart);
      val labelBefore = new DexLabel(methodCode);
      val labelAfter = new DexLabel(methodCode);
      tryStart.addCatchHandler(catchBlock);

      val regCurrentClass = new DexRegister();
      val getMethodParams = Arrays.asList(new DexRegister[] { regCurrentClass, regMethodName, regMethodArgumentsArray } );

      instrumentedCode.add(new DexInstruction_Move(methodCode, regCurrentClass, regDestObjectClass, true));
      instrumentedCode.add(labelBefore);
      instrumentedCode.add(tryStart);
      instrumentedCode.add(new DexInstruction_Invoke(methodCode, classType, "getDeclaredMethod", getMethodPrototype, getMethodParams, Opcode_Invoke.Virtual));
      instrumentedCode.add(new DexInstruction_MoveResult(methodCode, regMethodObject, true));
      instrumentedCode.add(new DexInstruction_Goto(methodCode, labelAfter));
      instrumentedCode.add(tryEnd);
      instrumentedCode.add(catchBlock);
      instrumentedCode.add(new DexInstruction_Invoke(methodCode, classType, "getSuperclass", new DexPrototype(classType, null), Arrays.asList(regCurrentClass), Opcode_Invoke.Virtual));
      instrumentedCode.add(new DexInstruction_MoveResult(methodCode, regCurrentClass, true));
      instrumentedCode.add(new DexInstruction_Goto(methodCode, labelBefore));
      instrumentedCode.add(labelAfter);

    }
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
    instrumentedCode.add(new DexInstruction_MoveResult(methodCode, regTo, true));

    return instrumentedCode;
  }
  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }
}
