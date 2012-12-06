package uk.ac.cam.db538.dexter.dex.code.insn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import lombok.val;

import org.jf.dexlib.DexFile;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction10x;
import org.jf.dexlib.Code.Format.Instruction22t;
import org.junit.Test;

import uk.ac.cam.db538.dexter.dex.DexAssemblingCache;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexLabel;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.Utils;

public class DexInstruction_IfTest_Test {

  @Test
  public void testParse_IfTest() {
    Utils.parseAndCompare(
      new Instruction[] {
        new Instruction10x(Opcode.NOP),
        new Instruction22t(Opcode.IF_EQ, (byte) 0, (byte) 1, (short) -1),
        new Instruction22t(Opcode.IF_NE, (byte) 2, (byte) 3, (short) -3),
        new Instruction22t(Opcode.IF_LT, (byte) 4, (byte) 5, (short) -5),
        new Instruction22t(Opcode.IF_GE, (byte) 6, (byte) 7, (short) -7),
        new Instruction22t(Opcode.IF_GT, (byte) 8, (byte) 9, (short) 4),
        new Instruction22t(Opcode.IF_LE, (byte) 10, (byte) 11, (short) 2),
        new Instruction10x(Opcode.NOP)
      }, new String[] {
        "L0:",
        "nop",
        "if-eq v0, v1, L0",
        "if-ne v2, v3, L0",
        "if-lt v4, v5, L0",
        "if-ge v6, v7, L0",
        "if-gt v8, v9, L13",
        "if-le v10, v11, L13",
        "L13:",
        "nop"
      });
  }

  @Test
  public void testAssemble_IfTest() {
    val code = new DexCode();

    val regANum = Utils.numFitsInto_Unsigned(4);
    val regBNum = Utils.numFitsInto_Unsigned(4) - 1;
    val regA = new DexRegister(regANum);
    val regB = new DexRegister(regBNum);
    val regAlloc = Utils.genRegAlloc(regA, regB);

    val label = new DexLabel(code);
    val nop = new DexInstruction_Nop(code);
    val insn = new DexInstruction_IfTest(code, regA, regB, label, Opcode_IfTest.eq);
    code.add(label);
    code.add(nop);
    code.add(insn);

    val asm = code.assembleBytecode(regAlloc, new DexAssemblingCache(new DexFile()));
    assertEquals(2, asm.size());
    assertTrue(asm.get(0) instanceof Instruction10x);
    assertTrue(asm.get(1) instanceof Instruction22t);

    val asmInsn = (Instruction22t) asm.get(1);
    assertEquals(Opcode.IF_EQ, asmInsn.opcode);
    assertEquals(regANum, asmInsn.getRegisterA());
    assertEquals(regBNum, asmInsn.getRegisterB());
    assertEquals(-1, asmInsn.getTargetAddressOffset());
  }

  @Test(expected=InstructionAssemblyException.class)
  public void testAssemble_IfTest_ZeroOffset() {
    val code = new DexCode();

    val regANum = Utils.numFitsInto_Unsigned(4);
    val regBNum = Utils.numFitsInto_Unsigned(4) - 1;
    val regA = new DexRegister(regANum);
    val regB = new DexRegister(regBNum);
    val regAlloc = Utils.genRegAlloc(regA, regB);

    val label = new DexLabel(code);
    val insn = new DexInstruction_IfTest(code, regA, regB, label, Opcode_IfTest.eq);
    code.add(label);
    code.add(insn);

    code.assembleBytecode(regAlloc, new DexAssemblingCache(new DexFile()));
  }

  @Test(expected=InstructionAssemblyException.class)
  public void testAssemble_IfTest_WrongAlloc_RegA() {
    val code = new DexCode();

    val regANum = Utils.numFitsInto_Unsigned(5);
    val regBNum = Utils.numFitsInto_Unsigned(4) - 1;
    val regA = new DexRegister(regANum);
    val regB = new DexRegister(regBNum);
    val regAlloc = Utils.genRegAlloc(regA, regB);

    val label = new DexLabel(code);
    val nop = new DexInstruction_Nop(code);
    val insn = new DexInstruction_IfTest(code, regA, regB, label, Opcode_IfTest.eq);
    code.add(label);
    code.add(nop);
    code.add(insn);

    code.assembleBytecode(regAlloc, new DexAssemblingCache(new DexFile()));
  }

