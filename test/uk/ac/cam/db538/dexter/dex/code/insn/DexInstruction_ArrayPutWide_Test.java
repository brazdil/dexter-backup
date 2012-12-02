package uk.ac.cam.db538.dexter.dex.code.insn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import lombok.val;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction23x;
import org.junit.Test;

import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.Utils;

public class DexInstruction_ArrayPutWide_Test {

  @Test
  public void testParse_ArrayPutWide() throws InstructionParsingException {
    Utils.parseAndCompare(
      new Instruction[] {
        new Instruction23x(Opcode.APUT_WIDE, (short) 200, (short) 201, (short) 202),
        new Instruction23x(Opcode.APUT_WIDE, (short) 203, (short) 204, (short) 205)
      }, new String[] {
        "aput-wide v200, {v201}[v202]",
        "aput-wide v203, {v204}[v205]",
      });
  }

  @Test
  public void testAssemble_ArrayPutWide() {
    val regNFrom = Utils.numFitsInto_Unsigned(8);
    val regNArray = Utils.numFitsInto_Unsigned(8) - 1;
    val regNIndex = Utils.numFitsInto_Unsigned(8) - 2;
    val regFrom1 = new DexRegister(regNFrom);
    val regFrom2 = new DexRegister(regNFrom + 1);
    val regArray = new DexRegister(regNArray);
    val regIndex = new DexRegister(regNIndex);
    val regAlloc = Utils.genRegAlloc(regFrom1, regFrom2, regArray, regIndex);

    val insn = new DexInstruction_ArrayPutWide(
      null,
      regFrom1,
      regFrom2,
      regArray,
      regIndex);

    val asm = insn.assembleBytecode(Utils.genAsmState(regAlloc));
    assertEquals(1, asm.length);
    assertTrue(asm[0] instanceof Instruction23x);

    val asmInsn = (Instruction23x) asm[0];
    assertEquals(regNFrom, asmInsn.getRegisterA());
    assertEquals(regNArray, asmInsn.getRegisterB());
    assertEquals(regNIndex, asmInsn.getRegisterC());
    assertEquals(Opcode.APUT_WIDE, asmInsn.opcode);
  }

  @Test(expected=InstructionAssemblyException.class)
  public void testAssemble_ArrayPutWide_WrongAllocation_RegisterFrom() {
    val regNFrom = Utils.numFitsInto_Unsigned(9);
    val regNArray = Utils.numFitsInto_Unsigned(8) - 1;
    val regNIndex = Utils.numFitsInto_Unsigned(8) - 2;
    val regFrom1 = new DexRegister(regNFrom);
    val regFrom2 = new DexRegister(regNFrom + 1);
    val regArray = new DexRegister(regNArray);
    val regIndex = new DexRegister(regNIndex);
    val regAlloc = Utils.genRegAlloc(regFrom1, regFrom2, regArray, regIndex);

    val insn = new DexInstruction_ArrayPutWide(
      null,
      regFrom1,
      regFrom2,
      regArray,
      regIndex);

    insn.assembleBytecode(Utils.genAsmState(regAlloc));
  }

  @Test(expected=InstructionAssemblyException.class)
  public void testAssemble_ArrayPutWide_WrongAllocation_RegisterArray() {
    val regNFrom = Utils.numFitsInto_Unsigned(8);
    val regNArray = Utils.numFitsInto_Unsigned(9);
    val regNIndex = Utils.numFitsInto_Unsigned(8) - 2;
    val regFrom1 = new DexRegister(regNFrom);
    val regFrom2 = new DexRegister(regNFrom + 1);
    val regArray = new DexRegister(regNArray);
    val regIndex = new DexRegister(regNIndex);
    val regAlloc = Utils.genRegAlloc(regFrom1, regFrom2, regArray, regIndex);

    val insn = new DexInstruction_ArrayPutWide(
      null,
      regFrom1,
      regFrom2,
      regArray,
      regIndex);

    insn.assembleBytecode(Utils.genAsmState(regAlloc));
  }

  @Test(expected=InstructionAssemblyException.class)
  public void testAssemble_ArrayPutWide_WrongAllocation_RegisterIndex() {
    val regNFrom = Utils.numFitsInto_Unsigned(8);
    val regNArray = Utils.numFitsInto_Unsigned(8) - 1;
    val regNIndex = Utils.numFitsInto_Unsigned(9);
    val regFrom1 = new DexRegister(regNFrom);
    val regFrom2 = new DexRegister(regNFrom + 1);
    val regArray = new DexRegister(regNArray);
    val regIndex = new DexRegister(regNIndex);
    val regAlloc = Utils.genRegAlloc(regFrom1, regFrom2, regArray, regIndex);

    val insn = new DexInstruction_ArrayPutWide(
      null,
      regFrom1,
      regFrom2,
      regArray,
      regIndex);

    insn.assembleBytecode(Utils.genAsmState(regAlloc));
  }

  @Test(expected=InstructionAssemblyException.class)
  public void testAssemble_ArrayPutWide_WrongAllocation_FollowUp() {
    val regNFrom = Utils.numFitsInto_Unsigned(8);
    val regNArray = Utils.numFitsInto_Unsigned(8) - 1;
    val regNIndex = Utils.numFitsInto_Unsigned(8) - 2;
    val regFrom1 = new DexRegister(regNFrom);
    val regFrom2 = new DexRegister(regNFrom + 2);
    val regArray = new DexRegister(regNArray);
    val regIndex = new DexRegister(regNIndex);
    val regAlloc = Utils.genRegAlloc(regFrom1, regFrom2, regArray, regIndex);

    val insn = new DexInstruction_ArrayPutWide(
      null,
      regFrom1,
      regFrom2,
      regArray,
      regIndex);

    insn.assembleBytecode(Utils.genAsmState(regAlloc));
  }
}
