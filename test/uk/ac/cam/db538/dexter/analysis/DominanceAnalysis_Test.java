package uk.ac.cam.db538.dexter.analysis;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import lombok.val;

import org.junit.Test;

import uk.ac.cam.db538.dexter.analysis.cfg.CfgBasicBlock;
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

    assertTrue(dom.isDominant(blockB, blockC));
    assertFalse(dom.isDominant(blockC, blockB));
  }

  @Test
  public void testAllCombined() {
    val code = new DexCode();

    val r0 = new DexRegister(0);

    val lLoop = new DexLabel(code);
    val lIfTarget1 = new DexLabel(code);
    val lIfTarget2 = new DexLabel(code);
    val lIfEnd1 = new DexLabel(code);
    val lIfEnd2 = new DexLabel(code);
    val i0 = new DexInstruction_Const(code, r0, 1);
    val i1 = new DexInstruction_IfTest(code, r0, r0, lIfTarget1, Opcode_IfTest.eq);
    val i2 = new DexInstruction_IfTest(code, r0, r0, lIfTarget2, Opcode_IfTest.eq);
    val i3 = new DexInstruction_Goto(code, lIfEnd2);
    val i4 = new DexInstruction_Const(code, r0, 3);
    val i5 = new DexInstruction_Goto(code, lIfEnd1);
    val i6 = new DexInstruction_Const(code, r0, 4);
    val i7 = new DexInstruction_Goto(code, lLoop);

    code.add(i0);
    code.add(lLoop);
    code.add(i1);
    code.add(i2);
    code.add(i3);
    code.add(lIfTarget2);
    code.add(i4);
    code.add(lIfEnd2);
    code.add(i5);
    code.add(lIfTarget1);
    code.add(i6);
    code.add(lIfEnd1);
    code.add(i7);

    val dom = new DominanceAnalysis(code);
    val cfg = dom.getCfg();
    val basicBlocks = cfg.getBasicBlocks();

    val block1 = basicBlocks.get(0);
    val block2 = basicBlocks.get(1);
    val block3 = basicBlocks.get(2);
    val block4 = basicBlocks.get(3);
    val block5 = basicBlocks.get(4);
    val block6 = basicBlocks.get(5);
    val block7 = basicBlocks.get(6);
    val block8 = basicBlocks.get(7);
    assertTrue(block1.getInstructions().contains(i0));
    assertTrue(block2.getInstructions().contains(i1));
    assertTrue(block3.getInstructions().contains(i2));
    assertTrue(block4.getInstructions().contains(i3));
    assertTrue(block5.getInstructions().contains(i4));
    assertTrue(block6.getInstructions().contains(i5));
    assertTrue(block7.getInstructions().contains(i6));
    assertTrue(block8.getInstructions().contains(i7));

    assertTrue(block2.getPredecessors().contains(block1));
    assertTrue(block2.getPredecessors().contains(block8));
    assertTrue(block3.getPredecessors().contains(block2));
    assertTrue(block4.getPredecessors().contains(block3));
    assertTrue(block5.getPredecessors().contains(block3));
    assertTrue(block6.getPredecessors().contains(block4));
    assertTrue(block6.getPredecessors().contains(block5));
    assertTrue(block7.getPredecessors().contains(block2));
    assertTrue(block8.getPredecessors().contains(block6));
    assertTrue(block8.getPredecessors().contains(block7));

    assertTrue(dom.isDominant(block1, block2));

    assertTrue(dom.isDominant(block2, block3));
    assertTrue(dom.isDominant(block2, block7));
    assertTrue(dom.isDominant(block2, block8));

    assertTrue(dom.isDominant(block3, block4));
    assertTrue(dom.isDominant(block3, block5));
    assertTrue(dom.isDominant(block3, block6));

    assertFalse(dom.isDominant(block3, block7));
    assertFalse(dom.isDominant(block7, block3));
    assertFalse(dom.isDominant(block3, block8));
    assertFalse(dom.isDominant(block8, block3));
    assertFalse(dom.isDominant(block7, block8));
    assertFalse(dom.isDominant(block8, block7));

    assertFalse(dom.isDominant(block4, block5));
    assertFalse(dom.isDominant(block5, block4));
    assertFalse(dom.isDominant(block4, block6));
    assertFalse(dom.isDominant(block6, block4));
    assertFalse(dom.isDominant(block5, block6));
    assertFalse(dom.isDominant(block6, block5));

    assertTrue(dom.getDominanceFrontier(block1).isEmpty());
    assertEquals(asSet(block2), dom.getDominanceFrontier(block2));
    assertEquals(asSet(block8), dom.getDominanceFrontier(block3));
    assertEquals(asSet(block6), dom.getDominanceFrontier(block4));
    assertEquals(asSet(block6), dom.getDominanceFrontier(block5));
    assertEquals(asSet(block8), dom.getDominanceFrontier(block6));
    assertEquals(asSet(block8), dom.getDominanceFrontier(block7));
    assertEquals(asSet(block2), dom.getDominanceFrontier(block8));
  }

  private Set<CfgBasicBlock> asSet(CfgBasicBlock ... blocks) {
    return new HashSet<CfgBasicBlock>(Arrays.asList(blocks));
  }
}
