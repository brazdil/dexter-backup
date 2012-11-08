package uk.ac.cam.db538.dexter.analysis.cfg;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import lombok.val;

import org.junit.Test;

import uk.ac.cam.db538.dexter.dex.DexParsingCache;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexLabel;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_BinaryOp;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_IfTest;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ReturnVoid;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_BinaryOp;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_IfTest;
import uk.ac.cam.db538.dexter.dex.code.reg.DexRegister;

public class ControlFlowGraphTest {

  @Test
  public void testBlockRecognition_Empty() {
    val code = new DexCode(new DexParsingCache());

    val cfg = new ControlFlowGraph(code);
    val start = cfg.getStartBlock();
    val exit = cfg.getExitBlock();

    assertEquals(1, start.getSuccessors().size());
    assertEquals(1, exit.getPredecessors().size());

    assertEquals(exit, start.getSuccessors().toArray()[0]);
    assertEquals(start, exit.getPredecessors().toArray()[0]);
  }

  @Test
  public void testBlockRecognition_SingleInsn() {
    val code = new DexCode(new DexParsingCache());
    val insnReturn = new DexInstruction_ReturnVoid(code);
    code.add(insnReturn);

    val cfg = new ControlFlowGraph(code);
    val start = cfg.getStartBlock();
    val exit = cfg.getExitBlock();

    // find successor of START
    assertEquals(1, start.getSuccessors().size());
    val succ = start.getSuccessors().toArray()[0];
    assertTrue(succ instanceof BasicBlock);

    // inspect block
    val block = (BasicBlock) succ;
    val insns = block.getInstructions();
    assertEquals(1, insns.size());
    assertEquals(insnReturn, insns.get(0));

    // check it points to EXIT
    assertEquals(1, block.getSuccessors().size());
    assertEquals(exit, block.getSuccessors().toArray()[0]);
  }

  @Test
  public void testBlockRecognition_MoreBlocks() {
    val code = new DexCode(new DexParsingCache());
    val r0 = new DexRegister(0);
    val r1 = new DexRegister(1);
    val r2 = new DexRegister(2);

    val i0 = new DexLabel(code, 0L);
    code.add(i0);
    val i1 = new DexInstruction_BinaryOp(code, r0, r1, r2, Opcode_BinaryOp.AddInt);
    code.add(i1);
    val i2 = new DexLabel(code, 0L);
    code.add(i2);
    val i3 = new DexInstruction_ReturnVoid(code);
    code.add(i3);
    val i4 = new DexInstruction_IfTest(code, r0, r1, i0, Opcode_IfTest.eq);
    code.add(i4);
    val i5 = new DexInstruction_BinaryOp(code, r0, r1, r2, Opcode_BinaryOp.AddInt);
    code.add(i5);
    val i6 = new DexInstruction_IfTest(code, r0, r1, i2, Opcode_IfTest.eq);
    code.add(i6);
    val i7 = new DexInstruction_BinaryOp(code, r0, r1, r2, Opcode_BinaryOp.AddInt);
    code.add(i7);

    val cfg = new ControlFlowGraph(code);
    val start = cfg.getStartBlock();
    val exit = cfg.getExitBlock();

    assertEquals(5, cfg.getBasicBlocks().size());
    val b1 = cfg.getBasicBlocks().get(0);
    val b1Insns = b1.getInstructions();
    val b2 = cfg.getBasicBlocks().get(1);
    val b2Insns = b2.getInstructions();
    val b3 = cfg.getBasicBlocks().get(2);
    val b3Insns = b3.getInstructions();
    val b4 = cfg.getBasicBlocks().get(3);
    val b4Insns = b4.getInstructions();
    val b5 = cfg.getBasicBlocks().get(4);
    val b5Insns = b5.getInstructions();

    // find successor of START
    // check that it contains i0, i1
    assertEquals(1, start.getSuccessors().size());
    assertTrue(start.getSuccessors().contains(b1));
    assertEquals(2, b1Insns.size());
    assertEquals(i0, b1Insns.get(0));
    assertEquals(i1, b1Insns.get(1));
    assertEquals(1, b1.getSuccessors().size());
    assertTrue(b1.getSuccessors().contains(b2));

    // second block directly after the first one
    assertEquals(2, b2Insns.size());
    assertEquals(i2, b2Insns.get(0));
    assertEquals(i3, b2Insns.get(1));
    assertEquals(1, b2.getSuccessors().size());
    assertTrue(b2.getSuccessors().contains(exit));

    // third block doesn't have a predecessor
    assertEquals(0, b3.getPredecessors().size());
    assertEquals(1, b3Insns.size());
    assertEquals(i4, b3Insns.get(0));
    assertEquals(2, b3.getSuccessors().size());
    assertTrue(b3.getSuccessors().contains(b4));
    assertTrue(b3.getSuccessors().contains(b1));

    // fourth block is pretty normal
    assertEquals(1, b4.getPredecessors().size());
    assertEquals(2, b4Insns.size());
    assertEquals(i5, b4Insns.get(0));
    assertEquals(i6, b4Insns.get(1));
    assertEquals(2, b4.getSuccessors().size());
    assertTrue(b4.getSuccessors().contains(b5));
    assertTrue(b4.getSuccessors().contains(b2));

    // fifth block is connected to EXIT
    assertEquals(1, b5.getPredecessors().size());
    assertEquals(1, b5Insns.size());
    assertEquals(i7, b5Insns.get(0));
    assertEquals(1, b5.getSuccessors().size());
    assertTrue(b5.getSuccessors().contains(exit));
  }
}
