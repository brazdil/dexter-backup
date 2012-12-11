package uk.ac.cam.db538.dexter.dex.code;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.NoSuchElementException;

import lombok.val;

import org.jf.dexlib.CodeItem;
import org.jf.dexlib.CodeItem.EncodedCatchHandler;
import org.jf.dexlib.CodeItem.EncodedTypeAddrPair;
import org.jf.dexlib.CodeItem.TryItem;
import org.jf.dexlib.DexFile;
import org.jf.dexlib.TypeIdItem;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction10t;
import org.jf.dexlib.Code.Format.Instruction10x;
import org.junit.Test;

import uk.ac.cam.db538.dexter.analysis.coloring.NodeRun;
import uk.ac.cam.db538.dexter.dex.DexAssemblingCache;
import uk.ac.cam.db538.dexter.dex.DexParsingCache;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_BinaryOp;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_BinaryOpWide;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_IfTestZero;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Nop;
import uk.ac.cam.db538.dexter.dex.code.insn.InstructionParsingException;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_BinaryOp;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_BinaryOpWide;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_IfTestZero;

public class DexCodeTest {

  @Test
  public void testInsertBefore_Middle() {
    val code = new DexCode();

    val elem1 = new DexLabel(code, 1);
    val elem2 = new DexLabel(code, 2);
    val elem3 = new DexLabel(code, 3);

    code.add(elem1);
    code.add(elem3);
    code.insertBefore(elem2, elem3);

    val insnList = code.getInstructionList();
    assertEquals(elem1, insnList.get(0));
    assertEquals(elem2, insnList.get(1));
    assertEquals(elem3, insnList.get(2));
  }

  @Test
  public void testInsertBefore_First() {
    val code = new DexCode();

    val elem1 = new DexLabel(code, 1);
    val elem2 = new DexLabel(code, 2);
    val elem3 = new DexLabel(code, 3);

    code.add(elem2);
    code.add(elem3);
    code.insertBefore(elem1, elem2);

    val insnList = code.getInstructionList();
    assertEquals(elem1, insnList.get(0));
    assertEquals(elem2, insnList.get(1));
    assertEquals(elem3, insnList.get(2));
  }

  @Test(expected=NoSuchElementException.class)
  public void testInsertBefore_NotFound() {
    val code = new DexCode();

    val elem1 = new DexLabel(code, 1);
    val elem2 = new DexLabel(code, 2);
    val elem3 = new DexLabel(code, 3);

    code.add(elem3);
    code.insertBefore(elem1, elem2);
  }

  @Test
  public void testInsertAfter_Middle() {
    val code = new DexCode();

    val elem1 = new DexLabel(code, 1);
    val elem2 = new DexLabel(code, 2);
    val elem3 = new DexLabel(code, 3);

    code.add(elem1);
    code.add(elem3);
    code.insertAfter(elem2, elem1);

    val insnList = code.getInstructionList();
    assertEquals(elem1, insnList.get(0));
    assertEquals(elem2, insnList.get(1));
    assertEquals(elem3, insnList.get(2));
  }

  @Test
  public void testInsertBefore_Last() {
    val code = new DexCode();

    val elem1 = new DexLabel(code, 1);
    val elem2 = new DexLabel(code, 2);
    val elem3 = new DexLabel(code, 3);

    code.add(elem1);
    code.add(elem2);
    code.insertAfter(elem3, elem2);

    val insnList = code.getInstructionList();
    assertEquals(elem1, insnList.get(0));
    assertEquals(elem2, insnList.get(1));
    assertEquals(elem3, insnList.get(2));
  }

  @Test(expected=NoSuchElementException.class)
  public void testInsertAfter_NotFound() {
    val code = new DexCode();

    val elem1 = new DexLabel(code, 1);
    val elem2 = new DexLabel(code, 2);
    val elem3 = new DexLabel(code, 3);

    code.add(elem3);
    code.insertAfter(elem1, elem2);
  }

  private static void addFollowConstraint(DexRegister r1, DexRegister r2, DexCode code) {
    code.add(new DexInstruction_BinaryOpWide(code, r1, r2, r1, r2, r1, r2, Opcode_BinaryOpWide.AddLong));
  }

