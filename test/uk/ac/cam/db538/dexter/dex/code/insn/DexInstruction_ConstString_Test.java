package uk.ac.cam.db538.dexter.dex.code.insn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import lombok.val;

import org.jf.dexlib.StringIdItem;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction21c;
import org.junit.Test;

import uk.ac.cam.db538.dexter.dex.code.DexRegister;
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

  @Test
  public void testAssemble() {
    val regNTo = Utils.numFitsInto_Unsigned(8);
    val regTo = new DexRegister(regNTo);
    val regAlloc = Utils.genRegAlloc(regTo);

    val insn = new DexInstruction_ConstString(null, regTo, "TEST");

    val asm = insn.assembleBytecode(Utils.genAsmState(regAlloc));
    assertEquals(1, asm.length);
    assertTrue(asm[0] instanceof Instruction21c);

    val asmInsn = (Instruction21c) asm[0];
    assertEquals(regNTo, asmInsn.getRegisterA());
    assertEquals("TEST", ((StringIdItem) asmInsn.getReferencedItem()).getStringValue());
  }

  @Test(expected=InstructionAssemblyException.class)
  public void testAssemble_WrongAllocation() {
    val regNTo = Utils.numFitsInto_Unsigned(9);
    val regTo = new DexRegister(regNTo);
    val regAlloc = Utils.genRegAlloc(regTo);

    val insn = new DexInstruction_ConstString(null, regTo, "TEST");

    insn.assembleBytecode(Utils.genAsmState(regAlloc));
  }
}
