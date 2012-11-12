package uk.ac.cam.db538.dexter.analysis.coloring;

import lombok.val;

import org.junit.Test;
import static org.junit.Assert.*;

import uk.ac.cam.db538.dexter.dex.DexParsingCache;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_BinaryOp;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_InstanceOf;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ReturnVoid;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_BinaryOp;
import uk.ac.cam.db538.dexter.dex.code.reg.DexRegister;
import uk.ac.cam.db538.dexter.dex.type.DexReferenceType;

public class GraphColoring_ActualSituationsTest {

  private static void genClash(DexCode code, DexRegister reg1, DexRegister reg2) {
    code.add(new DexInstruction_BinaryOp(code, reg1, reg1, reg2, Opcode_BinaryOp.AddInt));
    code.add(new DexInstruction_ReturnVoid(code));
  }

  private static void gen4bitRangeConstraint(DexCode code, DexRegister reg, DexParsingCache cache) {
    code.add(new DexInstruction_InstanceOf(code, reg, reg, DexReferenceType.parse("Ljava.lang.String;", cache)));
    code.add(new DexInstruction_ReturnVoid(code));
  }

  @Test
  public void testNoSpilling() {
    val code = new DexCode(new DexParsingCache());

    val rA = new DexRegister(0);
    val rB = new DexRegister(1);
    val rC = new DexRegister(2);
    val rD = new DexRegister(3);
    val rW = new DexRegister(4);
    val rX = new DexRegister(5);
    val rY = new DexRegister(6);
    val rZ = new DexRegister(7);

    // clash A, B, C, D with each other
    genClash(code, rA, rB);
    genClash(code, rA, rC);
    genClash(code, rA, rD);
    genClash(code, rB, rC);
    genClash(code, rB, rD);
    genClash(code, rC, rD);

    // clash W with B, D
    genClash(code, rW, rB);
    genClash(code, rW, rD);

    // clash Y with W, X, Z
    genClash(code, rY, rW);
    genClash(code, rY, rX);
    genClash(code, rY, rZ);

    // generate coloring
    val coloring = new GraphColoring(code);
    int cA = coloring.getColor(rA);
    int cB = coloring.getColor(rB);
    int cC = coloring.getColor(rC);
    int cD = coloring.getColor(rD);
    int cW = coloring.getColor(rW);
    int cX = coloring.getColor(rX);
    int cY = coloring.getColor(rY);
    int cZ = coloring.getColor(rZ);

    // check that the code hasn't changed
    assertEquals(coloring.getCode(), coloring.getModifiedCode());

    // check that there are no clashes in the coloring
    assertTrue(cA != cB);
    assertTrue(cA != cC);
    assertTrue(cA != cD);
    assertTrue(cB != cC);
    assertTrue(cB != cD);
    assertTrue(cC != cD);

    assertTrue(cW != cB);
    assertTrue(cW != cD);

    assertTrue(cY != cW);
    assertTrue(cY != cX);
    assertTrue(cY != cZ);
  }

  private static int SpillingRegCount = 22;
  private static int SpillingRegCount_ExpectedSpilledRegs = SpillingRegCount - 16;

  @Test
  public void testNeedsSpilling() {
    val cache = new DexParsingCache();
    val code = new DexCode(cache);

    // generate registers
    val regs = new DexRegister[SpillingRegCount];
    for (int i = 0; i < SpillingRegCount; ++i)
      regs[i] = new DexRegister(i);

    // clash them all
    for (int i = 0; i < SpillingRegCount; ++i)
      for (int j = i + 1; j < SpillingRegCount; ++j)
        genClash(code, regs[i], regs[j]);

    // constraint them to 4 bits
    for (int i = 0; i < SpillingRegCount; ++i)
      gen4bitRangeConstraint(code, regs[i], cache);

    val coloring = new GraphColoring(code);
    val newCode = coloring.getModifiedCode();

    // should have increased the number of registers
    assertEquals((SpillingRegCount_ExpectedSpilledRegs + 1) * SpillingRegCount, newCode.getUsedRegisters().size());
    assertEquals(SpillingRegCount + 2, coloring.getNumberOfColorsUsed()); // needs two distinct temp registers

    // check that there are no clashes
    for (int i = 0; i < SpillingRegCount; ++i)
      for (int j = i + 1; j < SpillingRegCount; ++j)
        assertFalse(coloring.getColor(regs[i]) == coloring.getColor(regs[j]));
  }
}
