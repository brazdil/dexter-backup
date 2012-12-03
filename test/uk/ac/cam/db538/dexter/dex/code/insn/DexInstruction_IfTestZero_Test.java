package uk.ac.cam.db538.dexter.dex.code.insn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import lombok.val;

import org.jf.dexlib.DexFile;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction10x;
import org.jf.dexlib.Code.Format.Instruction21t;
import org.junit.Test;

import uk.ac.cam.db538.dexter.dex.DexAssemblingCache;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexLabel;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.Utils;

public class DexInstruction_IfTestZero_Test {

  @Test
  public void testParse_IfTestZero() {
    Utils.parseAndCompare(
      new Instruction[] {
        new Instruction10x(Opcode.NOP),
        new Instruction21t(Opcode.IF_EQZ, (short) 130, (short) -1),
        new Instruction21t(Opcode.IF_NEZ, (short) 140, (short) -3),
        new Instruction21t(Opcode.IF_LTZ, (short) 150, (short) -5),
        new Instruction21t(Opcode.IF_GEZ, (short) 160, (short) -7),
        new Instruction21t(Opcode.IF_GTZ, (short) 170, (short) 4),
        new Instruction21t(Opcode.IF_LEZ, (short) 180, (short) 2),
        new Instruction10x(Opcode.NOP)
      }, new String[] {
        "L0:",
        "nop",
        "if-eqz v130, L0",
        "if-nez v140, L0",
        "if-ltz v150, L0",
        "if-gez v160, L0",
        "if-gtz v170, L13",
        "if-lez v180, L13",
        "L13:",
        "nop"
      });
  }

  @Test
  public void testAssemble_IfTestZero() {
    val code = new DexCode();

    val regNum = Utils.numFitsInto_Unsigned(8);
    val reg = new DexRegister(regNum);
    val regAlloc = Utils.genRegAlloc(reg);

    val label = new DexLabel(code);
    val nop = new DexInstruction_Nop(code);
    val insn = new DexInstruction_IfTestZero(code, reg, label, Opcode_IfTestZero.eqz);
    code.add(label);
    code.add(nop);
    code.add(insn);

    val asm = code.assembleBytecode(regAlloc, new DexAssemblingCache(new DexFile()));
    assertEquals(2, asm.size());
    assertTrue(asm.get(0) instanceof Instruction10x);
    assertTrue(asm.get(1) instanceof Instruction21t);

    val asmInsn = (Instruction21t) asm.get(1);
    assertEquals(Opcode.IF_EQZ, asmInsn.opcode);
    assertEquals(regNum, asmInsn.getRegisterA());
    assertEquals(-1, asmInsn.getTargetAddressOffset());
  }

  @Test(expected=InstructionAssemblyException.class)
  public void testAssemble_IfTestZero_ZeroOffset() {
    val code = new DexCode();

    val regNum = Utils.numFitsInto_Unsigned(8);
    val reg = new DexRegister(regNum);
    val regAlloc = Utils.genRegAlloc(reg);

    val label = new DexLabel(code);
    val insn = new DexInstruction_IfTestZero(code, reg, label, Opcode_IfTestZero.eqz);
    code.add(label);
    code.add(insn);

    code.assembleBytecode(regAlloc, new DexAssemblingCache(new DexFile()));
  }

  @Test(expected=InstructionAssemblyException.class)
  public void testAssemble_IfTestZero_WrongAlloc() {
    val code = new DexCode();

    val regNum = Utils.numFitsInto_Unsigned(9);
    val reg = new DexRegister(regNum);
    val regAlloc = Utils.genRegAlloc(reg);

    val label = new DexLabel(code);
    val nop = new DexInstruction_Nop(code);
    val insn = new DexInstruction_IfTestZero(code, reg, label, Opcode_IfTestZero.eqz);
    code.add(label);
    code.add(nop);
    code.add(insn);

    code.assembleBytecode(regAlloc, new DexAssemblingCache(new DexFile()));
  }

  @Test
  public void testAssemble_IfTestZero_OffsetStillOK() {
    val code = new DexCode();

    val regNum = Utils.numFitsInto_Unsigned(8);
    val reg = new DexRegister(regNum);
    val regAlloc = Utils.genRegAlloc(reg);

    val label = new DexLabel(code);
    val nop = new DexInstruction_Nop(code);
    val insn = new DexInstruction_IfTestZero(code, reg, label, Opcode_IfTestZero.eqz);

    code.add(insn);
    for (int i = 0; i < 32765; ++i)
      code.add(new DexInstruction_Nop(code));
    code.add(label);
    code.add(nop);

    val asm = code.assembleBytecode(regAlloc, new DexAssemblingCache(new DexFile()));
    val asmInsn = (Instruction21t) asm.get(0);
    assertEquals(32767, asmInsn.getTargetAddressOffset());
  }

  @Test
  public void testAssemble_IfTestZero_OffsetTooLong() {
    val code = new DexCode();

    val regNum = Utils.numFitsInto_Unsigned(8);
    val reg = new DexRegister(regNum);
    val regAlloc = Utils.genRegAlloc(reg);

    val label = new DexLabel(code);
    val nop = new DexInstruction_Nop(code);
    val insn = new DexInstruction_IfTestZero(code, reg, label, Opcode_IfTestZero.eqz);

    code.add(insn);
    for (int i = 0; i < 32766; ++i)
      code.add(new DexInstruction_Nop(code));
    code.add(label);
    code.add(nop);

    try {
      code.assembleBytecode(regAlloc, new DexAssemblingCache(new DexFile()));
      fail("Should have thrown exception");
    } catch (InstructionOffsetException e) {
      assertEquals(insn, e.getProblematicInstruction());
    }
  }
}
