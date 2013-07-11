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

import uk.ac.cam.db538.dexter.dex.code.elem.DexCatch;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCatchAll;
import uk.ac.cam.db538.dexter.dex.code.elem.DexLabel;
import uk.ac.cam.db538.dexter.dex.code.elem.DexTryBlockEnd;
import uk.ac.cam.db538.dexter.dex.code.elem.DexTryBlockStart;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Nop;
import uk.ac.cam.db538.dexter.dex.code.insn.InstructionParseError;
import uk.ac.cam.db538.dexter.dex.type.DexTypeCache;

public class DexCode_Test {

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
    assertEquals(elem1, insnList.get(1));
    assertEquals(elem2, insnList.get(2));
    assertEquals(elem3, insnList.get(3));
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
    assertEquals(elem1, insnList.get(1));
    assertEquals(elem2, insnList.get(2));
    assertEquals(elem3, insnList.get(3));
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
    assertEquals(elem1, insnList.get(1));
    assertEquals(elem2, insnList.get(2));
    assertEquals(elem3, insnList.get(3));
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
    assertEquals(elem1, insnList.get(1));
    assertEquals(elem2, insnList.get(2));
    assertEquals(elem3, insnList.get(3));
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

  @Test(expected=InstructionParseError.class)
  public void testLabels_InvalidOffset_Positive() throws InstructionParseError {
    Utils.parseAndCompare(
      new Instruction[] {
        new Instruction10t(Opcode.GOTO, 2),
        new Instruction10x(Opcode.NOP)
      }, null);
  }

  @Test(expected=InstructionParseError.class)
  public void testLabels_InvalidOffset_Negative() throws InstructionParseError {
    Utils.parseAndCompare(
      new Instruction[] {
        new Instruction10x(Opcode.NOP),
        new Instruction10t(Opcode.GOTO, -2)
      }, null);
  }

  @Test
  public void testParse_TryBlock_StartAtBeginning_EndInMiddle() {
    val handler1 = new EncodedCatchHandler(null, -1);
    val try1 = new TryItem(0, 1, handler1);

    val insns = Arrays.asList(new Instruction[] { new Instruction10x(Opcode.NOP), new Instruction10x(Opcode.NOP) });
    val tries = Arrays.asList(new TryItem[] { try1 });
    val handlers = Arrays.asList(new EncodedCatchHandler[] { handler1 });

    val codeItem = CodeItem.internCodeItem(new DexFile(), 1, 0, 0, null, insns, tries, handlers);
    val dexCode = new DexCode(codeItem, new DexTypeCache());

    assertTrue(dexCode.getInstructionList().get(1) instanceof DexTryBlockStart);
    assertTrue(dexCode.getInstructionList().get(2) instanceof DexInstruction_Nop);
    assertTrue(dexCode.getInstructionList().get(3) instanceof DexTryBlockEnd);
    assertTrue(dexCode.getInstructionList().get(4) instanceof DexInstruction_Nop);
  }

  @Test
  public void testParse_TryBlock_StartInMiddle_EndAtEnd() {
    val handler1 = new EncodedCatchHandler(null, -1);
    val try1 = new TryItem(1, 1, handler1);

    val insns = Arrays.asList(new Instruction[] { new Instruction10x(Opcode.NOP), new Instruction10x(Opcode.NOP) });
    val tries = Arrays.asList(new TryItem[] { try1 });
    val handlers = Arrays.asList(new EncodedCatchHandler[] { handler1 });

    val codeItem = CodeItem.internCodeItem(new DexFile(), 1, 0, 0, null, insns, tries, handlers);
    val dexCode = new DexCode(codeItem, new DexTypeCache());

    assertTrue(dexCode.getInstructionList().get(1) instanceof DexInstruction_Nop);
    assertTrue(dexCode.getInstructionList().get(2) instanceof DexTryBlockStart);
    assertTrue(dexCode.getInstructionList().get(3) instanceof DexInstruction_Nop);
    assertTrue(dexCode.getInstructionList().get(4) instanceof DexTryBlockEnd);
  }

