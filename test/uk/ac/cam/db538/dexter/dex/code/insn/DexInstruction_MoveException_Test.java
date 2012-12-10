package uk.ac.cam.db538.dexter.dex.code.insn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import lombok.val;

import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction11x;
import org.junit.Test;

import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.Utils;

public class DexInstruction_MoveException_Test {

  @Test
  public void testParse() {
    Utils.parseAndCompare(new Instruction11x(Opcode.MOVE_EXCEPTION, (short) 255),
                          "move-exception v255");
  }

  @Test
  public void testAssemble() {
    val rTo = Utils.numFitsInto_Unsigned(8);
    val regTo = new DexRegister(rTo);
    val regAlloc = Utils.genRegAlloc(regTo);

    val insn = new DexInstruction_MoveException(null, regTo);

    val asm = insn.assembleBytecode(Utils.genAsmState(regAlloc));
    assertEquals(1, asm.length);
    assertTrue(asm[0] instanceof Instruction11x);

    val asmInsn = (Instruction11x) asm[0];
    assertEquals(Opcode.MOVE_EXCEPTION, asmInsn.opcode);
    assertEquals(rTo, asmInsn.getRegisterA());
  }

  @Test(expected=InstructionAssemblyException.class)
  public void testAssemble_WrongAllocation() {
    val rTo = Utils.numFitsInto_Unsigned(9);
    val regTo = new DexRegister(rTo);
    val regAlloc = Utils.genRegAlloc(regTo);

    val insn = new DexInstruction_MoveException(null, regTo);

    insn.assembleBytecode(Utils.genAsmState(regAlloc));
  }
}
