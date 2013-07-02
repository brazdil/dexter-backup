package uk.ac.cam.db538.dexter.dex.code.insn;

import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction12x;
import org.jf.dexlib.Code.Format.Instruction22x;
import org.jf.dexlib.Code.Format.Instruction32x;
import org.junit.Test;

import uk.ac.cam.db538.dexter.dex.code.Utils;

public class DexInstruction_Move_Test {

  @Test
  public void testParse_Move() {
    Utils.parseAndCompare(new Instruction12x(Opcode.MOVE, (byte) 1, (byte) 2),
                          "move v1, v2");
  }

  @Test
  public void testParse_MoveObject() {
    Utils.parseAndCompare(new Instruction12x(Opcode.MOVE_OBJECT, (byte) 1, (byte) 2),
                          "move-object v1, v2");
  }

  @Test
  public void testParse_MoveFrom16() {
    Utils.parseAndCompare(new Instruction22x(Opcode.MOVE_FROM16, (short) 255, 65535),
                          "move v255, v65535");
  }

  @Test
  public void testParse_MoveObjectFrom16() {
    Utils.parseAndCompare(new Instruction22x(Opcode.MOVE_OBJECT_FROM16, (short) 255, 65535),
                          "move-object v255, v65535");
  }

  @Test
  public void testParse_Move16() {
    Utils.parseAndCompare(new Instruction32x(Opcode.MOVE_16, 65534, 65535),
                          "move v65534, v65535");
  }

  @Test
  public void testParse_MoveObject16() {
    Utils.parseAndCompare(new Instruction32x(Opcode.MOVE_OBJECT_16, 65534, 65535),
                          "move-object v65534, v65535");
  }

}