  @Test(expected=InstructionParseError.class)
  public void testParse_TryBlock_StartOffsetError() {
    val handler1 = new EncodedCatchHandler(null, -1);
    val try1 = new TryItem(2, 1, handler1);

    val insns = Arrays.asList(new Instruction[] { new Instruction10x(Opcode.NOP), new Instruction10x(Opcode.NOP) });
    val tries = Arrays.asList(new TryItem[] { try1 });
    val handlers = Arrays.asList(new EncodedCatchHandler[] { handler1 });

    val codeItem = CodeItem.internCodeItem(new DexFile(), 1, 0, 0, null, insns, tries, handlers);
    new DexCode(codeItem, new DexTypeCache());
  }

  @Test(expected=InstructionParseError.class)
  public void testParse_TryBlock_EndOffsetError() {
    val handler1 = new EncodedCatchHandler(null, -1);
    val try1 = new TryItem(0, 3, handler1);

    val insns = Arrays.asList(new Instruction[] { new Instruction10x(Opcode.NOP), new Instruction10x(Opcode.NOP) });
    val tries = Arrays.asList(new TryItem[] { try1 });
    val handlers = Arrays.asList(new EncodedCatchHandler[] { handler1 });

    val codeItem = CodeItem.internCodeItem(new DexFile(), 1, 0, 0, null, insns, tries, handlers);
    new DexCode(codeItem, new DexTypeCache());
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
    val dexCode = new DexCode(codeItem, new DexTypeCache());

    assertTrue(dexCode.getInstructionList().get(1) instanceof DexTryBlockStart);
    assertTrue(dexCode.getInstructionList().get(2) instanceof DexInstruction_Nop);
    assertTrue(dexCode.getInstructionList().get(3) instanceof DexTryBlockEnd);
    assertTrue(dexCode.getInstructionList().get(4) instanceof DexCatch);
    assertTrue(dexCode.getInstructionList().get(5) instanceof DexInstruction_Nop);
    assertTrue(dexCode.getInstructionList().get(6) instanceof DexInstruction_Nop);

    val catchElem = (DexCatch) dexCode.getInstructionList().get(4);
    assertEquals("Lcom/example/MyException;", catchElem.getExceptionType().getDescriptor());

    val tryStartElem = (DexTryBlockStart) dexCode.getInstructionList().get(1);
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
    val dexCode = new DexCode(codeItem, new DexTypeCache());

    assertTrue(dexCode.getInstructionList().get(1) instanceof DexTryBlockStart);
    assertTrue(dexCode.getInstructionList().get(2) instanceof DexInstruction_Nop);
    assertTrue(dexCode.getInstructionList().get(3) instanceof DexTryBlockEnd);
    assertTrue(dexCode.getInstructionList().get(4) instanceof DexCatch);
    assertTrue(dexCode.getInstructionList().get(5) instanceof DexInstruction_Nop);
    assertTrue(dexCode.getInstructionList().get(6) instanceof DexInstruction_Nop);
  }

  @Test(expected=InstructionParseError.class)
  public void testParse_CatchBlock_WrongOffset() {
    val dexFile = new DexFile();

    val exceptionType = TypeIdItem.internTypeIdItem(dexFile, "Lcom/example/MyException;");

    val handler1 = new EncodedCatchHandler(new EncodedTypeAddrPair[] { new EncodedTypeAddrPair(exceptionType, 3) }, -1);
    val try1 = new TryItem(0, 1, handler1);

    val insns = Arrays.asList(new Instruction[] { new Instruction10x(Opcode.NOP), new Instruction10x(Opcode.NOP), new Instruction10x(Opcode.NOP) });
    val tries = Arrays.asList(new TryItem[] { try1 });
    val handlers = Arrays.asList(new EncodedCatchHandler[] { handler1 });

    val codeItem = CodeItem.internCodeItem(dexFile, 1, 0, 0, null, insns, tries, handlers);
    new DexCode(codeItem, new DexTypeCache());
  }

