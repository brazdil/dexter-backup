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

public class DexInstruction_ArrayGetWide_Test {

  @Test
  public void testParse_ArrayGetWide() throws InstructionParsingException {
    Utils.parseAndCompare(
      new Instruction[] {
        new Instruction23x(Opcode.AGET_WIDE, (short) 200, (short) 201, (short) 202),
        new Instruction23x(Opcode.AGET_WIDE, (short) 203, (short) 204, (short) 205)
      }, new String[] {
        "aget-wide v200, {v201}[v202]",
        "aget-wide v203, {v204}[v205]"
      });
  }

  @Test
  public void testAssemble_ArrayGetWide() {
    val regNTo = Utils.numFitsInto_Unsigned(8);
    val regNArray = Utils.numFitsInto_Unsigned(8) - 1;
    val regNIndex = Utils.numFitsInto_Unsigned(8) - 2;
    val regTo1 = new DexRegister(regNTo);
    val regTo2 = new DexRegister(regNTo + 1);
    val regArray = new DexRegister(regNArray);
    val regIndex = new DexRegister(regNIndex);
    val regAlloc = Utils.genRegAlloc(regTo1, regTo2, regArray, regIndex);

    val insn = new DexInstruction_ArrayGetWide(
      null,
      regTo1,
      regTo2,
      regArray,
      regIndex);

    val asm = insn.assembleBytecode(Utils.genAsmState(regAlloc));
    assertEquals(1, asm.length);
    assertTrue(asm[0] instanceof Instruction23x);

    val asmInsn = (Instruction23x) asm[0];
    assertEquals(regNTo, asmInsn.getRegisterA());
    assertEquals(regNArray, asmInsn.getRegisterB());
    assertEquals(regNIndex, asmInsn.getRegisterC());
    assertEquals(Opcode.AGET_WIDE, asmInsn.opcode);
  }

  @Test(expected=InstructionAssemblyException.class)
  public void testAssemble_ArrayGetWide_WrongAllocation_RegTo_Range() {
    val regNTo = Utils.numFitsInto_Unsigned(9);
    val regNArray = Utils.numFitsInto_Unsigned(8) - 1;
    val regNIndex = Utils.numFitsInto_Unsigned(8) - 2;
    val regTo1 = new DexRegister(regNTo);
    val regTo2 = new DexRegister(regNTo + 1);
    val regArray = new DexRegister(regNArray);
    val regIndex = new DexRegister(regNIndex);
    val regAlloc = Utils.genRegAlloc(regTo1, regTo2, regArray, regIndex);

    val insn = new DexInstruction_ArrayGetWide(
      null,
      regTo1,
      regTo2,
      regArray,
      regIndex);

    insn.assembleBytecode(Utils.genAsmState(regAlloc));
  }

  @Test(expected=InstructionAssemblyException.class)
  public void testAssemble_ArrayGetWide_WrongAllocation_RegTo_Follow() {
    val regNTo = Utils.numFitsInto_Unsigned(8);
    val regNArray = Utils.numFitsInto_Unsigned(8) - 1;
    val regNIndex = Utils.numFitsInto_Unsigned(8) - 2;
    val regTo1 = new DexRegister(regNTo);
    val regTo2 = new DexRegister(regNTo + 2);
    val regArray = new DexRegister(regNArray);
    val regIndex = new DexRegister(regNIndex);
    val regAlloc = Utils.genRegAlloc(regTo1, regTo2, regArray, regIndex);

    val insn = new DexInstruction_ArrayGetWide(
      null,
      regTo1,
      regTo2,
      regArray,
      regIndex);

    insn.assembleBytecode(Utils.genAsmState(regAlloc));
  }

  @Test(expected=InstructionAssemblyException.class)
  public void testAssemble_ArrayGetWide_WrongAllocation_RegArray() {
    val regNTo = Utils.numFitsInto_Unsigned(8);
    val regNArray = Utils.numFitsInto_Unsigned(9);
    val regNIndex = Utils.numFitsInto_Unsigned(8) - 2;
    val regTo1 = new DexRegister(regNTo);
    val regTo2 = new DexRegister(regNTo + 1);
    val regArray = new DexRegister(regNArray);
    val regIndex = new DexRegister(regNIndex);
    val regAlloc = Utils.genRegAlloc(regTo1, regTo2, regArray, regIndex);

    val insn = new DexInstruction_ArrayGetWide(
      null,
      regTo1,
      regTo2,
      regArray,
      regIndex);

    insn.assembleBytecode(Utils.genAsmState(regAlloc));
  }

  @Test(expected=InstructionAssemblyException.class)
  public void testAssemble_ArrayGetWide_WrongAllocation_RegIndex() {
    val regNTo = Utils.numFitsInto_Unsigned(8);
    val regNArray = Utils.numFitsInto_Unsigned(8) - 1;
    val regNIndex = Utils.numFitsInto_Unsigned(9);
    val regTo1 = new DexRegister(regNTo);
    val regTo2 = new DexRegister(regNTo + 1);
    val regArray = new DexRegister(regNArray);
    val regIndex = new DexRegister(regNIndex);
    val regAlloc = Utils.genRegAlloc(regTo1, regTo2, regArray, regIndex);

    val insn = new DexInstruction_ArrayGetWide(
      null,
      regTo1,
      regTo2,
      regArray,
      regIndex);

    insn.assembleBytecode(Utils.genAsmState(regAlloc));
  }
}
