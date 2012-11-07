package uk.ac.cam.db538.dexter.dex.code.insn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import lombok.val;

import org.jf.dexlib.DexFile;
import org.jf.dexlib.StringIdItem;
import org.jf.dexlib.TypeIdItem;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction10t;
import org.jf.dexlib.Code.Format.Instruction10x;
import org.jf.dexlib.Code.Format.Instruction11x;
import org.jf.dexlib.Code.Format.Instruction12x;
import org.jf.dexlib.Code.Format.Instruction20t;
import org.jf.dexlib.Code.Format.Instruction21c;
import org.jf.dexlib.Code.Format.Instruction21h;
import org.jf.dexlib.Code.Format.Instruction21s;
import org.jf.dexlib.Code.Format.Instruction21t;
import org.jf.dexlib.Code.Format.Instruction22c;
import org.jf.dexlib.Code.Format.Instruction22t;
import org.jf.dexlib.Code.Format.Instruction22x;
import org.jf.dexlib.Code.Format.Instruction30t;
import org.jf.dexlib.Code.Format.Instruction31c;
import org.jf.dexlib.Code.Format.Instruction31i;
import org.jf.dexlib.Code.Format.Instruction32x;
import org.jf.dexlib.Code.Format.Instruction51l;
import org.junit.Test;

import uk.ac.cam.db538.dexter.dex.DexParsingCache;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.type.UnknownTypeException;

public class DexInstructionTest {

  @Test
  public void testFitsIntoBits_Signed() {
    assertTrue(DexInstruction.fitsIntoBits_Signed(127L, 8));
    assertFalse(DexInstruction.fitsIntoBits_Signed(128L, 8));
    assertTrue(DexInstruction.fitsIntoBits_Signed(-128L, 8));
    assertFalse(DexInstruction.fitsIntoBits_Signed(-129L, 8));
  }

  @Test
  public void testFitsIntoBits_Unsigned() {
    assertTrue(DexInstruction.fitsIntoBits_Unsigned(255L, 8));
    assertFalse(DexInstruction.fitsIntoBits_Unsigned(256L, 8));
    assertTrue(DexInstruction.fitsIntoBits_Unsigned(0L, 8));
    assertFalse(DexInstruction.fitsIntoBits_Unsigned(-1L, 8));
  }

  @Test
  public void testFitsIntoHighBits_Signed() {
    assertTrue(DexInstruction.fitsIntoHighBits_Signed(127L << 4, 8, 4));
    assertFalse(DexInstruction.fitsIntoHighBits_Signed(128L << 4, 8, 4));
    assertFalse(DexInstruction.fitsIntoHighBits_Signed((127L << 4) | (1L << 3), 8, 4));
    assertTrue(DexInstruction.fitsIntoHighBits_Signed(-128L << 4, 8, 4));
    assertFalse(DexInstruction.fitsIntoHighBits_Signed(-129L << 4, 8, 4));
    assertFalse(DexInstruction.fitsIntoHighBits_Signed((-128L << 4) | (1L << 3), 8, 4));
  }

  private static DexCodeElement compare(Instruction insn, String output) {
    DexCode code;
    try {
      code = new DexCode(new Instruction[] { insn }, new DexParsingCache());
    } catch (UnknownTypeException | InstructionParsingException e) {
      fail(e.getClass().getName() + ": " + e.getMessage());
      return null;
    }
    val insnList = code.getInstructionList();
    assertEquals(1, insnList.size());
    val insnInsn = insnList.get(0);
    assertEquals(output, insnInsn.getOriginalAssembly());
    return insnInsn;
  }

  private static void compareList(Instruction[] insns, String[] output) throws InstructionParsingException {
    DexCode code;
    try {
      code = new DexCode(insns, new DexParsingCache());
    } catch (UnknownTypeException e) {
      fail(e.getClass().getName() + ": " + e.getMessage());
      return;
    }

    val insnList = code.getInstructionList();
    assertEquals(output.length, insnList.size());
    for (int i = 0; i < output.length; ++i)
      assertEquals(output[i], insnList.get(i).getOriginalAssembly());
  }

