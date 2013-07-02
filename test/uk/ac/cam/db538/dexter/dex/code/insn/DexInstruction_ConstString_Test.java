package uk.ac.cam.db538.dexter.dex.code.insn;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction21c;
import org.junit.Test;

import uk.ac.cam.db538.dexter.dex.code.Utils;

public class DexInstruction_ConstString_Test {

  @Test
  public void testParse() throws InstructionParsingException {
    Utils.parseAndCompare(
      new Instruction[] {
        new Instruction21c(Opcode.CONST_STRING, (short) 236, Utils.getStringItem("Hello, world!")),
        new Instruction21c(Opcode.CONST_STRING, (short) 237, Utils.getStringItem("Hello, \"world!")),
        new Instruction21c(Opcode.CONST_STRING, (short) 238, Utils.getStringItem("123456789012345")),
        new Instruction21c(Opcode.CONST_STRING, (short) 239, Utils.getStringItem("1234567890123456")),
        new Instruction21c(Opcode.CONST_STRING, (short) 240, Utils.getStringItem("12345678901234\""))
      }, new String[] {
        "const-string v236, \"Hello, world!\"",
        "const-string v237, \"Hello, \\\"world!\"",
        "const-string v238, \"123456789012345\"",
        "const-string v239, \"123456789012345...\"",
        "const-string v240, \"12345678901234\\...\""
      });
  }
}
