package uk.ac.cam.db538.dexter.dex.code.insn;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction21c;
import org.junit.Test;

import uk.ac.cam.db538.dexter.dex.code.Utils;

public class DexInstruction_StaticGet_Test {

  @Test
  public void testParse_StaticGet() throws InstructionParsingException {
    Utils.parseAndCompare(
      new Instruction[] {
        new Instruction21c(Opcode.SGET_OBJECT, (short) 236, Utils.getFieldItem("Lcom/example/MyClass1;", "Ljava/lang/Object;", "TestField1")),
        new Instruction21c(Opcode.SGET, (short) 237, Utils.getFieldItem("Lcom/example/MyClass2;", "I", "TestField2")),
        new Instruction21c(Opcode.SGET, (short) 230, Utils.getFieldItem("Lcom/example/MyClass2;", "F", "TestField2B")),
        new Instruction21c(Opcode.SGET_BOOLEAN, (short) 238, Utils.getFieldItem("Lcom/example/MyClass3;", "Z", "TestField3")),
        new Instruction21c(Opcode.SGET_BYTE, (short) 239, Utils.getFieldItem("Lcom/example/MyClass4;", "B", "TestField4")),
        new Instruction21c(Opcode.SGET_CHAR, (short) 240, Utils.getFieldItem("Lcom/example/MyClass5;", "C", "TestField5")),
        new Instruction21c(Opcode.SGET_SHORT, (short) 241, Utils.getFieldItem("Lcom/example/MyClass6;", "S", "TestField6"))
      }, new String[] {
        "sget-object v236, com.example.MyClass1.TestField1",
        "sget-int-float v237, com.example.MyClass2.TestField2",
        "sget-int-float v230, com.example.MyClass2.TestField2B",
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
}