  @Test
  public void testGetRegister_ReuseRegisters() {
    val insn = (DexInstruction_Move)
               compare(
                 new Instruction12x(Opcode.MOVE, (byte) 3, (byte) 3),
                 "move v3, v3");
    assertTrue(insn.getRegTo() == insn.getRegFrom());
  }

  @Test
  public void testMove() {
    compare(new Instruction12x(Opcode.MOVE, (byte) 1, (byte) 2),
            "move v1, v2");
  }

  @Test
  public void testMoveObject() {
    compare(new Instruction12x(Opcode.MOVE_OBJECT, (byte) 1, (byte) 2),
            "move-object v1, v2");
  }

  @Test
  public void testMoveFrom16() {
    compare(new Instruction22x(Opcode.MOVE_FROM16, (short) 255, 65535),
            "move v255, v65535");
  }

  @Test
  public void testMoveObjectFrom16() {
    compare(new Instruction22x(Opcode.MOVE_OBJECT_FROM16, (short) 255, 65535),
            "move-object v255, v65535");
  }

  @Test
  public void testMove16() {
    compare(new Instruction32x(Opcode.MOVE_16, 65534, 65535),
            "move v65534, v65535");
  }

  @Test
  public void testMoveObject16() {
    compare(new Instruction32x(Opcode.MOVE_OBJECT_16, 65534, 65535),
            "move-object v65534, v65535");
  }

  @Test
  public void testMoveWide() {
    val insn = (DexInstruction_MoveWide)
               compare(
                 new Instruction12x(Opcode.MOVE_WIDE, (byte) 8, (byte) 5),
                 "move-wide v8, v5");
    assertEquals(Integer.valueOf(8), insn.getRegTo1().getId());
    assertEquals(Integer.valueOf(9), insn.getRegTo2().getId());
    assertEquals(Integer.valueOf(5), insn.getRegFrom1().getId());
    assertEquals(Integer.valueOf(6), insn.getRegFrom2().getId());
  }

  @Test
  public void testMoveWideFrom16() {
    val insn = (DexInstruction_MoveWide)
               compare(
                 new Instruction22x(Opcode.MOVE_WIDE_FROM16, (short) 253, 62435),
                 "move-wide v253, v62435");
    assertEquals(Integer.valueOf(253), insn.getRegTo1().getId());
    assertEquals(Integer.valueOf(254), insn.getRegTo2().getId());
    assertEquals(Integer.valueOf(62435), insn.getRegFrom1().getId());
    assertEquals(Integer.valueOf(62436), insn.getRegFrom2().getId());
  }

  @Test
  public void testMoveWide16() {
    val insn = (DexInstruction_MoveWide)
               compare(
                 new Instruction32x(Opcode.MOVE_WIDE_16, 60123, 62435),
                 "move-wide v60123, v62435");
    assertEquals(Integer.valueOf(60123), insn.getRegTo1().getId());
    assertEquals(Integer.valueOf(60124), insn.getRegTo2().getId());
    assertEquals(Integer.valueOf(62435), insn.getRegFrom1().getId());
    assertEquals(Integer.valueOf(62436), insn.getRegFrom2().getId());
  }

  @Test
  public void testMoveResult() {
    compare(new Instruction11x(Opcode.MOVE_RESULT, (short) 234),
            "move-result v234");
  }

  @Test
  public void testMoveResultObject() {
    compare(new Instruction11x(Opcode.MOVE_RESULT_OBJECT, (short) 234),
            "move-result-object v234");
  }

  @Test
  public void testMoveResultWide() {
    val insn = (DexInstruction_MoveResultWide)
               compare(
                 new Instruction11x(Opcode.MOVE_RESULT_WIDE, (short) 233),
                 "move-result-wide v233");
    assertEquals(Integer.valueOf(233), insn.getRegTo1().getId());
    assertEquals(Integer.valueOf(234), insn.getRegTo2().getId());
  }