  @Test
  public void testGetFollowConstraints_Empty() {
    val code = new DexCode();
    val constraints = code.getFollowRuns();
    assertTrue(constraints.isEmpty());
  }

  @Test
  public void testGetFollowConstraints_NoConstraints() {
    val code = new DexCode();

    val r1 = new DexRegister(1);
    val r2 = new DexRegister(2);
    val r3 = new DexRegister(3);

    code.add(new DexInstruction_BinaryOp(code, r1, r2, r3, Opcode_BinaryOp.AddInt));

    val constraints = code.getFollowRuns();
    assertEquals(3, constraints.values().size());

    val run1 = new NodeRun();
    run1.add(r1);
    val run2 = new NodeRun();
    run2.add(r2);
    val run3 = new NodeRun();
    run3.add(r3);

    assertTrue(constraints.values().contains(run1));
    assertTrue(constraints.values().contains(run2));
    assertTrue(constraints.values().contains(run3));
  }

  @Test
  public void testGetFollowConstraints_SingleConstraint() {
    val code = new DexCode();

    val r1 = new DexRegister(1);
    val r2 = new DexRegister(2);
    val r3 = new DexRegister(3);

    code.add(new DexInstruction_BinaryOp(code, r1, r2, r3, Opcode_BinaryOp.AddInt));
    addFollowConstraint(r1, r2, code);

    val constraints = code.getFollowRuns();

    val run1 = new NodeRun();
    run1.add(r1);
    run1.add(r2);
    val run2 = new NodeRun();
    run2.add(r3);

    assertEquals(run1, constraints.get(r1));
    assertEquals(run1, constraints.get(r2));
    assertEquals(run2, constraints.get(r3));
  }

  @Test
  public void testGetFollowConstraints_SingleConstraintMultipleTimes() {
    val code = new DexCode();

    val r1 = new DexRegister(1);
    val r2 = new DexRegister(2);
    val r3 = new DexRegister(3);

    code.add(new DexInstruction_BinaryOp(code, r1, r2, r3, Opcode_BinaryOp.AddInt));
    addFollowConstraint(r1, r2, code);
    addFollowConstraint(r1, r2, code);

    val constraints = code.getFollowRuns();

    val run1 = new NodeRun();
    run1.add(r1);
    run1.add(r2);
    val run2 = new NodeRun();
    run2.add(r3);

    assertEquals(run1, constraints.get(r1));
    assertEquals(run1, constraints.get(r2));
    assertEquals(run2, constraints.get(r3));
  }

  @Test
  public void testGetFollowConstraints_ChainingConstraints() {
    val code = new DexCode();

    val r1 = new DexRegister(1);
    val r2 = new DexRegister(2);
    val r3 = new DexRegister(3);
    val r4 = new DexRegister(4);

    addFollowConstraint(r1, r2, code);
    addFollowConstraint(r2, r3, code);
    addFollowConstraint(r3, r4, code);

    val constraints = code.getFollowRuns();

    val run1 = new NodeRun();
    run1.add(r1);
    run1.add(r2);
    run1.add(r3);
    run1.add(r4);

    assertEquals(run1, constraints.get(r1));
    assertEquals(run1, constraints.get(r2));
    assertEquals(run1, constraints.get(r3));
    assertEquals(run1, constraints.get(r4));
  }

  @Test(expected=RuntimeException.class)
  public void testGetFollowConstraints_Inconsistency_ClashingConstraints() {
    val code = new DexCode();

    val r1 = new DexRegister(1);
    val r2 = new DexRegister(2);
    val r3 = new DexRegister(3);

    addFollowConstraint(r1, r2, code);
    addFollowConstraint(r1, r3, code);

    code.getFollowRuns();
  }

  @Test(expected=RuntimeException.class)
  public void testGetFollowConstraints_Inconsistency_ClashingConstraints_OppositeDirection() {
    val code = new DexCode();

    val r1 = new DexRegister(1);
    val r2 = new DexRegister(2);
    val r3 = new DexRegister(3);

    addFollowConstraint(r1, r3, code);
    addFollowConstraint(r2, r3, code);

    code.getFollowRuns();
  }

