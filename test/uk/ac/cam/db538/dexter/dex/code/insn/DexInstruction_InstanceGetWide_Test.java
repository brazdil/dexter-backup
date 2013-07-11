package uk.ac.cam.db538.dexter.dex.code.insn;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction22c;
import org.junit.Test;

import uk.ac.cam.db538.dexter.dex.code.Utils;

public class DexInstruction_InstanceGetWide_Test {

  @Test
  public void testParse_InstanceGetWide() throws InstructionParseError {
    Utils.parseAndCompare(
      new Instruction[] {
        new Instruction22c(Opcode.IGET_WIDE, (byte) 0, (byte) 1, Utils.getFieldItem("Lcom/example/MyClass5;", "J", "TestField5")),
        new Instruction22c(Opcode.IGET_WIDE, (byte) 2, (byte) 3, Utils.getFieldItem("Lcom/example/MyClass6;", "D", "TestField6"))
      }, new String[] {
        "iget-wide v0, {v1}com.example.MyClass5.TestField5",
        "iget-wide v2, {v3}com.example.MyClass6.TestField6",
      });
  }

  @Test(expected=InstructionArgumentException.class)
  public void testParse_InstanceGetWide_WrongType() throws InstructionParseError {
    Utils.parseAndCompare(
      new Instruction22c(Opcode.IGET_WIDE, (byte) 0, (byte) 1, Utils.getFieldItem("Lcom/example/MyClass1;", "I", "TestField1")),
      "");
  }
}
