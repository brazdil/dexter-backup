package uk.ac.cam.db538.dexter.dex.code.insn;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction21c;
import org.junit.Test;

import uk.ac.cam.db538.dexter.dex.code.Utils;

public class DexInstruction_StaticPut_Test {

  @Test
  public void testParse_StaticPut() throws InstructionParsingException {
    Utils.parseAndCompare(
      new Instruction[] {
        new Instruction21c(Opcode.SPUT_OBJECT, (short) 236, Utils.getFieldItem("Lcom/example/MyClass1;", "Ljava/lang/Object;", "TestField1")),
        new Instruction21c(Opcode.SPUT, (short) 237, Utils.getFieldItem("Lcom/example/MyClass2;", "I", "TestField2")),
        new Instruction21c(Opcode.SPUT, (short) 230, Utils.getFieldItem("Lcom/example/MyClass2;", "F", "TestField2B")),
        new Instruction21c(Opcode.SPUT_BOOLEAN, (short) 238, Utils.getFieldItem("Lcom/example/MyClass3;", "Z", "TestField3")),
        new Instruction21c(Opcode.SPUT_BYTE, (short) 239, Utils.getFieldItem("Lcom/example/MyClass4;", "B", "TestField4")),
        new Instruction21c(Opcode.SPUT_CHAR, (short) 240, Utils.getFieldItem("Lcom/example/MyClass5;", "C", "TestField5")),
        new Instruction21c(Opcode.SPUT_SHORT, (short) 241, Utils.getFieldItem("Lcom/example/MyClass6;", "S", "TestField6"))
      }, new String[] {
        "sput-object v236, com.example.MyClass1.TestField1",
        "sput-int-float v237, com.example.MyClass2.TestField2",
        "sput-int-float v230, com.example.MyClass2.TestField2B",
        "sput-boolean v238, com.example.MyClass3.TestField3",
        "sput-byte v239, com.example.MyClass4.TestField4",
        "sput-char v240, com.example.MyClass5.TestField5",
        "sput-short v241, com.example.MyClass6.TestField6"
      });
  }

  @Test(expected=InstructionArgumentException.class)
  public void testParse_StaticPut_WrongType_Object() throws InstructionParsingException {
    Utils.parseAndCompare(
      new Instruction21c(Opcode.SPUT_OBJECT, (short) 236, Utils.getFieldItem("Lcom/example/MyClass1;", "I", "TestField1")),
      "");
  }

  @Test(expected=InstructionArgumentException.class)
  public void testParse_StaticPut_WrongType_Integer() throws InstructionParsingException {
    Utils.parseAndCompare(
      new Instruction21c(Opcode.SPUT, (short) 236, Utils.getFieldItem("Lcom/example/MyClass1;", "Z", "TestField1")),
      "");
  }

  @Test(expected=InstructionArgumentException.class)
  public void testParse_StaticPut_WrongType_Boolean() throws InstructionParsingException {
    Utils.parseAndCompare(
      new Instruction21c(Opcode.SPUT_BOOLEAN, (short) 236, Utils.getFieldItem("Lcom/example/MyClass1;", "B", "TestField1")),
      "");
  }

  @Test(expected=InstructionArgumentException.class)
  public void testParse_StaticPut_WrongType_Byte() throws InstructionParsingException {
    Utils.parseAndCompare(
      new Instruction21c(Opcode.SPUT_BYTE, (short) 236, Utils.getFieldItem("Lcom/example/MyClass1;", "C", "TestField1")),
      "");
  }

  @Test(expected=InstructionArgumentException.class)
  public void testParse_StaticPut_WrongType_Char() throws InstructionParsingException {
    Utils.parseAndCompare(
      new Instruction21c(Opcode.SPUT_CHAR, (short) 236, Utils.getFieldItem("Lcom/example/MyClass1;", "S", "TestField1")),
      "");
  }

  @Test(expected=InstructionArgumentException.class)
  public void testParse_StaticPut_WrongType_Short() throws InstructionParsingException {
    Utils.parseAndCompare(
      new Instruction21c(Opcode.SPUT_SHORT, (short) 236, Utils.getFieldItem("Lcom/example/MyClass1;", "I", "TestField1")),
      "");
  }
}
