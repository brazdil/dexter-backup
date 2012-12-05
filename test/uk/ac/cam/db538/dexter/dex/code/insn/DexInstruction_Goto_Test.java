package uk.ac.cam.db538.dexter.dex.code.insn;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction10t;
import org.jf.dexlib.Code.Format.Instruction10x;
import org.jf.dexlib.Code.Format.Instruction20t;
import org.jf.dexlib.Code.Format.Instruction30t;
import org.jf.dexlib.Code.Format.Instruction32x;
import org.junit.Test;

import uk.ac.cam.db538.dexter.dex.code.Utils;

public class DexInstruction_Goto_Test {

  @Test
  public void testGoto() {
    Utils.parseAndCompare(
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
    Utils.parseAndCompare(
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

  @Test
  public void testGoto16() {
    Utils.parseAndCompare(
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
    Utils.parseAndCompare(
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
  public void testGoto32() {
    Utils.parseAndCompare(
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
    Utils.parseAndCompare(
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
}
