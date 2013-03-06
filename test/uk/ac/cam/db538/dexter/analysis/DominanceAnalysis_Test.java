package uk.ac.cam.db538.dexter.analysis;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import lombok.val;

import org.junit.Test;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexLabel;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_BinaryOp;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Const;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Goto;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_IfTest;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Move;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_BinaryOp;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_IfTest;

public class DominanceAnalysis_Test {

  @Test
  public void testSingleBlock() {
    val code = new DexCode();

    val r0 = new DexRegister(0);
    val r1 = new DexRegister(1);
    val r2 = new DexRegister(2);
    val r3 = new DexRegister(3);

    val i0 = new DexInstruction_Const(code, r0, 1);
    code.add(i0);
    val i1 = new DexInstruction_Const(code, r1, 2);
    code.add(i1);
    val i2 = new DexInstruction_BinaryOp(code, r2, r0, r1, Opcode_BinaryOp.AddInt);
    code.add(i2);
    val i3 = new DexInstruction_Move(code, r3, r2, false);
    code.add(i3);

    val dom = new DominanceAnalysis(code);
    val block = dom.getCfg().getBasicBlocks().get(0);
    assertTrue(dom.isDominant(block, block));
  }

  @Test
  public void testBranchingBlocks() {
    val code = new DexCode();

    val r0 = new DexRegister(0);
    val r1 = new DexRegister(1);
    val r2 = new DexRegister(2);
    val r3 = new DexRegister(3);
    val r4 = new DexRegister(3);

    val i5 = new DexLabel(code, 0);
    val i7 = new DexLabel(code, 1);
    val i0 = new DexInstruction_Const(code, r0, 1);
    code.add(i0);
    val i1 = new DexInstruction_Const(code, r1, 2);
    code.add(i1);
    val i2 = new DexInstruction_IfTest(code, r4, r3, i5, Opcode_IfTest.eq);
    code.add(i2);
    val i3 = new DexInstruction_BinaryOp(code, r2, r0, r1, Opcode_BinaryOp.AddInt);
    code.add(i3);
    val i4 = new DexInstruction_Goto(code, i7);
    code.add(i4);
    code.add(i5);
    val i6 = new DexInstruction_Move(code, r3, r0, false);
    code.add(i6);
    code.add(i7);

    val dom = new DominanceAnalysis(code);
    val cfg = dom.getCfg();
    val basicBlocks = cfg.getBasicBlocks();

    val blockA = basicBlocks.get(0);
    val blockB = basicBlocks.get(1);
    val blockC = basicBlocks.get(2);
    val blockD = basicBlocks.get(3);
    assertTrue(blockA.getInstructions().contains(i0));
    assertTrue(blockB.getInstructions().contains(i3));
    assertTrue(blockC.getInstructions().contains(i6));
    assertTrue(blockD.getInstructions().contains(i7));

    assertTrue(dom.isDominant(blockA, blockA));
    assertTrue(dom.isDominant(blockA, blockB));
    assertTrue(dom.isDominant(blockA, blockC));
    assertTrue(dom.isDominant(blockA, blockD));

    assertFalse(dom.isDominant(blockB, blockC));
    assertFalse(dom.isDominant(blockC, blockB));

    assertFalse(dom.isDominant(blockB, blockD));
    assertFalse(dom.isDominant(blockD, blockB));

    assertFalse(dom.isDominant(blockB, blockC));
    assertFalse(dom.isDominant(blockC, blockB));
  }

  @Test
  public void testLoopingBlock() {
    val code = new DexCode();

    val r0 = new DexRegister(0);
    val r1 = new DexRegister(1);
    val r2 = new DexRegister(2);
    val r3 = new DexRegister(3);

    val i0 = new DexInstruction_Const(code, r0, 1);
    code.add(i0);
    val i1 = new DexInstruction_Const(code, r1, 2);
    code.add(i1);
    val i2 = new DexLabel(code, 0);
    code.add(i2);
    val i3 = new DexInstruction_Const(code, r2, 3);
    code.add(i3);
    val i4 = new DexInstruction_BinaryOp(code, r3, r0, r0, Opcode_BinaryOp.AddInt);
    code.add(i4);
    val i5 = new DexInstruction_IfTest(code, r1, r1, i2, Opcode_IfTest.eq);
    code.add(i5);
    val i6 = new DexInstruction_BinaryOp(code, r3, r2, r2, Opcode_BinaryOp.AddInt);
    code.add(i6);

    val dom = new DominanceAnalysis(code);
    val cfg = dom.getCfg();
    val basicBlocks = cfg.getBasicBlocks();

    val blockA = basicBlocks.get(0);
    val blockB = basicBlocks.get(1);
    val blockC = basicBlocks.get(2);
    assertTrue(blockA.getInstructions().contains(i0));
    assertTrue(blockB.getInstructions().contains(i3));
    assertTrue(blockC.getInstructions().contains(i6));

    assertTrue(dom.isDominant(blockA, blockA));
    assertTrue(dom.isDominant(blockA, blockB));
    assertTrue(dom.isDominant(blockA, blockC));

    assertFalse(dom.isDominant(blockB, blockC));
    assertFalse(dom.isDominant(blockC, blockB));
  }
}