  @Test(expected=RuntimeException.class)
  public void testGetFollowConstraints_Inconsistency_ClashingConstraints_WithinRun() {
    val code = new DexCode();

    val r1 = new DexRegister(1);
    val r2 = new DexRegister(2);
    val r3 = new DexRegister(3);
    val r4 = new DexRegister(4);

    addFollowConstraint(r1, r2, code);
    addFollowConstraint(r2, r3, code);
    addFollowConstraint(r3, r4, code);
    addFollowConstraint(r1, r4, code);

    code.getFollowRuns();
  }

  @Test(expected=InstructionParsingException.class)
  public void testLabels_InvalidOffset_Positive() throws InstructionParsingException {
    Utils.parseAndCompare(
      new Instruction[] {
        new Instruction10t(Opcode.GOTO, 2),
        new Instruction10x(Opcode.NOP)
      }, null);
  }

  @Test(expected=InstructionParsingException.class)
  public void testLabels_InvalidOffset_Negative() throws InstructionParsingException {
    Utils.parseAndCompare(
      new Instruction[] {
        new Instruction10x(Opcode.NOP),
        new Instruction10t(Opcode.GOTO, -2)
      }, null);
  }

  @Test
  public void testAssembleBytecode_OffsetTooLong_Instrumentation() {
    val code = new DexCode();

    val regNum = Utils.numFitsInto_Unsigned(8);
    val reg = new DexRegister(regNum);

    val label = new DexLabel(code);
    val nop = new DexInstruction_Nop(code);
    val insn = new DexInstruction_IfTestZero(code, reg, label, Opcode_IfTestZero.eqz);

    val r1 = new DexRegister(1);
    val r2 = new DexRegister(2);
    val r3 = new DexRegister(3);

    code.add(insn);
    for (int i = 0; i < 32766 / 2; ++i) // size of BinOp is 2
      code.add(new DexInstruction_BinaryOp(code, r1, r2, r3, Opcode_BinaryOp.AddInt));
    code.add(label);
    code.add(nop);

    val regAlloc = Utils.genRegAlloc(reg, r1, r2, r3);
    val asm = code.assembleBytecode(regAlloc, new DexAssemblingCache(new DexFile()));
    assertEquals(3 + 32766/2 + 1, asm.size());
    assertEquals(Opcode.IF_EQZ, asm.get(0).opcode); // the original IfTestZero
    assertEquals(Opcode.GOTO, asm.get(1).opcode); // jump to successor
    assertEquals(Opcode.GOTO_32, asm.get(2).opcode); // long jump to branch
    assertEquals(Opcode.ADD_INT, asm.get(3).opcode); // original successor
  }

  @Test
  public void testParse_TryBlock_StartAtBeginning_EndInMiddle() {
    val handler1 = new EncodedCatchHandler(null, -1);
    val try1 = new TryItem(0, 1, handler1);

    val insns = Arrays.asList(new Instruction[] { new Instruction10x(Opcode.NOP), new Instruction10x(Opcode.NOP) });
    val tries = Arrays.asList(new TryItem[] { try1 });
    val handlers = Arrays.asList(new EncodedCatchHandler[] { handler1 });

    val codeItem = CodeItem.internCodeItem(new DexFile(), 1, 0, 0, null, insns, tries, handlers);
    val dexCode = new DexCode(codeItem, new DexParsingCache());

    assertTrue(dexCode.getInstructionList().get(0) instanceof DexTryBlockStart);
    assertTrue(dexCode.getInstructionList().get(1) instanceof DexInstruction_Nop);
    assertTrue(dexCode.getInstructionList().get(2) instanceof DexTryBlockEnd);
    assertTrue(dexCode.getInstructionList().get(3) instanceof DexInstruction_Nop);
  }