  @Test(expected=InstructionAssemblyException.class)
  public void testAssemble_IfTest_WrongAlloc_RegB() {
    val code = new DexCode();

    val regANum = Utils.numFitsInto_Unsigned(4);
    val regBNum = Utils.numFitsInto_Unsigned(5);
    val regA = new DexRegister(regANum);
    val regB = new DexRegister(regBNum);
    val regAlloc = Utils.genRegAlloc(regA, regB);

    val label = new DexLabel(code);
    val nop = new DexInstruction_Nop(code);
    val insn = new DexInstruction_IfTest(code, regA, regB, label, Opcode_IfTest.eq);
    code.add(label);
    code.add(nop);
    code.add(insn);

    code.assembleBytecode(regAlloc, new DexAssemblingCache(new DexFile()));
  }

  @Test
  public void testAssemble_IfTest_OffsetTooLong() {
    val code = new DexCode();

    val regANum = Utils.numFitsInto_Unsigned(4);
    val regBNum = Utils.numFitsInto_Unsigned(4) - 1;
    val regA = new DexRegister(regANum);
    val regB = new DexRegister(regBNum);
    val regAlloc = Utils.genRegAlloc(regA, regB);

    val label = new DexLabel(code);
    val nop = new DexInstruction_Nop(code);
    val insn = new DexInstruction_IfTest(code, regA, regB, label, Opcode_IfTest.eq);

    code.add(insn);
    for (int i = 0; i < 32766; ++i)
      code.add(new DexInstruction_Nop(code));
    code.add(label);
    code.add(nop);

    try {
      code.disableJumpFixing();
      code.assembleBytecode(regAlloc, new DexAssemblingCache(new DexFile()));
      fail("Should have thrown exception");
    } catch (InstructionOffsetException e) {
      assertEquals(insn, e.getProblematicInstruction());
    }
  }

  @Test
  public void testFixLongJump() {
    val code = new DexCode();

    val label = new DexLabel(code);
    val regA = new DexRegister();
    val regB = new DexRegister();
    val opcode = Opcode_IfTest.eq;

    val insnIf = new DexInstruction_IfTest(code, regA, regB, label, opcode);
    val insnNop1 = new DexInstruction_Nop(code);
    val insnNop2 = new DexInstruction_Nop(code);

    code.add(insnIf);
    code.add(insnNop1);
    code.add(label);
    code.add(insnNop2);

    val fixedIfElems = insnIf.fixLongJump();
    assertEquals(5, fixedIfElems.length);

    assertTrue(fixedIfElems[0] instanceof DexInstruction_IfTest);
    assertTrue(fixedIfElems[1] instanceof DexInstruction_Goto);
    assertTrue(fixedIfElems[2] instanceof DexLabel);
    assertTrue(fixedIfElems[3] instanceof DexInstruction_Goto);
    assertTrue(fixedIfElems[4] instanceof DexLabel);

    val newIf = (DexInstruction_IfTest) fixedIfElems[0];
    val newGotoSucc = (DexInstruction_Goto) fixedIfElems[1];
    val newLabelLongJump = (DexLabel) fixedIfElems[2];
    val newGotoLongJump = (DexInstruction_Goto) fixedIfElems[3];
    val newLabelSucc = (DexLabel) fixedIfElems[4];

    assertEquals(regA, newIf.getRegA());
    assertEquals(regB, newIf.getRegB());
    assertEquals(newLabelLongJump, newIf.getTarget());
    assertEquals(opcode, newIf.getInsnOpcode());

    assertEquals(newLabelSucc, newGotoSucc.getTarget());
    assertEquals(label, newGotoLongJump.getTarget());
  }
}
