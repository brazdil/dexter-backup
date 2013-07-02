package uk.ac.cam.db538.dexter.dex.code.insn;

import static org.junit.Assert.assertEquals;
import lombok.val;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction12x;
import org.jf.dexlib.Code.Format.Instruction23x;
import org.junit.Test;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.Utils;

public class DexInstruction_BinaryOpWide_Test {

  @Test
  public void testParse_BinaryOpWide() throws InstructionParsingException {
    Utils.parseAndCompare(
      new Instruction[] {
        new Instruction23x(Opcode.ADD_LONG, (short) 234, (short) 235, (short) 236),
        new Instruction23x(Opcode.SUB_LONG, (short) 234, (short) 235, (short) 236),
        new Instruction23x(Opcode.MUL_LONG, (short) 234, (short) 235, (short) 236),
        new Instruction23x(Opcode.DIV_LONG, (short) 234, (short) 235, (short) 236),
        new Instruction23x(Opcode.REM_LONG, (short) 234, (short) 235, (short) 236),
        new Instruction23x(Opcode.AND_LONG, (short) 234, (short) 235, (short) 236),
        new Instruction23x(Opcode.OR_LONG, (short) 234, (short) 235, (short) 236),
        new Instruction23x(Opcode.XOR_LONG, (short) 234, (short) 235, (short) 236),
        new Instruction23x(Opcode.SHL_LONG, (short) 234, (short) 235, (short) 236),
        new Instruction23x(Opcode.SHR_LONG, (short) 234, (short) 235, (short) 236),
        new Instruction23x(Opcode.USHR_LONG, (short) 234, (short) 235, (short) 236),
        new Instruction23x(Opcode.ADD_DOUBLE, (short) 234, (short) 235, (short) 236),
        new Instruction23x(Opcode.SUB_DOUBLE, (short) 234, (short) 235, (short) 236),
        new Instruction23x(Opcode.MUL_DOUBLE, (short) 234, (short) 235, (short) 236),
        new Instruction23x(Opcode.DIV_DOUBLE, (short) 234, (short) 235, (short) 236),
        new Instruction23x(Opcode.REM_DOUBLE, (short) 234, (short) 235, (short) 236)
      }, new String[] {
        "add-long v234|v235, v235|v236, v236|v237",
        "sub-long v234|v235, v235|v236, v236|v237",
        "mul-long v234|v235, v235|v236, v236|v237",
        "div-long v234|v235, v235|v236, v236|v237",
        "rem-long v234|v235, v235|v236, v236|v237",
        "and-long v234|v235, v235|v236, v236|v237",
        "or-long v234|v235, v235|v236, v236|v237",
        "xor-long v234|v235, v235|v236, v236|v237",
        "shl-long v234|v235, v235|v236, v236|v237",
        "shr-long v234|v235, v235|v236, v236|v237",
        "ushr-long v234|v235, v235|v236, v236|v237",
        "add-double v234|v235, v235|v236, v236|v237",
        "sub-double v234|v235, v235|v236, v236|v237",
        "mul-double v234|v235, v235|v236, v236|v237",
        "div-double v234|v235, v235|v236, v236|v237",
        "rem-double v234|v235, v235|v236, v236|v237"
      });

    val insn = (DexInstruction_BinaryOpWide)
               Utils.parseAndCompare(new Instruction23x(Opcode.ADD_LONG, (short) 2, (short) 12, (short) 112),
                                     "add-long v2|v3, v12|v13, v112|v113");
    assertEquals(2, insn.getRegTarget1().getOriginalIndex());
    assertEquals(3, insn.getRegTarget2().getOriginalIndex());
    assertEquals(12, insn.getRegSourceA1().getOriginalIndex());
    assertEquals(13, insn.getRegSourceA2().getOriginalIndex());
    assertEquals(112, insn.getRegSourceB1().getOriginalIndex());
    assertEquals(113, insn.getRegSourceB2().getOriginalIndex());
  }

