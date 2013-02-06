package uk.ac.cam.db538.dexter.dex.code.insn.pseudo;

import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import lombok.val;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.elem.DexLabel;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ArrayGet;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ArrayLength;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Const;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Goto;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_IfTest;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Invoke;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_MoveResult;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_NewInstance;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_GetPut;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_IfTest;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_Invoke;
import uk.ac.cam.db538.dexter.dex.method.DexPrototype;
import uk.ac.cam.db538.dexter.dex.type.DexArrayType;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexVoid;

public class DexPseudoinstruction_GetMethodCaller extends DexPseudoinstruction {

  @Getter private final DexRegister regTo;

  public DexPseudoinstruction_GetMethodCaller(DexCode methodCode, DexRegister regTo) {
    super(methodCode);
    this.regTo = regTo;
  }

  @Override
  public List<DexCodeElement> unwrap() {
    val code = getMethodCode();
    val parsingCache = getParentFile().getParsingCache();

    val typeVoid = DexVoid.parse("V", parsingCache);
    val typeException = DexClassType.parse("Ljava/lang/Exception;", parsingCache);
    val typeString = DexClassType.parse("Ljava/lang/String;", parsingCache);
    val typeStackTraceElement = DexClassType.parse("Ljava/lang/StackTraceElement;", parsingCache);
    val typeStackTraceElementArray = DexArrayType.parse("[Ljava/lang/StackTraceElement;", parsingCache);

    val regException = new DexRegister();
    val regStackArray = new DexRegister();
    val regStackArrayLength = new DexRegister();
    val regConstOne = new DexRegister();
    val regCallerInfo = new DexRegister();

    val labelStackArrayNonempty = new DexLabel(code);
    val labelEnd = new DexLabel(code);

    return Arrays.asList(new DexCodeElement[] {
                           // regException = new Exception()
                           new DexInstruction_NewInstance(
                             code,
                             regException,
                             typeException),
                           new DexInstruction_Invoke(
                             code,
                             typeException,
                             "<init>",
                             new DexPrototype(typeVoid, null),
                             createList(regException),
                             Opcode_Invoke.Direct),
                           // regStackArray = regException.getStackTrace()
                           new DexInstruction_Invoke(
                             code,
                             typeException,
                             "getStackTrace",
                             new DexPrototype(typeStackTraceElementArray, null),
                             createList(regException),
                             Opcode_Invoke.Virtual),
                           new DexInstruction_MoveResult(
                             code,
                             regStackArray,
                             true),
                           // regConstOne = 1
                           new DexInstruction_Const(code, regConstOne, 1),
                           // if regStackArray.length <= 1
                           new DexInstruction_ArrayLength(code, regStackArrayLength, regStackArray),
                           new DexInstruction_IfTest(code, regStackArrayLength, regConstOne, labelStackArrayNonempty, Opcode_IfTest.gt),
                           // regTo = null
                           new DexInstruction_Const(code, regTo, 0),
                           // exit
                           new DexInstruction_Goto(code, labelEnd),
                           // else
                           labelStackArrayNonempty,
                           // regCallerInfo = regStackArray[regConstOne]
                           new DexInstruction_ArrayGet(
                             code,
                             regCallerInfo,
                             regStackArray,
                             regConstOne,
                             Opcode_GetPut.Object),
                           // regTo = regCallerInfo.getClassName()
                           new DexInstruction_Invoke(
                             code,
                             typeStackTraceElement,
                             "getClassName",
                             new DexPrototype(typeString, null),
                             createList(regCallerInfo),
                             Opcode_Invoke.Virtual),
                           new DexInstruction_MoveResult(
                             code,
                             regTo,
                             true),
                           labelEnd
                         });
  }
}
