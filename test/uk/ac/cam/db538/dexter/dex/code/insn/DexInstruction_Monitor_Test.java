package uk.ac.cam.db538.dexter.dex.code.insn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import lombok.val;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction11x;
import org.junit.Test;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.Utils;

public class DexInstruction_Monitor_Test {

  @Test
  public void testParse() {
    Utils.parseAndCompare(
      new Instruction[] {
        new Instruction11x(Opcode.MONITOR_ENTER, (short) 255),
        new Instruction11x(Opcode.MONITOR_EXIT, (short) 254)
      }, new String[] {
        "monitor-enter v255",
        "monitor-exit v254"
      });
  }

  @Test
  public void testAssemble_Enter() {
    val regN = Utils.numFitsInto_Unsigned(8);
    val reg = new DexRegister(regN);
    val regAlloc = Utils.genRegAlloc(reg);

    val code = new DexCode();
    val insn = new DexInstruction_Monitor(code, reg, true);
    code.add(insn);

    val asm = insn.assembleBytecode(Utils.genAsmState(code, regAlloc));
    assertEquals(1, asm.length);
    assertTrue(asm[0] instanceof Instruction11x);

    val asmInsn = (Instruction11x) asm[0];
    assertEquals(Opcode.MONITOR_ENTER, asmInsn.opcode);
    assertEquals(regN, asmInsn.getRegisterA());
  }

  @Test
  public void testAssemble_Exit() {
    val regN = Utils.numFitsInto_Unsigned(8);
    val reg = new DexRegister(regN);
    val regAlloc = Utils.genRegAlloc(reg);

    val code = new DexCode();
    val insn = new DexInstruction_Monitor(code, reg, false);
    code.add(insn);

    val asm = insn.assembleBytecode(Utils.genAsmState(code, regAlloc));
    assertEquals(1, asm.length);
    assertTrue(asm[0] instanceof Instruction11x);

    val asmInsn = (Instruction11x) asm[0];
    assertEquals(Opcode.MONITOR_EXIT, asmInsn.opcode);
    assertEquals(regN, asmInsn.getRegisterA());
  }

  @Test(expected=InstructionAssemblyException.class)
  public void testAssemble_WrongAllocation() {
    val regN = Utils.numFitsInto_Unsigned(9);
    val reg = new DexRegister(regN);
    val regAlloc = Utils.genRegAlloc(reg);

    val code = new DexCode();
    val insn = new DexInstruction_Monitor(code, reg, true);
    code.add(insn);

    insn.assembleBytecode(Utils.genAsmState(code, regAlloc));
  }
}
