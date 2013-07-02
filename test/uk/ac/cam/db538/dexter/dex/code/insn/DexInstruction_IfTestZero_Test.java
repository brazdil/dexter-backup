package uk.ac.cam.db538.dexter.dex.code.insn;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction10x;
import org.jf.dexlib.Code.Format.Instruction21t;
import org.junit.Test;

import uk.ac.cam.db538.dexter.dex.code.Utils;

public class DexInstruction_IfTestZero_Test {

  @Test
  public void testParse_IfTestZero() {
    Utils.parseAndCompare(
      new Instruction[] {
        new Instruction10x(Opcode.NOP),
        new Instruction21t(Opcode.IF_EQZ, (short) 130, (short) -1),
        new Instruction21t(Opcode.IF_NEZ, (short) 140, (short) -3),
        new Instruction21t(Opcode.IF_LTZ, (short) 150, (short) -5),
        new Instruction21t(Opcode.IF_GEZ, (short) 160, (short) -7),
        new Instruction21t(Opcode.IF_GTZ, (short) 170, (short) 4),
        new Instruction21t(Opcode.IF_LEZ, (short) 180, (short) 2),
        new Instruction10x(Opcode.NOP)
      }, new String[] {
        "L0:",
        "nop",
        "if-eqz v130, L0",
        "if-nez v140, L0",
        "if-ltz v150, L0",
        "if-gez v160, L0",
        "if-gtz v170, L13",
        "if-lez v180, L13",
        "L13:",
        "nop"
      });
  }
}
