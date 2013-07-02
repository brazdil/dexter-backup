package uk.ac.cam.db538.dexter.dex.code.insn;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction22c;
import org.junit.Test;

import uk.ac.cam.db538.dexter.dex.code.Utils;

public class DexInstruction_InstanceGet_Test {

  @Test
  public void testParse_InstanceGet() throws InstructionParsingException {
    Utils.parseAndCompare(
      new Instruction[] {
        new Instruction22c(Opcode.IGET_OBJECT, (byte) 0, (byte) 1, Utils.getFieldItem("Lcom/example/MyClass1;", "Ljava/lang/Object;", "TestField1")),
        new Instruction22c(Opcode.IGET, (byte) 2, (byte) 3, Utils.getFieldItem("Lcom/example/MyClass2;", "I", "TestField2")),
        new Instruction22c(Opcode.IGET, (byte) 4, (byte) 5, Utils.getFieldItem("Lcom/example/MyClass2;", "F", "TestField2B")),
        new Instruction22c(Opcode.IGET_BOOLEAN, (byte) 6, (byte) 7, Utils.getFieldItem("Lcom/example/MyClass3;", "Z", "TestField3")),
        new Instruction22c(Opcode.IGET_BYTE, (byte) 8, (byte) 9, Utils.getFieldItem("Lcom/example/MyClass4;", "B", "TestField4")),
        new Instruction22c(Opcode.IGET_CHAR, (byte) 10, (byte) 11, Utils.getFieldItem("Lcom/example/MyClass5;", "C", "TestField5")),
        new Instruction22c(Opcode.IGET_SHORT, (byte) 12, (byte) 13, Utils.getFieldItem("Lcom/example/MyClass6;", "S", "TestField6"))
      }, new String[] {
        "iget-object v0, {v1}com.example.MyClass1.TestField1",
        "iget-int-float v2, {v3}com.example.MyClass2.TestField2",
        "iget-int-float v4, {v5}com.example.MyClass2.TestField2B",
        "iget-boolean v6, {v7}com.example.MyClass3.TestField3",
        "iget-byte v8, {v9}com.example.MyClass4.TestField4",
        "iget-char v10, {v11}com.example.MyClass5.TestField5",
        "iget-short v12, {v13}com.example.MyClass6.TestField6"
      });
  }

  @Test(expected=InstructionArgumentException.class)
  public void testParse_InstanceGet_WrongType_Object() throws InstructionParsingException {
    Utils.parseAndCompare(
      new Instruction22c(Opcode.IGET_OBJECT, (byte) 10, (byte) 11, Utils.getFieldItem("Lcom/example/MyClass1;", "I", "TestField1")),
      "");
  }

  @Test(expected=InstructionArgumentException.class)
  public void testParse_InstanceGet_WrongType_Integer() throws InstructionParsingException {
    Utils.parseAndCompare(
      new Instruction22c(Opcode.IGET, (byte) 10, (byte) 11, Utils.getFieldItem("Lcom/example/MyClass1;", "Z", "TestField1")),
      "");
  }

  @Test(expected=InstructionArgumentException.class)
  public void testParse_InstanceGet_WrongType_Boolean() throws InstructionParsingException {
    Utils.parseAndCompare(
      new Instruction22c(Opcode.IGET_BOOLEAN, (byte) 10, (byte) 11, Utils.getFieldItem("Lcom/example/MyClass1;", "B", "TestField1")),
      "");
  }

  @Test(expected=InstructionArgumentException.class)
  public void testParse_InstanceGet_WrongType_Byte() throws InstructionParsingException {
    Utils.parseAndCompare(
      new Instruction22c(Opcode.IGET_BYTE, (byte) 10, (byte) 11, Utils.getFieldItem("Lcom/example/MyClass1;", "C", "TestField1")),
      "");
  }

  @Test(expected=InstructionArgumentException.class)
  public void testParse_InstanceGet_WrongType_Char() throws InstructionParsingException {
    Utils.parseAndCompare(
      new Instruction22c(Opcode.IGET_CHAR, (byte) 10, (byte) 11, Utils.getFieldItem("Lcom/example/MyClass1;", "S", "TestField1")),
      "");
  }

  @Test(expected=InstructionArgumentException.class)
  public void testParse_InstanceGet_WrongType_Short() throws InstructionParsingException {
    Utils.parseAndCompare(
      new Instruction22c(Opcode.IGET_SHORT, (byte) 10, (byte) 11, Utils.getFieldItem("Lcom/example/MyClass1;", "I", "TestField1")),
      "");
  }
}
