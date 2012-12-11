package uk.ac.cam.db538.dexter.dex.code.insn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import lombok.val;

import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction11x;
import org.junit.Test;

import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.Utils;

public class DexInstruction_Throw_Test {

  @Test
  public void testParse() {
    Utils.parseAndCompare(new Instruction11x(Opcode.THROW, (short) 255),
                          "throw v255");
  }

  @Test
  public void testAssemble() {
    val rFrom = Utils.numFitsInto_Unsigned(8);
    val regFrom = new DexRegister(rFrom);
    val regAlloc = Utils.genRegAlloc(regFrom);

    val insn = new DexInstruction_Throw(null, regFrom);

    val asm = insn.assembleBytecode(Utils.genAsmState(regAlloc));
    assertEquals(1, asm.length);
    assertTrue(asm[0] instanceof Instruction11x);

    val asmInsn = (Instruction11x) asm[0];
    assertEquals(Opcode.THROW, asmInsn.opcode);
    assertEquals(rFrom, asmInsn.getRegisterA());
  }

  @Test(expected=InstructionAssemblyException.class)
  public void testAssemble_WrongAllocation() {
    val rFrom = Utils.numFitsInto_Unsigned(9);
    val regFrom = new DexRegister(rFrom);
    val regAlloc = Utils.genRegAlloc(regFrom);

    val insn = new DexInstruction_Throw(null, regFrom);

    insn.assembleBytecode(Utils.genAsmState(regAlloc));
  }
}