  @Test
  public void testParse_BinaryOpWide2addr() throws InstructionParsingException {
    Utils.parseAndCompare(
      new Instruction[] {
        new Instruction12x(Opcode.ADD_LONG_2ADDR, (byte) 4, (byte) 14),
        new Instruction12x(Opcode.SUB_LONG_2ADDR, (byte) 4, (byte) 14),
        new Instruction12x(Opcode.MUL_LONG_2ADDR, (byte) 4, (byte) 14),
        new Instruction12x(Opcode.DIV_LONG_2ADDR, (byte) 4, (byte) 14),
        new Instruction12x(Opcode.REM_LONG_2ADDR, (byte) 4, (byte) 14),
        new Instruction12x(Opcode.AND_LONG_2ADDR, (byte) 4, (byte) 14),
        new Instruction12x(Opcode.OR_LONG_2ADDR, (byte) 4, (byte) 14),
        new Instruction12x(Opcode.XOR_LONG_2ADDR, (byte) 4, (byte) 14),
        new Instruction12x(Opcode.SHL_LONG_2ADDR, (byte) 4, (byte) 14),
        new Instruction12x(Opcode.SHR_LONG_2ADDR, (byte) 4, (byte) 14),
        new Instruction12x(Opcode.USHR_LONG_2ADDR, (byte) 4, (byte) 14),
        new Instruction12x(Opcode.ADD_DOUBLE_2ADDR, (byte) 4, (byte) 14),
        new Instruction12x(Opcode.SUB_DOUBLE_2ADDR, (byte) 4, (byte) 14),
        new Instruction12x(Opcode.MUL_DOUBLE_2ADDR, (byte) 4, (byte) 14),
        new Instruction12x(Opcode.DIV_DOUBLE_2ADDR, (byte) 4, (byte) 14),
        new Instruction12x(Opcode.REM_DOUBLE_2ADDR, (byte) 4, (byte) 14)
      }, new String[] {
        "add-long v4|v5, v4|v5, v14|v15",
        "sub-long v4|v5, v4|v5, v14|v15",
        "mul-long v4|v5, v4|v5, v14|v15",
        "div-long v4|v5, v4|v5, v14|v15",
        "rem-long v4|v5, v4|v5, v14|v15",
        "and-long v4|v5, v4|v5, v14|v15",
        "or-long v4|v5, v4|v5, v14|v15",
        "xor-long v4|v5, v4|v5, v14|v15",
        "shl-long v4|v5, v4|v5, v14|v15",
        "shr-long v4|v5, v4|v5, v14|v15",
        "ushr-long v4|v5, v4|v5, v14|v15",
        "add-double v4|v5, v4|v5, v14|v15",
        "sub-double v4|v5, v4|v5, v14|v15",
        "mul-double v4|v5, v4|v5, v14|v15",
        "div-double v4|v5, v4|v5, v14|v15",
        "rem-double v4|v5, v4|v5, v14|v15"
      });

    val insn = (DexInstruction_BinaryOpWide)
               Utils.parseAndCompare(new Instruction12x(Opcode.ADD_LONG_2ADDR, (byte) 3, (byte) 6),
                                     "add-long v3|v4, v3|v4, v6|v7");
    assertEquals(3, insn.getRegTarget1().getOriginalIndex());
    assertEquals(4, insn.getRegTarget2().getOriginalIndex());
    assertEquals(3, insn.getRegSourceA1().getOriginalIndex());
    assertEquals(4, insn.getRegSourceA2().getOriginalIndex());
    assertEquals(6, insn.getRegSourceB1().getOriginalIndex());
    assertEquals(7, insn.getRegSourceB2().getOriginalIndex());
  }

    @Test
  public void testInstrument() {
    val reg0 = new DexRegister(0);
    val reg1 = new DexRegister(1);
    val reg2 = new DexRegister(2);
    val reg3 = new DexRegister(3);
    val reg4 = new DexRegister(4);
    val reg5 = new DexRegister(5);
    val code = new DexCode();
    code.add(new DexInstruction_BinaryOpWide(code, reg0, reg1, reg2, reg3, reg4, reg5, Opcode_BinaryOpWide.SubLong));

    Utils.instrumentAndCompare(
      code,
      new String[] {
        "sub-long v0|v1, v2|v3, v4|v5",
        "or-int v6, v8, v9",
        "or-int v6, v6, v10",
        "or-int v6, v6, v11",
        "move v7, v6"
      });
  }
}
