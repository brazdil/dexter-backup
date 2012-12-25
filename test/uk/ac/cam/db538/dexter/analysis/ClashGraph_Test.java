package uk.ac.cam.db538.dexter.analysis;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import lombok.val;

import org.junit.Test;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_BinaryOp;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Const;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_BinaryOp;

public class ClashGraph_Test {

  @Test
  public void testNoClashes() {
    val code = new DexCode();

    val r0 = new DexRegister(0);
    val r1 = new DexRegister(1);

    val i0 = new DexInstruction_Const(code, r0, 1);
    code.add(i0);
    val i1 = new DexInstruction_Const(code, r1, 2);
    code.add(i1);

    val clashGraph = new ClashGraph(code);
    assertTrue(clashGraph.noEdgesLeft());
  }

  @Test
  public void testSingleClash() {
    val code = new DexCode();

    val r0 = new DexRegister(0);
    val r1 = new DexRegister(1);
    val r2 = new DexRegister(2);

    val i0 = new DexInstruction_Const(code, r0, 1);
    code.add(i0);
    val i1 = new DexInstruction_Const(code, r1, 2);
    code.add(i1);
    val i2 = new DexInstruction_BinaryOp(code, r2, r0, r1, Opcode_BinaryOp.AddInt);
    code.add(i2);

    val clashGraph = new ClashGraph(code);
    assertTrue(clashGraph.areClashing(r0, r1));
    assertFalse(clashGraph.areClashing(r0, r2));
    assertFalse(clashGraph.areClashing(r1, r2));
  }

  @Test
  public void testUndirectedEdges() {
    val code = new DexCode();

    val r0 = new DexRegister(0);
    val r1 = new DexRegister(1);
    val r2 = new DexRegister(2);

    val i0 = new DexInstruction_Const(code, r0, 1);
    code.add(i0);
    val i1 = new DexInstruction_Const(code, r1, 2);
    code.add(i1);
    val i2 = new DexInstruction_BinaryOp(code, r2, r0, r1, Opcode_BinaryOp.AddInt);
    code.add(i2);

    val clashGraph = new ClashGraph(code);
    assertTrue(clashGraph.areClashing(r0, r1));
    assertTrue(clashGraph.areClashing(r1, r0));
  }

  @Test
  public void testRemoveLowestDegreeNode() {
    val code = new DexCode();

    val r0 = new DexRegister(0);
    val r1 = new DexRegister(1);
    val r2 = new DexRegister(2);

    val i0 = new DexInstruction_Const(code, r0, 1);
    code.add(i0);
    val i1 = new DexInstruction_Const(code, r1, 2);
    code.add(i1);
    val i2 = new DexInstruction_BinaryOp(code, r2, r0, r1, Opcode_BinaryOp.AddInt);
    code.add(i2);
    val i3 = new DexInstruction_BinaryOp(code, r2, r0, r2, Opcode_BinaryOp.AddInt);
    code.add(i3);

    val clashGraph = new ClashGraph(code);
    val lr1 = clashGraph.removeLowestDegreeNode();
    val lr2 = clashGraph.removeLowestDegreeNode();
    val lr3 = clashGraph.removeLowestDegreeNode();
    assertTrue(clashGraph.noVerticesLeft());
    assertNull(clashGraph.removeLowestDegreeNode());

    assertTrue(r1.equals(lr1) || r2.equals(lr1));
    assertTrue(r1.equals(lr2) || r2.equals(lr2));
    assertFalse(lr1.equals(lr2));
    assertTrue(r0.equals(lr3));
  }

  @Test
  public void testUnusedCodeBug() {
    val code = new DexCode();

    val r0 = new DexRegister(0);
    val r1 = new DexRegister(1);
    val r2 = new DexRegister(2);
    val r3 = new DexRegister(3);

    val i0 = new DexInstruction_Const(code, r0, 1);
    code.add(i0);
    val i1 = new DexInstruction_Const(code, r1, 2);
    code.add(i1);
    val i2 = new DexInstruction_Const(code, r3, 6);
    code.add(i2);
    val i3 = new DexInstruction_BinaryOp(code, r2, r0, r1, Opcode_BinaryOp.AddInt);
    code.add(i3);
    val i4 = new DexInstruction_BinaryOp(code, r2, r0, r2, Opcode_BinaryOp.AddInt);
    code.add(i4);

    val clashGraph = new ClashGraph(code);

    // r3 cannot be assigned to either of r0 and r1
    assertTrue(clashGraph.areClashing(r3, r0));
    assertTrue(clashGraph.areClashing(r3, r1));
  }
}