  @Test
  public void testMoveException() {
    compare(new Instruction11x(Opcode.MOVE_EXCEPTION, (short) 231),
            "move-exception v231");
  }

  @Test
  public void testReturn() {
    compare(new Instruction11x(Opcode.RETURN, (short) 231),
            "return v231");
  }

  @Test
  public void testReturnObject() {
    compare(new Instruction11x(Opcode.RETURN_OBJECT, (short) 230),
            "return-object v230");
  }

  @Test
  public void testReturnWide() {
    val insn = (DexInstruction_ReturnWide)
               compare(
                 new Instruction11x(Opcode.RETURN_WIDE, (short) 235),
                 "return-wide v235");
    assertEquals(Integer.valueOf(235), insn.getRegFrom1().getId());
    assertEquals(Integer.valueOf(236), insn.getRegFrom2().getId());
  }

  @Test
  public void testConstWide16() {
    compare(new Instruction21s(Opcode.CONST_WIDE_16, (short) 236, (short) 32082),
            "const-wide v236, #32082");
    val insn = (DexInstruction_ConstWide)
               compare(new Instruction21s(Opcode.CONST_WIDE_16, (short) 236, (short) -32082),
                       "const-wide v236, #-32082");
    assertEquals(Integer.valueOf(236), insn.getRegTo1().getId());
    assertEquals(Integer.valueOf(237), insn.getRegTo2().getId());
  }

  @Test
  public void testConstWide32() {
    compare(new Instruction31i(Opcode.CONST_WIDE_32, (short) 236, 0x01ABCDEF),
            "const-wide v236, #28036591");
    val insn = (DexInstruction_ConstWide)
               compare(new Instruction31i(Opcode.CONST_WIDE_32, (short) 236, 0xABCDEF01),
                       "const-wide v236, #-1412567295");
    assertEquals(Integer.valueOf(236), insn.getRegTo1().getId());
    assertEquals(Integer.valueOf(237), insn.getRegTo2().getId());
  }

  @Test
  public void testConstWide() {
    compare(new Instruction51l(Opcode.CONST_WIDE, (short) 236, 0x0102030405060708L),
            "const-wide v236, #72623859790382856");
    val insn = (DexInstruction_ConstWide)
               compare(new Instruction51l(Opcode.CONST_WIDE, (short) 236, 0xFFFFFFFFFFFFFFFEL),
                       "const-wide v236, #-2");
    assertEquals(Integer.valueOf(236), insn.getRegTo1().getId());
    assertEquals(Integer.valueOf(237), insn.getRegTo2().getId());
  }

  @Test
  public void testConstWideHigh16() {
    compare(new Instruction21h(Opcode.CONST_WIDE_HIGH16, (short) 236, (short) 0x1234),
            "const-wide v236, #1311673391471656960");
    val insn = (DexInstruction_ConstWide)
               compare(new Instruction21h(Opcode.CONST_WIDE_HIGH16, (short) 236, (short) 0xFEDC),
                       "const-wide v236, #-82190693199511552");
    assertEquals(Integer.valueOf(236), insn.getRegTo1().getId());
    assertEquals(Integer.valueOf(237), insn.getRegTo2().getId());
  }

  private static StringIdItem getStringItem(String str) {
    return StringIdItem.internStringIdItem(new DexFile(), str);
  }

  @Test
  public void testConstString() {
    compare(new Instruction21c(Opcode.CONST_STRING, (short) 236, getStringItem("Hello, world!")),
            "const-string v236, \"Hello, world!\"");
    // escaping characters
    compare(new Instruction21c(Opcode.CONST_STRING, (short) 236, getStringItem("Hello, \"world!")),
            "const-string v236, \"Hello, \\\"world!\"");
    // cutting off after 15 characters
    compare(new Instruction21c(Opcode.CONST_STRING, (short) 236, getStringItem("123456789012345")),
            "const-string v236, \"123456789012345\"");
    compare(new Instruction21c(Opcode.CONST_STRING, (short) 236, getStringItem("1234567890123456")),
            "const-string v236, \"123456789012345...\"");
    compare(new Instruction21c(Opcode.CONST_STRING, (short) 236, getStringItem("12345678901234\"")),
            "const-string v236, \"12345678901234\\...\"");
  }