  @Test
  public void testParse_TryBlock_StartInMiddle_EndAtEnd() {
    val handler1 = new EncodedCatchHandler(null, -1);
    val try1 = new TryItem(1, 1, handler1);

    val insns = Arrays.asList(new Instruction[] { new Instruction10x(Opcode.NOP), new Instruction10x(Opcode.NOP) });
    val tries = Arrays.asList(new TryItem[] { try1 });
    val handlers = Arrays.asList(new EncodedCatchHandler[] { handler1 });

    val codeItem = CodeItem.internCodeItem(new DexFile(), 1, 0, 0, null, insns, tries, handlers);
    val dexCode = new DexCode(codeItem, new DexParsingCache());

    assertTrue(dexCode.getInstructionList().get(0) instanceof DexInstruction_Nop);
    assertTrue(dexCode.getInstructionList().get(1) instanceof DexTryBlockStart);
    assertTrue(dexCode.getInstructionList().get(2) instanceof DexInstruction_Nop);
    assertTrue(dexCode.getInstructionList().get(3) instanceof DexTryBlockEnd);
  }

  @Test(expected=InstructionParsingException.class)
  public void testParse_TryBlock_StartOffsetError() {
    val handler1 = new EncodedCatchHandler(null, -1);
    val try1 = new TryItem(2, 1, handler1);

    val insns = Arrays.asList(new Instruction[] { new Instruction10x(Opcode.NOP), new Instruction10x(Opcode.NOP) });
    val tries = Arrays.asList(new TryItem[] { try1 });
    val handlers = Arrays.asList(new EncodedCatchHandler[] { handler1 });

    val codeItem = CodeItem.internCodeItem(new DexFile(), 1, 0, 0, null, insns, tries, handlers);
    new DexCode(codeItem, new DexParsingCache());
  }

  @Test(expected=InstructionParsingException.class)
  public void testParse_TryBlock_EndOffsetError() {
    val handler1 = new EncodedCatchHandler(null, -1);
    val try1 = new TryItem(0, 3, handler1);

    val insns = Arrays.asList(new Instruction[] { new Instruction10x(Opcode.NOP), new Instruction10x(Opcode.NOP) });
    val tries = Arrays.asList(new TryItem[] { try1 });
    val handlers = Arrays.asList(new EncodedCatchHandler[] { handler1 });

    val codeItem = CodeItem.internCodeItem(new DexFile(), 1, 0, 0, null, insns, tries, handlers);
    new DexCode(codeItem, new DexParsingCache());
  }

  @Test
  public void testParse_CatchBlock() {
    val dexFile = new DexFile();

    val exceptionType = TypeIdItem.internTypeIdItem(dexFile, "Lcom/example/MyException;");

    val handler1 = new EncodedCatchHandler(new EncodedTypeAddrPair[] { new EncodedTypeAddrPair(exceptionType, 1) }, -1);
    val try1 = new TryItem(0, 1, handler1);

    val insns = Arrays.asList(new Instruction[] { new Instruction10x(Opcode.NOP), new Instruction10x(Opcode.NOP), new Instruction10x(Opcode.NOP) });
    val tries = Arrays.asList(new TryItem[] { try1 });
    val handlers = Arrays.asList(new EncodedCatchHandler[] { handler1 });

    val codeItem = CodeItem.internCodeItem(dexFile, 1, 0, 0, null, insns, tries, handlers);
    val dexCode = new DexCode(codeItem, new DexParsingCache());

    assertTrue(dexCode.getInstructionList().get(0) instanceof DexTryBlockStart);
    assertTrue(dexCode.getInstructionList().get(1) instanceof DexInstruction_Nop);
    assertTrue(dexCode.getInstructionList().get(2) instanceof DexTryBlockEnd);
    assertTrue(dexCode.getInstructionList().get(3) instanceof DexCatch);
    assertTrue(dexCode.getInstructionList().get(4) instanceof DexInstruction_Nop);
    assertTrue(dexCode.getInstructionList().get(5) instanceof DexInstruction_Nop);

    val catchElem = (DexCatch) dexCode.getInstructionList().get(3);
    assertEquals("Lcom/example/MyException;", catchElem.getExceptionType().getDescriptor());

    val tryStartElem = (DexTryBlockStart) dexCode.getInstructionList().get(0);
    assertEquals(null, tryStartElem.getCatchAllHandler());
    assertEquals(1, tryStartElem.getCatchHandlers().size());
    assertTrue(tryStartElem.getCatchHandlers().contains(catchElem));
  }

