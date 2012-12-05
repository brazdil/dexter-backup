package uk.ac.cam.db538.dexter.dex.code.insn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import lombok.val;

import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction11x;
import org.junit.Test;

import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.Utils;

public class DexInstruction_MoveResult_Test {

  @Test
  public void testParse_Primitive() {
    Utils.parseAndCompare(new Instruction11x(Opcode.MOVE_RESULT, (short) 255),
                          "move-result v255");
  }

  @Test
  public void testParse_Object() {
    Utils.parseAndCompare(new Instruction11x(Opcode.MOVE_RESULT_OBJECT, (short) 254),
                          "move-result-object v254");
  }

  @Test
  public void testAssemble_Primitive() {
    val rTo = Utils.numFitsInto_Unsigned(8);
    val regTo = new DexRegister(rTo);
    val regAlloc = Utils.genRegAlloc(regTo);

    val insn = new DexInstruction_MoveResult(null, regTo, false);

    val asm = insn.assembleBytecode(Utils.genAsmState(regAlloc));
    assertEquals(1, asm.length);
    assertTrue(asm[0] instanceof Instruction11x);

    val asmInsn = (Instruction11x) asm[0];
    assertEquals(Opcode.MOVE_RESULT, asmInsn.opcode);
    assertEquals(rTo, asmInsn.getRegisterA());
  }

  @Test
  public void testAssemble_Object() {
    val rTo = Utils.numFitsInto_Unsigned(8);
    val regTo = new DexRegister(rTo);
    val regAlloc = Utils.genRegAlloc(regTo);

    val insn = new DexInstruction_MoveResult(null, regTo, true);

    val asm = insn.assembleBytecode(Utils.genAsmState(regAlloc));
    assertEquals(1, asm.length);
    assertTrue(asm[0] instanceof Instruction11x);

    val asmInsn = (Instruction11x) asm[0];
    assertEquals(Opcode.MOVE_RESULT_OBJECT, asmInsn.opcode);
    assertEquals(rTo, asmInsn.getRegisterA());
  }

  @Test(expected=InstructionAssemblyException.class)
  public void testAssemble_WrongAllocation() {
    val rTo = Utils.numFitsInto_Unsigned(9);
    val regTo = new DexRegister(rTo);
    val regAlloc = Utils.genRegAlloc(regTo);

    val insn = new DexInstruction_MoveResult(null, regTo, true);

    insn.assembleBytecode(Utils.genAsmState(regAlloc));
  }
}