  @Test
  public void testConstStringJumbo() {
    compare(new Instruction31c(Opcode.CONST_STRING_JUMBO, (short) 236, getStringItem("Hello, world!")),
            "const-string v236, \"Hello, world!\"");
  }

  private static TypeIdItem getTypeItem(String desc) {
    return TypeIdItem.internTypeIdItem(new DexFile(), desc);
  }

  @Test
  public void testConstClass() {
    compare(new Instruction21c(Opcode.CONST_CLASS, (short) 236, getTypeItem("Ljava.lang.String;")),
            "const-class v236, Ljava.lang.String;");
    compare(new Instruction21c(Opcode.CONST_CLASS, (short) 236, getTypeItem("[Ljava.lang.String;")),
            "const-class v236, [Ljava.lang.String;");
  }

  @Test
  public void testMonitorEnter() {
    compare(new Instruction11x(Opcode.MONITOR_ENTER, (short) 244),
            "monitor-enter v244");
  }

  @Test
  public void testMonitorExit() {
    compare(new Instruction11x(Opcode.MONITOR_EXIT, (short) 245),
            "monitor-exit v245");
  }

  @Test
  public void testCheckCast() {
    compare(new Instruction21c(Opcode.CHECK_CAST, (short) 236, getTypeItem("Ljava.lang.String;")),
            "check-cast v236, Ljava.lang.String;");
    compare(new Instruction21c(Opcode.CHECK_CAST, (short) 236, getTypeItem("[Ljava.lang.String;")),
            "check-cast v236, [Ljava.lang.String;");
  }

  @Test
  public void testInstanceOf() {
    compare(new Instruction22c(Opcode.INSTANCE_OF, (byte) 4, (byte) 5, getTypeItem("Ljava.lang.String;")),
            "instance-of v4, v5, Ljava.lang.String;");
    compare(new Instruction22c(Opcode.INSTANCE_OF, (byte) 4, (byte) 5, getTypeItem("[Ljava.lang.String;")),
            "instance-of v4, v5, [Ljava.lang.String;");
  }

  @Test
  public void testNewInstance() {
    compare(new Instruction21c(Opcode.NEW_INSTANCE, (short) 236, getTypeItem("Ljava.lang.String;")),
            "new-instance v236, Ljava.lang.String;");
  }

  @Test
  public void testNewArray() {
    compare(new Instruction22c(Opcode.NEW_ARRAY, (byte) 4, (byte) 5, getTypeItem("[Ljava.lang.String;")),
            "new-array v4, v5, [Ljava.lang.String;");
    compare(new Instruction22c(Opcode.NEW_ARRAY, (byte) 4, (byte) 5, getTypeItem("[I")),
            "new-array v4, v5, [I");
    compare(new Instruction22c(Opcode.NEW_ARRAY, (byte) 4, (byte) 12, getTypeItem("[[[I")),
            "new-array v4, v12, [[[I");
  }

  @Test
  public void testThrow() {
    compare(new Instruction11x(Opcode.THROW, (byte) 243),
            "throw v243");
  }

  @Test
  public void testGoto() throws InstructionParsingException {
    compareList(
      new Instruction[] {
        new Instruction10x(Opcode.NOP),
        new Instruction10t(Opcode.GOTO, -1),
        new Instruction10x(Opcode.NOP)
      }, new String[] {
        "L0:",
        "nop",
        "goto L0",
        "nop"
      });
    compareList(
      new Instruction[] {
        new Instruction32x(Opcode.MOVE_16, 12345, 23456),
        new Instruction10t(Opcode.GOTO, 1),
        new Instruction10x(Opcode.NOP)
      }, new String[] {
        "move v12345, v23456",
        "goto L4",
        "L4:",
        "nop"
      });
  }