  @Test
  public void testParse_CatchBlock_Multiple() {
    val dexFile = new DexFile();

    val exceptionType = TypeIdItem.internTypeIdItem(dexFile, "Lcom/example/MyException;");

    val handler1 = new EncodedCatchHandler(new EncodedTypeAddrPair[] { new EncodedTypeAddrPair(exceptionType, 1) }, -1);
    val handler2 = new EncodedCatchHandler(new EncodedTypeAddrPair[] { new EncodedTypeAddrPair(exceptionType, 1) }, -1);
    val try1 = new TryItem(0, 1, handler1);

    val insns = Arrays.asList(new Instruction[] { new Instruction10x(Opcode.NOP), new Instruction10x(Opcode.NOP), new Instruction10x(Opcode.NOP) });
    val tries = Arrays.asList(new TryItem[] { try1 });
    val handlers = Arrays.asList(new EncodedCatchHandler[] { handler1, handler2 });

    val codeItem = CodeItem.internCodeItem(dexFile, 1, 0, 0, null, insns, tries, handlers);
    val dexCode = new DexCode(codeItem, new DexParsingCache());

    assertTrue(dexCode.getInstructionList().get(0) instanceof DexTryBlockStart);
    assertTrue(dexCode.getInstructionList().get(1) instanceof DexInstruction_Nop);
    assertTrue(dexCode.getInstructionList().get(2) instanceof DexTryBlockEnd);
    assertTrue(dexCode.getInstructionList().get(3) instanceof DexCatch);
    assertTrue(dexCode.getInstructionList().get(4) instanceof DexInstruction_Nop);
    assertTrue(dexCode.getInstructionList().get(5) instanceof DexInstruction_Nop);
  }

  @Test(expected=InstructionParsingException.class)
  public void testParse_CatchBlock_WrongOffset() {
    val dexFile = new DexFile();

    val exceptionType = TypeIdItem.internTypeIdItem(dexFile, "Lcom/example/MyException;");

    val handler1 = new EncodedCatchHandler(new EncodedTypeAddrPair[] { new EncodedTypeAddrPair(exceptionType, 3) }, -1);
    val try1 = new TryItem(0, 1, handler1);

    val insns = Arrays.asList(new Instruction[] { new Instruction10x(Opcode.NOP), new Instruction10x(Opcode.NOP), new Instruction10x(Opcode.NOP) });
    val tries = Arrays.asList(new TryItem[] { try1 });
    val handlers = Arrays.asList(new EncodedCatchHandler[] { handler1 });

    val codeItem = CodeItem.internCodeItem(dexFile, 1, 0, 0, null, insns, tries, handlers);
    new DexCode(codeItem, new DexParsingCache());
  }

  @Test(expected=InstructionParsingException.class)
  public void testParse_CatchBlockNotMentionedInCodeItem() {
    val dexFile = new DexFile();

    val exceptionType = TypeIdItem.internTypeIdItem(dexFile, "Lcom/example/MyException;");

    val handler1 = new EncodedCatchHandler(new EncodedTypeAddrPair[] { new EncodedTypeAddrPair(exceptionType, 1) }, -1);
    val try1 = new TryItem(0, 1, handler1);

    val insns = Arrays.asList(new Instruction[] { new Instruction10x(Opcode.NOP), new Instruction10x(Opcode.NOP), new Instruction10x(Opcode.NOP) });
    val tries = Arrays.asList(new TryItem[] { try1 });
    val handlers = Arrays.asList(new EncodedCatchHandler[] { });

    val codeItem = CodeItem.internCodeItem(dexFile, 1, 0, 0, null, insns, tries, handlers);
    new DexCode(codeItem, new DexParsingCache());
  }

