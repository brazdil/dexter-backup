package uk.ac.cam.db538.dexter.dex.code.insn;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction10x;
import org.jf.dexlib.Code.Format.Instruction22t;
import org.junit.Test;

import uk.ac.cam.db538.dexter.dex.code.Utils;

public class DexInstruction_IfTest_Test {

  @Test
  public void testParse_IfTest() {
    Utils.parseAndCompare(
      new Instruction[] {
        new Instruction10x(Opcode.NOP),
        new Instruction22t(Opcode.IF_EQ, (byte) 0, (byte) 1, (short) -1),
        new Instruction22t(Opcode.IF_NE, (byte) 2, (byte) 3, (short) -3),
        new Instruction22t(Opcode.IF_LT, (byte) 4, (byte) 5, (short) -5),
        new Instruction22t(Opcode.IF_GE, (byte) 6, (byte) 7, (short) -7),
        new Instruction22t(Opcode.IF_GT, (byte) 8, (byte) 9, (short) 4),
        new Instruction22t(Opcode.IF_LE, (byte) 10, (byte) 11, (short) 2),
        new Instruction10x(Opcode.NOP)
      }, new String[] {
        "L0:",
        "nop",
        "if-eq v0, v1, L0",
        "if-ne v2, v3, L0",
        "if-lt v4, v5, L0",
        "if-ge v6, v7, L0",
        "if-gt v8, v9, L13",
        "if-le v10, v11, L13",
        "L13:",
        "nop"
      });
  }
}