  @Test(expected=InstructionParsingException.class)
  public void testLabels_InvalidOffset_Positive() throws InstructionParsingException {
    compareList(
      new Instruction[] {
        new Instruction10t(Opcode.GOTO, 2),
        new Instruction10x(Opcode.NOP)
      }, null);
  }

  @Test(expected=InstructionParsingException.class)
  public void testLabels_InvalidOffset_Negative() throws InstructionParsingException {
    compareList(
      new Instruction[] {
        new Instruction10x(Opcode.NOP),
        new Instruction10t(Opcode.GOTO, -2)
      }, null);
  }

  @Test
  public void testGoto16() throws InstructionParsingException {
    compareList(
      new Instruction[] {
        new Instruction10x(Opcode.NOP),
        new Instruction20t(Opcode.GOTO_16, -1),
        new Instruction10x(Opcode.NOP)
      }, new String[] {
        "L0:",
        "nop",
        "goto L0",
        "nop"
      });
    compareList(
      new Instruction[] {
        new Instruction32x(Opcode.MOVE_16, 12345, 23456),
        new Instruction20t(Opcode.GOTO_16, 2),
        new Instruction10x(Opcode.NOP)
      }, new String[] {
        "move v12345, v23456",
        "goto L5",
        "L5:",
        "nop"
      });
  }

  @Test
  public void testGoto32() throws InstructionParsingException {
    compareList(
      new Instruction[] {
        new Instruction10x(Opcode.NOP),
        new Instruction30t(Opcode.GOTO_32, -1),
        new Instruction10x(Opcode.NOP)
      }, new String[] {
        "L0:",
        "nop",
        "goto L0",
        "nop"
      });
    compareList(
      new Instruction[] {
        new Instruction32x(Opcode.MOVE_16, 12345, 23456),
        new Instruction30t(Opcode.GOTO_32, 3),
        new Instruction10x(Opcode.NOP)
      }, new String[] {
        "move v12345, v23456",
        "goto L6",
        "L6:",
        "nop"
      });
  }

  @Test
  public void testIfTest() throws InstructionParsingException {
    compareList(
      new Instruction[] {
        new Instruction10x(Opcode.NOP),
        new Instruction22t(Opcode.IF_EQ, (byte) 0, (byte) 1, (short) -1),
        new Instruction22t(Opcode.IF_NE, (byte) 2, (byte) 3, (short) -3),
        new Instruction22t(Opcode.IF_LT, (byte) 4, (byte) 5, (short) -5),
        new Instruction22t(Opcode.IF_GE, (byte) 6, (byte) 7, (short) -7),
        new Instruction22t(Opcode.IF_GT, (byte) 8, (byte) 9, (short) -9),
        new Instruction22t(Opcode.IF_LE, (byte) 10, (byte) 11, (short) -10)
      }, new String[] {
        "L0:",
        "nop",
        "L1:",
        "if-eq v0, v1, L0",
        "if-ne v2, v3, L0",
        "if-lt v4, v5, L0",
        "if-ge v6, v7, L0",
        "if-gt v8, v9, L0",
        "if-le v10, v11, L1"
      });
  }

  @Test
  public void testIfTestZero() throws InstructionParsingException {
    compareList(
      new Instruction[] {
        new Instruction10x(Opcode.NOP),
        new Instruction21t(Opcode.IF_EQZ, (short) 130, (short) -1),
        new Instruction21t(Opcode.IF_NEZ, (short) 140, (short) -3),
        new Instruction21t(Opcode.IF_LTZ, (short) 150, (short) -5),
        new Instruction21t(Opcode.IF_GEZ, (short) 160, (short) -7),
        new Instruction21t(Opcode.IF_GTZ, (short) 170, (short) -9),
        new Instruction21t(Opcode.IF_LEZ, (short) 180, (short) -10)
      }, new String[] {
        "L0:",
        "nop",
        "L1:",
        "if-eqz v130, L0",
        "if-nez v140, L0",
        "if-ltz v150, L0",
        "if-gez v160, L0",
        "if-gtz v170, L0",
        "if-lez v180, L1"
      });
  }

