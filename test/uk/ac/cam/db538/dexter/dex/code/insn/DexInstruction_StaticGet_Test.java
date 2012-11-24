package uk.ac.cam.db538.dexter.dex.code.insn;

import static org.junit.Assert.*;
import lombok.val;

import org.jf.dexlib.DexFile;
import org.jf.dexlib.FieldIdItem;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction21c;
import org.junit.Test;

import uk.ac.cam.db538.dexter.dex.DexAssemblingCache;
import uk.ac.cam.db538.dexter.dex.DexParsingCache;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.Utils;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;

public class DexInstruction_StaticGet_Test {

  @Test
  public void testParse_StaticGet() throws InstructionParsingException {
    Utils.parseAndCompare(
      new Instruction[] {
        new Instruction21c(Opcode.SGET_OBJECT, (short) 236, Utils.getFieldItem("Lcom/example/MyClass1;", "Ljava/lang/Object;", "TestField1")),
        new Instruction21c(Opcode.SGET, (short) 237, Utils.getFieldItem("Lcom/example/MyClass2;", "I", "TestField2")),
        new Instruction21c(Opcode.SGET_BOOLEAN, (short) 238, Utils.getFieldItem("Lcom/example/MyClass3;", "Z", "TestField3")),
        new Instruction21c(Opcode.SGET_BYTE, (short) 239, Utils.getFieldItem("Lcom/example/MyClass4;", "B", "TestField4")),
        new Instruction21c(Opcode.SGET_CHAR, (short) 240, Utils.getFieldItem("Lcom/example/MyClass5;", "C", "TestField5")),
        new Instruction21c(Opcode.SGET_SHORT, (short) 241, Utils.getFieldItem("Lcom/example/MyClass6;", "S", "TestField6"))
      }, new String[] {
        "sget-object v236, com.example.MyClass1.TestField1",
        "sget-int v237, com.example.MyClass2.TestField2",
        "sget-boolean v238, com.example.MyClass3.TestField3",
        "sget-byte v239, com.example.MyClass4.TestField4",
        "sget-char v240, com.example.MyClass5.TestField5",
        "sget-short v241, com.example.MyClass6.TestField6"
      });
  }

  @Test(expected=InstructionArgumentException.class)
  public void testParse_StaticGet_WrongType_Object() throws InstructionParsingException {
    Utils.parseAndCompare(
      new Instruction21c(Opcode.SGET_OBJECT, (short) 236, Utils.getFieldItem("Lcom/example/MyClass1;", "I", "TestField1")),
      "");
  }

  @Test(expected=InstructionArgumentException.class)
  public void testParse_StaticGet_WrongType_Integer() throws InstructionParsingException {
    Utils.parseAndCompare(
      new Instruction21c(Opcode.SGET, (short) 236, Utils.getFieldItem("Lcom/example/MyClass1;", "Z", "TestField1")),
      "");
  }

  @Test(expected=InstructionArgumentException.class)
  public void testParse_StaticGet_WrongType_Boolean() throws InstructionParsingException {
    Utils.parseAndCompare(
      new Instruction21c(Opcode.SGET_BOOLEAN, (short) 236, Utils.getFieldItem("Lcom/example/MyClass1;", "B", "TestField1")),
      "");
  }

  @Test(expected=InstructionArgumentException.class)
  public void testParse_StaticGet_WrongType_Byte() throws InstructionParsingException {
    Utils.parseAndCompare(
      new Instruction21c(Opcode.SGET_BYTE, (short) 236, Utils.getFieldItem("Lcom/example/MyClass1;", "C", "TestField1")),
      "");
  }

  @Test(expected=InstructionArgumentException.class)
  public void testParse_StaticGet_WrongType_Char() throws InstructionParsingException {
    Utils.parseAndCompare(
      new Instruction21c(Opcode.SGET_CHAR, (short) 236, Utils.getFieldItem("Lcom/example/MyClass1;", "S", "TestField1")),
      "");
  }

  @Test(expected=InstructionArgumentException.class)
  public void testParse_StaticGet_WrongType_Short() throws InstructionParsingException {
    Utils.parseAndCompare(
      new Instruction21c(Opcode.SGET_SHORT, (short) 236, Utils.getFieldItem("Lcom/example/MyClass1;", "I", "TestField1")),
      "");
  }

  @Test
  public void testAssemble_NewInstance() {
    val cache = new DexParsingCache();

    val regNTo = Utils.numFitsInto_Unsigned(8);
    val regTo = new DexRegister(regNTo);
    val regAlloc = Utils.genRegAlloc(regTo);

    val insn = new DexInstruction_StaticGet(
      null,
      regTo,
      DexClassType.parse("Lcom/test/SomeClass;", cache),
      DexRegisterType.parse("Ljava/lang/String;", cache),
      "AwesomeField",
      Opcode_GetPut.Object);

    val asm = insn.assembleBytecode(regAlloc, new DexAssemblingCache(new DexFile()));
    assertEquals(1, asm.length);
    assertTrue(asm[0] instanceof Instruction21c);

    val asmInsn = (Instruction21c) asm[0];
    assertEquals(regNTo, asmInsn.getRegisterA());
    assertEquals(Opcode.SGET_OBJECT, asmInsn.opcode);

    val asmInsnRef = (FieldIdItem) asmInsn.getReferencedItem();
    assertEquals("Lcom/test/SomeClass;", asmInsnRef.getContainingClass().getTypeDescriptor());
    assertEquals("Ljava/lang/String;", asmInsnRef.getFieldType().getTypeDescriptor());
    assertEquals("AwesomeField", asmInsnRef.getFieldName().getStringValue());
  }

  @Test(expected=InstructionAssemblyException.class)
  public void testAssemble_StaticGet_WrongAllocation() {
    val cache = new DexParsingCache();

    val regNTo = Utils.numFitsInto_Unsigned(9);
    val regTo = new DexRegister(regNTo);
    val regAlloc = Utils.genRegAlloc(regTo);

    val insn = new DexInstruction_StaticGet(
      null,
      regTo,
      DexClassType.parse("Lcom/test/SomeClass;", cache),
      DexRegisterType.parse("Ljava/lang/String;", cache),
      "AwesomeField",
      Opcode_GetPut.Object);

    insn.assembleBytecode(regAlloc, new DexAssemblingCache(new DexFile()));
  }
}
