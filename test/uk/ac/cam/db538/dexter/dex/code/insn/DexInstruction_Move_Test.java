package uk.ac.cam.db538.dexter.dex.code.insn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import lombok.val;

import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction12x;
import org.jf.dexlib.Code.Format.Instruction22x;
import org.jf.dexlib.Code.Format.Instruction32x;
import org.junit.Test;

import uk.ac.cam.db538.dexter.dex.code.DexRegister;
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

  @Test
  public void testAssemble_Move() {
    val rTo = Utils.numFitsInto_Unsigned(4);
    val rFrom = Utils.numFitsInto_Unsigned(4);

    val regTo = new DexRegister(rTo);
    val regFrom = new DexRegister(rFrom);
    val regAlloc = Utils.genRegAlloc(regTo, regFrom);

    val insn = new DexInstruction_Move(null, regTo, regFrom, false);

    val asm = insn.assembleBytecode(regAlloc);
    assertEquals(1, asm.length);
    assertTrue(asm[0] instanceof Instruction12x);

    val asmInsn = (Instruction12x) asm[0];
    assertEquals(Opcode.MOVE, asmInsn.opcode);
    assertEquals(rTo, asmInsn.getRegisterA());
    assertEquals(rFrom, asmInsn.getRegisterB());
  }

  @Test
  public void testAssemble_MoveObject() {
    val rTo = Utils.numFitsInto_Unsigned(4);
    val rFrom = Utils.numFitsInto_Unsigned(4);

    val regTo = new DexRegister(rTo);
    val regFrom = new DexRegister(rFrom);
    val regAlloc = Utils.genRegAlloc(regTo, regFrom);

    val insn = new DexInstruction_Move(null, regTo, regFrom, true);

    val asm = insn.assembleBytecode(regAlloc);
    assertEquals(1, asm.length);
    assertTrue(asm[0] instanceof Instruction12x);

    val asmInsn = (Instruction12x) asm[0];
    assertEquals(Opcode.MOVE_OBJECT, asmInsn.opcode);
    assertEquals(rTo, asmInsn.getRegisterA());
    assertEquals(rFrom, asmInsn.getRegisterB());
  }

  @Test
  public void testAssemble_MoveFrom16() {
    val rTo = Utils.numFitsInto_Unsigned(8);
    val rFrom = Utils.numFitsInto_Unsigned(9);

    val regTo = new DexRegister(rTo);
    val regFrom = new DexRegister(rFrom);
    val regAlloc = Utils.genRegAlloc(regTo, regFrom);

    val insn = new DexInstruction_Move(null, regTo, regFrom, false);

    val asm = insn.assembleBytecode(regAlloc);
    assertEquals(1, asm.length);
    assertTrue(asm[0] instanceof Instruction22x);

    val asmInsn = (Instruction22x) asm[0];
    assertEquals(Opcode.MOVE_FROM16, asmInsn.opcode);
    assertEquals(rTo, asmInsn.getRegisterA());
    assertEquals(rFrom, asmInsn.getRegisterB());
  }

  @Test
  public void testAssemble_MoveObjectFrom16() {
    val rTo = Utils.numFitsInto_Unsigned(4);
    val rFrom = Utils.numFitsInto_Unsigned(9);

    val regTo = new DexRegister(rTo);
    val regFrom = new DexRegister(rFrom);
    val regAlloc = Utils.genRegAlloc(regTo, regFrom);

    val insn = new DexInstruction_Move(null, regTo, regFrom, true);

    val asm = insn.assembleBytecode(regAlloc);
    assertEquals(1, asm.length);
    assertTrue(asm[0] instanceof Instruction22x);

    val asmInsn = (Instruction22x) asm[0];
    assertEquals(Opcode.MOVE_OBJECT_FROM16, asmInsn.opcode);
    assertEquals(rTo, asmInsn.getRegisterA());
    assertEquals(rFrom, asmInsn.getRegisterB());
  }

  @Test
  public void testAssemble_Move16() {
    val rTo = Utils.numFitsInto_Unsigned(9);
    val rFrom = Utils.numFitsInto_Unsigned(9);

    val regTo = new DexRegister(rTo);
    val regFrom = new DexRegister(rFrom);
    val regAlloc = Utils.genRegAlloc(regTo, regFrom);

    val insn = new DexInstruction_Move(null, regTo, regFrom, false);

    val asm = insn.assembleBytecode(regAlloc);
    assertEquals(1, asm.length);
    assertTrue(asm[0] instanceof Instruction32x);

    val asmInsn = (Instruction32x) asm[0];
    assertEquals(Opcode.MOVE_16, asmInsn.opcode);
    assertEquals(rTo, asmInsn.getRegisterA());
    assertEquals(rFrom, asmInsn.getRegisterB());
  }

  @Test
  public void testAssemble_MoveObject16() {
    val rTo = Utils.numFitsInto_Unsigned(9);
    val rFrom = Utils.numFitsInto_Unsigned(9);

    val regTo = new DexRegister(rTo);
    val regFrom = new DexRegister(rFrom);
    val regAlloc = Utils.genRegAlloc(regTo, regFrom);

    val insn = new DexInstruction_Move(null, regTo, regFrom, true);

    val asm = insn.assembleBytecode(regAlloc);
    assertEquals(1, asm.length);
    assertTrue(asm[0] instanceof Instruction32x);

    val asmInsn = (Instruction32x) asm[0];
    assertEquals(Opcode.MOVE_OBJECT_16, asmInsn.opcode);
    assertEquals(rTo, asmInsn.getRegisterA());
    assertEquals(rFrom, asmInsn.getRegisterB());
  }

}