  @Test
  public void testUnaryOp() throws InstructionParsingException {
    compareList(
      new Instruction[] {
        new Instruction12x(Opcode.NEG_INT, (byte) 0, (byte) 1),
        new Instruction12x(Opcode.NOT_INT, (byte) 2, (byte) 3),
        new Instruction12x(Opcode.NEG_FLOAT, (byte) 4, (byte) 5)
      }, new String[] {
        "neg-int v0, v1",
        "not-int v2, v3",
        "neg-float v4, v5"
      });
  }

  @Test
  public void testUnaryOpWide() throws InstructionParsingException {
    DexInstruction_UnaryOpWide insn;

    insn = (DexInstruction_UnaryOpWide)
           compare(new Instruction12x(Opcode.NEG_LONG, (byte) 0, (byte) 2),
                   "neg-long v0, v2");
    assertEquals(Integer.valueOf(0), insn.getRegTo1().getId());
    assertEquals(Integer.valueOf(1), insn.getRegTo2().getId());
    assertEquals(Integer.valueOf(2), insn.getRegFrom1().getId());
    assertEquals(Integer.valueOf(3), insn.getRegFrom2().getId());

    insn = (DexInstruction_UnaryOpWide)
           compare(new Instruction12x(Opcode.NOT_LONG, (byte) 4, (byte) 6),
                   "not-long v4, v6");
    assertEquals(Integer.valueOf(4), insn.getRegTo1().getId());
    assertEquals(Integer.valueOf(5), insn.getRegTo2().getId());
    assertEquals(Integer.valueOf(6), insn.getRegFrom1().getId());
    assertEquals(Integer.valueOf(7), insn.getRegFrom2().getId());

    insn = (DexInstruction_UnaryOpWide)
           compare(new Instruction12x(Opcode.NEG_DOUBLE, (byte) 8, (byte) 10),
                   "neg-double v8, v10");
    assertEquals(Integer.valueOf(8), insn.getRegTo1().getId());
    assertEquals(Integer.valueOf(9), insn.getRegTo2().getId());
    assertEquals(Integer.valueOf(10), insn.getRegFrom1().getId());
    assertEquals(Integer.valueOf(11), insn.getRegFrom2().getId());
  }

  @Test
  public void testConvert() throws InstructionParsingException {
    compareList(
      new Instruction[] {
        new Instruction12x(Opcode.INT_TO_FLOAT, (byte) 0, (byte) 1),
        new Instruction12x(Opcode.FLOAT_TO_INT, (byte) 2, (byte) 3),
        new Instruction12x(Opcode.INT_TO_BYTE, (byte) 4, (byte) 5),
        new Instruction12x(Opcode.INT_TO_CHAR, (byte) 6, (byte) 7),
        new Instruction12x(Opcode.INT_TO_SHORT, (byte) 8, (byte) 9)
      }, new String[] {
        "int-to-float v0, v1",
        "float-to-int v2, v3",
        "int-to-byte v4, v5",
        "int-to-char v6, v7",
        "int-to-short v8, v9"
      });
  }

  @Test
  public void testConvertToWide() throws InstructionParsingException {
    DexInstruction_ConvertToWide insn;

    insn = (DexInstruction_ConvertToWide)
           compare(new Instruction12x(Opcode.INT_TO_LONG, (byte) 0, (byte) 2),
                   "int-to-long v0, v2");
    assertEquals(Integer.valueOf(0), insn.getRegTo1().getId());
    assertEquals(Integer.valueOf(1), insn.getRegTo2().getId());
    assertEquals(Integer.valueOf(2), insn.getRegFrom().getId());

    insn = (DexInstruction_ConvertToWide)
           compare(new Instruction12x(Opcode.INT_TO_DOUBLE, (byte) 4, (byte) 6),
                   "int-to-double v4, v6");
    assertEquals(Integer.valueOf(4), insn.getRegTo1().getId());
    assertEquals(Integer.valueOf(5), insn.getRegTo2().getId());
    assertEquals(Integer.valueOf(6), insn.getRegFrom().getId());

    insn = (DexInstruction_ConvertToWide)
           compare(new Instruction12x(Opcode.FLOAT_TO_LONG, (byte) 8, (byte) 10),
                   "float-to-long v8, v10");
    assertEquals(Integer.valueOf(8), insn.getRegTo1().getId());
    assertEquals(Integer.valueOf(9), insn.getRegTo2().getId());
    assertEquals(Integer.valueOf(10), insn.getRegFrom().getId());

    insn = (DexInstruction_ConvertToWide)
           compare(new Instruction12x(Opcode.FLOAT_TO_DOUBLE, (byte) 12, (byte) 14),
                   "float-to-double v12, v14");
    assertEquals(Integer.valueOf(12), insn.getRegTo1().getId());
    assertEquals(Integer.valueOf(13), insn.getRegTo2().getId());
    assertEquals(Integer.valueOf(14), insn.getRegFrom().getId());
  }