  @Test
  public void testParse_CatchAllBlock() {
    val dexFile = new DexFile();

    val handler1 = new EncodedCatchHandler(null, 1);
    val try1 = new TryItem(0, 1, handler1);

    val insns = Arrays.asList(new Instruction[] { new Instruction10x(Opcode.NOP), new Instruction10x(Opcode.NOP), new Instruction10x(Opcode.NOP) });
    val tries = Arrays.asList(new TryItem[] { try1 });
    val handlers = Arrays.asList(new EncodedCatchHandler[] { handler1 });

    val codeItem = CodeItem.internCodeItem(dexFile, 1, 0, 0, null, insns, tries, handlers);
    val dexCode = new DexCode(codeItem, new DexParsingCache());

    assertTrue(dexCode.getInstructionList().get(0) instanceof DexTryBlockStart);
    assertTrue(dexCode.getInstructionList().get(1) instanceof DexInstruction_Nop);
    assertTrue(dexCode.getInstructionList().get(2) instanceof DexTryBlockEnd);
    assertTrue(dexCode.getInstructionList().get(3) instanceof DexCatchAll);
    assertTrue(dexCode.getInstructionList().get(4) instanceof DexInstruction_Nop);
    assertTrue(dexCode.getInstructionList().get(5) instanceof DexInstruction_Nop);

    val catchElem = (DexCatchAll) dexCode.getInstructionList().get(3);
    val tryStartElem = (DexTryBlockStart) dexCode.getInstructionList().get(0);
    assertEquals(catchElem, tryStartElem.getCatchAllHandler());
    assertEquals(0, tryStartElem.getCatchHandlers().size());
  }

  @Test
  public void testParse_CatchAllBlock_Multiple() {
    val dexFile = new DexFile();

    val handler1 = new EncodedCatchHandler(null, 1);
    val handler2 = new EncodedCatchHandler(null, 1);
    val try1 = new TryItem(0, 1, handler1);

    val insns = Arrays.asList(new Instruction[] { new Instruction10x(Opcode.NOP), new Instruction10x(Opcode.NOP), new Instruction10x(Opcode.NOP) });
    val tries = Arrays.asList(new TryItem[] { try1 });
    val handlers = Arrays.asList(new EncodedCatchHandler[] { handler1, handler2 });

    val codeItem = CodeItem.internCodeItem(dexFile, 1, 0, 0, null, insns, tries, handlers);
    val dexCode = new DexCode(codeItem, new DexParsingCache());

    assertTrue(dexCode.getInstructionList().get(0) instanceof DexTryBlockStart);
    assertTrue(dexCode.getInstructionList().get(1) instanceof DexInstruction_Nop);
    assertTrue(dexCode.getInstructionList().get(2) instanceof DexTryBlockEnd);
    assertTrue(dexCode.getInstructionList().get(3) instanceof DexCatchAll);
    assertTrue(dexCode.getInstructionList().get(4) instanceof DexInstruction_Nop);
    assertTrue(dexCode.getInstructionList().get(5) instanceof DexInstruction_Nop);
  }

  @Test(expected=InstructionParsingException.class)
  public void testParse_CatchAllBlock_WrongOffset() {
    val dexFile = new DexFile();

    val handler1 = new EncodedCatchHandler(null, 3);
    val try1 = new TryItem(0, 1, handler1);

    val insns = Arrays.asList(new Instruction[] { new Instruction10x(Opcode.NOP), new Instruction10x(Opcode.NOP), new Instruction10x(Opcode.NOP) });
    val tries = Arrays.asList(new TryItem[] { try1 });
    val handlers = Arrays.asList(new EncodedCatchHandler[] { handler1 });

    val codeItem = CodeItem.internCodeItem(dexFile, 1, 0, 0, null, insns, tries, handlers);
    new DexCode(codeItem, new DexParsingCache());
  }

  @Test(expected=InstructionParsingException.class)
  public void testParse_CatchAllBlockNotMentionedInCodeItem() {
    val dexFile = new DexFile();

    val handler1 = new EncodedCatchHandler(null, 1);
    val try1 = new TryItem(0, 1, handler1);

    val insns = Arrays.asList(new Instruction[] { new Instruction10x(Opcode.NOP), new Instruction10x(Opcode.NOP), new Instruction10x(Opcode.NOP) });
    val tries = Arrays.asList(new TryItem[] { try1 });
    val handlers = Arrays.asList(new EncodedCatchHandler[] { });

    val codeItem = CodeItem.internCodeItem(dexFile, 1, 0, 0, null, insns, tries, handlers);
    new DexCode(codeItem, new DexParsingCache());
  }
}