  @Test(expected=InstructionParseError.class)
  public void testParse_CatchBlockNotMentionedInCodeItem() {
    val dexFile = new DexFile();

    val exceptionType = TypeIdItem.internTypeIdItem(dexFile, "Lcom/example/MyException;");

    val handler1 = new EncodedCatchHandler(new EncodedTypeAddrPair[] { new EncodedTypeAddrPair(exceptionType, 1) }, -1);
    val try1 = new TryItem(0, 1, handler1);

    val insns = Arrays.asList(new Instruction[] { new Instruction10x(Opcode.NOP), new Instruction10x(Opcode.NOP), new Instruction10x(Opcode.NOP) });
    val tries = Arrays.asList(new TryItem[] { try1 });
    val handlers = Arrays.asList(new EncodedCatchHandler[] { });

    val codeItem = CodeItem.internCodeItem(dexFile, 1, 0, 0, null, insns, tries, handlers);
    new DexCode(codeItem, new DexTypeCache());
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
    val dexCode = new DexCode(codeItem, new DexTypeCache());

    assertTrue(dexCode.getInstructionList().get(1) instanceof DexTryBlockStart);
    assertTrue(dexCode.getInstructionList().get(2) instanceof DexInstruction_Nop);
    assertTrue(dexCode.getInstructionList().get(3) instanceof DexTryBlockEnd);
    assertTrue(dexCode.getInstructionList().get(4) instanceof DexCatchAll);
    assertTrue(dexCode.getInstructionList().get(5) instanceof DexInstruction_Nop);
    assertTrue(dexCode.getInstructionList().get(6) instanceof DexInstruction_Nop);

    val catchElem = (DexCatchAll) dexCode.getInstructionList().get(4);
    val tryStartElem = (DexTryBlockStart) dexCode.getInstructionList().get(1);
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
    val dexCode = new DexCode(codeItem, new DexTypeCache());

    assertTrue(dexCode.getInstructionList().get(1) instanceof DexTryBlockStart);
    assertTrue(dexCode.getInstructionList().get(2) instanceof DexInstruction_Nop);
    assertTrue(dexCode.getInstructionList().get(3) instanceof DexTryBlockEnd);
    assertTrue(dexCode.getInstructionList().get(4) instanceof DexCatchAll);
    assertTrue(dexCode.getInstructionList().get(5) instanceof DexInstruction_Nop);
    assertTrue(dexCode.getInstructionList().get(6) instanceof DexInstruction_Nop);
  }

  @Test(expected=InstructionParseError.class)
  public void testParse_CatchAllBlock_WrongOffset() {
    val dexFile = new DexFile();

    val handler1 = new EncodedCatchHandler(null, 3);
    val try1 = new TryItem(0, 1, handler1);

    val insns = Arrays.asList(new Instruction[] { new Instruction10x(Opcode.NOP), new Instruction10x(Opcode.NOP), new Instruction10x(Opcode.NOP) });
    val tries = Arrays.asList(new TryItem[] { try1 });
    val handlers = Arrays.asList(new EncodedCatchHandler[] { handler1 });

    val codeItem = CodeItem.internCodeItem(dexFile, 1, 0, 0, null, insns, tries, handlers);
    new DexCode(codeItem, new DexTypeCache());
  }

  @Test(expected=InstructionParseError.class)
  public void testParse_CatchAllBlockNotMentionedInCodeItem() {
    val dexFile = new DexFile();

    val handler1 = new EncodedCatchHandler(null, 1);
    val try1 = new TryItem(0, 1, handler1);

    val insns = Arrays.asList(new Instruction[] { new Instruction10x(Opcode.NOP), new Instruction10x(Opcode.NOP), new Instruction10x(Opcode.NOP) });
    val tries = Arrays.asList(new TryItem[] { try1 });
    val handlers = Arrays.asList(new EncodedCatchHandler[] { });

    val codeItem = CodeItem.internCodeItem(dexFile, 1, 0, 0, null, insns, tries, handlers);
    new DexCode(codeItem, new DexTypeCache());
  }
}
