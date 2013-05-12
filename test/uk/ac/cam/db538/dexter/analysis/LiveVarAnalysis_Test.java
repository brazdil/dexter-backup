package uk.ac.cam.db538.dexter.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import lombok.val;

import org.junit.Test;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.elem.DexLabel;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_BinaryOp;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Const;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Goto;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_IfTest;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Move;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_BinaryOp;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_IfTest;

public class LiveVarAnalysis_Test {

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

    val lva = new LiveVarAnalysis(code);

    val expLva0 = lva.getLiveVarsBefore(i0);
    assertEquals(0, expLva0.size());

    val expLva1 = lva.getLiveVarsBefore(i1);
    assertEquals(1, expLva1.size());
    assertTrue(expLva1.contains(r0));

    val expLva2 = lva.getLiveVarsBefore(i2);
    assertEquals(2, expLva2.size());
    assertTrue(expLva2.contains(r0));
    assertTrue(expLva2.contains(r1));

    val expLva3 = lva.getLiveVarsBefore(i3);
    assertEquals(1, expLva3.size());
    assertTrue(expLva3.contains(r2));
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

    val lva = new LiveVarAnalysis(code);

    val expLva0 = lva.getLiveVarsBefore(i0);
    assertEquals(2, expLva0.size());
    assertTrue(expLva0.contains(r3));
    assertTrue(expLva0.contains(r4));

    val expLva1 = lva.getLiveVarsBefore(i1);
    assertEquals(3, expLva1.size());
    assertTrue(expLva1.contains(r0));
    assertTrue(expLva1.contains(r3));
    assertTrue(expLva1.contains(r4));

    val expLva2 = lva.getLiveVarsBefore(i2);
    assertEquals(4, expLva2.size());
    assertTrue(expLva2.contains(r0));
    assertTrue(expLva2.contains(r1));
    assertTrue(expLva2.contains(r3));
    assertTrue(expLva2.contains(r4));

    val expLva3 = lva.getLiveVarsBefore(i3);
    assertEquals(2, expLva3.size());
    assertTrue(expLva3.contains(r0));
    assertTrue(expLva3.contains(r1));

    val expLva4 = lva.getLiveVarsBefore(i4);
    assertEquals(0, expLva4.size());

    val expLva5 = lva.getLiveVarsBefore(i5);
    assertEquals(1, expLva5.size());
    assertTrue(expLva5.contains(r0));

    val expLva6 = lva.getLiveVarsBefore(i6);
    assertEquals(1, expLva6.size());
    assertTrue(expLva6.contains(r0));
  }
  
  private <T> Set<T> createSet(T ... elems) {
	  return new HashSet<T>(Arrays.asList(elems));
  }

  @Test
  public void testLoopingBlock() {
    val r0 = new DexRegister();
    val r1 = new DexRegister();
    val r2 = new DexRegister();
    val r3 = new DexRegister();

    val code = new DexCode();
    val i0 = new DexInstruction_Const(code, r0, 1);
    val i1 = new DexInstruction_Const(code, r1, 2);
    val label = new DexLabel(code);
    val i2 = new DexInstruction_Const(code, r2, 3);
    val i3 = new DexInstruction_BinaryOp(code, r3, r0, r0, Opcode_BinaryOp.AddInt);
    val i4 = new DexInstruction_IfTest(code, r1, r1, label, Opcode_IfTest.eq);
    val i5 = new DexInstruction_BinaryOp(code, r3, r2, r2, Opcode_BinaryOp.AddInt);
    
    code.addAll(Arrays.asList(i0, i1, label, i2, i3, i4, i5));
    val lva = new LiveVarAnalysis(code);

    assertEquals(Collections.emptySet(), lva.getLiveVarsBefore(i0));
    assertEquals(createSet(r0), lva.getLiveVarsBefore(i1));
    assertEquals(createSet(r0, r1), lva.getLiveVarsBefore(label));
    assertEquals(createSet(r0, r1), lva.getLiveVarsBefore(i2));
    assertEquals(createSet(r0, r1, r2), lva.getLiveVarsBefore(i3));
    assertEquals(createSet(r0, r1, r2), lva.getLiveVarsBefore(i4));
    assertEquals(createSet(r2), lva.getLiveVarsBefore(i5));
  }
}
