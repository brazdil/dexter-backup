package uk.ac.cam.db538.dexter.dex.code.insn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import lombok.val;

import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction11x;
import org.junit.Test;

import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.Utils;

public class DexInstruction_MoveResultWide_Test {

  @Test
  public void testParse() {
    Utils.parseAndCompare(new Instruction11x(Opcode.MOVE_RESULT_WIDE, (short) 255),
                          "move-result-wide v255|v256");
  }

  @Test
  public void testAssemble() {
    val rTo1 = Utils.numFitsInto_Unsigned(8);
    val rTo2 = rTo1 + 1;

    val regTo1 = new DexRegister(rTo1);
    val regTo2 = new DexRegister(rTo2);
    val regAlloc = Utils.genRegAlloc(regTo1, regTo2);

    val insn = new DexInstruction_MoveResultWide(null, regTo1, regTo2);

    val asm = insn.assembleBytecode(Utils.genAsmState(regAlloc));
    assertEquals(1, asm.length);
    assertTrue(asm[0] instanceof Instruction11x);

    val asmInsn = (Instruction11x) asm[0];
    assertEquals(Opcode.MOVE_RESULT_WIDE, asmInsn.opcode);
    assertEquals(rTo1, asmInsn.getRegisterA());
  }

  @Test(expected=InstructionAssemblyException.class)
  public void testAssemble_WrongAllocation_OutOfRange() {
    val rTo1 = Utils.numFitsInto_Unsigned(9);
    val rTo2 = rTo1 + 1;

    val regTo1 = new DexRegister(rTo1);
    val regTo2 = new DexRegister(rTo2);
    val regAlloc = Utils.genRegAlloc(regTo1, regTo2);

    val insn = new DexInstruction_MoveResultWide(null, regTo1, regTo2);

    insn.assembleBytecode(Utils.genAsmState(regAlloc));
  }

  @Test(expected=InstructionAssemblyException.class)
  public void testAssemble_WrongAllocation_Follow() {
    val rTo1 = Utils.numFitsInto_Unsigned(8);
    val rTo2 = rTo1 - 1;

    val regTo1 = new DexRegister(rTo1);
    val regTo2 = new DexRegister(rTo2);
    val regAlloc = Utils.genRegAlloc(regTo1, regTo2);

    val insn = new DexInstruction_MoveResultWide(null, regTo1, regTo2);

    insn.assembleBytecode(Utils.genAsmState(regAlloc));
  }
}