  @Test
  public void testConvertFromWide() throws InstructionParsingException {
    DexInstruction_ConvertFromWide insn;

    insn = (DexInstruction_ConvertFromWide)
           compare(new Instruction12x(Opcode.LONG_TO_INT, (byte) 0, (byte) 2),
                   "long-to-int v0, v2");
    assertEquals(Integer.valueOf(0), insn.getRegTo().getId());
    assertEquals(Integer.valueOf(2), insn.getRegFrom1().getId());
    assertEquals(Integer.valueOf(3), insn.getRegFrom2().getId());

    insn = (DexInstruction_ConvertFromWide)
           compare(new Instruction12x(Opcode.LONG_TO_FLOAT, (byte) 8, (byte) 10),
                   "long-to-float v8, v10");
    assertEquals(Integer.valueOf(8), insn.getRegTo().getId());
    assertEquals(Integer.valueOf(10), insn.getRegFrom1().getId());
    assertEquals(Integer.valueOf(11), insn.getRegFrom2().getId());

    insn = (DexInstruction_ConvertFromWide)
           compare(new Instruction12x(Opcode.DOUBLE_TO_INT, (byte) 4, (byte) 6),
                   "double-to-int v4, v6");
    assertEquals(Integer.valueOf(4), insn.getRegTo().getId());
    assertEquals(Integer.valueOf(6), insn.getRegFrom1().getId());
    assertEquals(Integer.valueOf(7), insn.getRegFrom2().getId());

    insn = (DexInstruction_ConvertFromWide)
           compare(new Instruction12x(Opcode.DOUBLE_TO_FLOAT, (byte) 12, (byte) 14),
                   "double-to-float v12, v14");
    assertEquals(Integer.valueOf(12), insn.getRegTo().getId());
    assertEquals(Integer.valueOf(14), insn.getRegFrom1().getId());
    assertEquals(Integer.valueOf(15), insn.getRegFrom2().getId());
  }

  @Test
  public void testConvertWide() throws InstructionParsingException {
    DexInstruction_ConvertWide insn;

    insn = (DexInstruction_ConvertWide)
           compare(new Instruction12x(Opcode.LONG_TO_DOUBLE, (byte) 0, (byte) 2),
                   "long-to-double v0, v2");
    assertEquals(Integer.valueOf(0), insn.getRegTo1().getId());
    assertEquals(Integer.valueOf(1), insn.getRegTo2().getId());
    assertEquals(Integer.valueOf(2), insn.getRegFrom1().getId());
    assertEquals(Integer.valueOf(3), insn.getRegFrom2().getId());

    insn = (DexInstruction_ConvertWide)
           compare(new Instruction12x(Opcode.DOUBLE_TO_LONG, (byte) 8, (byte) 10),
                   "double-to-long v8, v10");
    assertEquals(Integer.valueOf(8), insn.getRegTo1().getId());
    assertEquals(Integer.valueOf(9), insn.getRegTo2().getId());
    assertEquals(Integer.valueOf(10), insn.getRegFrom1().getId());
    assertEquals(Integer.valueOf(11), insn.getRegFrom2().getId());
  }

}
