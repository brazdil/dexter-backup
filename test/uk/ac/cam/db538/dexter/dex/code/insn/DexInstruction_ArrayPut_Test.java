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

public class DexInstruction_ArrayPut_Test {

  @Test
  public void testParse_ArrayPut() throws InstructionParsingException {
    Utils.parseAndCompare(
      new Instruction[] {
        new Instruction23x(Opcode.APUT_OBJECT, (short) 200, (short) 201, (short) 202),
        new Instruction23x(Opcode.APUT, (short) 203, (short) 204, (short) 205),
        new Instruction23x(Opcode.APUT, (short) 206, (short) 207, (short) 208),
        new Instruction23x(Opcode.APUT_BOOLEAN, (short) 209, (short) 210, (short) 211),
        new Instruction23x(Opcode.APUT_BYTE, (short) 212, (short) 213, (short) 214),
        new Instruction23x(Opcode.APUT_CHAR, (short) 215, (short) 216, (short) 217),
        new Instruction23x(Opcode.APUT_SHORT, (short) 218, (short) 219, (short) 220)
      }, new String[] {
        "aput-object v200, {v201}[v202]",
        "aput-int-float v203, {v204}[v205]",
        "aput-int-float v206, {v207}[v208]",
        "aput-boolean v209, {v210}[v211]",
        "aput-byte v212, {v213}[v214]",
        "aput-char v215, {v216}[v217]",
        "aput-short v218, {v219}[v220]"
      });
  }

  @Test
  public void testAssemble_ArrayPut() {
    val regNFrom = Utils.numFitsInto_Unsigned(8);
    val regNArray = Utils.numFitsInto_Unsigned(8) - 1;
    val regNIndex = Utils.numFitsInto_Unsigned(8) - 2;
    val regFrom = new DexRegister(regNFrom);
    val regArray = new DexRegister(regNArray);
    val regIndex = new DexRegister(regNIndex);
    val regAlloc = Utils.genRegAlloc(regFrom, regArray, regIndex);

    val insn = new DexInstruction_ArrayPut(
      null,
      regFrom,
      regArray,
      regIndex,
      Opcode_GetPut.Object);

    val asm = insn.assembleBytecode(Utils.genAsmState(regAlloc));
    assertEquals(1, asm.length);
    assertTrue(asm[0] instanceof Instruction23x);

    val asmInsn = (Instruction23x) asm[0];
    assertEquals(regNFrom, asmInsn.getRegisterA());
    assertEquals(regNArray, asmInsn.getRegisterB());
    assertEquals(regNIndex, asmInsn.getRegisterC());
    assertEquals(Opcode.APUT_OBJECT, asmInsn.opcode);
  }

  @Test(expected=InstructionAssemblyException.class)
  public void testAssemble_ArrayPut_RegisterFrom() {
    val regNFrom = Utils.numFitsInto_Unsigned(9);
    val regNArray = Utils.numFitsInto_Unsigned(8) - 1;
    val regNIndex = Utils.numFitsInto_Unsigned(8) - 2;
    val regFrom = new DexRegister(regNFrom);
    val regArray = new DexRegister(regNArray);
    val regIndex = new DexRegister(regNIndex);
    val regAlloc = Utils.genRegAlloc(regFrom, regArray, regIndex);

    val insn = new DexInstruction_ArrayPut(
      null,
      regFrom,
      regArray,
      regIndex,
      Opcode_GetPut.Object);

    insn.assembleBytecode(Utils.genAsmState(regAlloc));
  }

  @Test(expected=InstructionAssemblyException.class)
  public void testAssemble_ArrayPut_RegisterArray() {
    val regNFrom = Utils.numFitsInto_Unsigned(8);
    val regNArray = Utils.numFitsInto_Unsigned(9);
    val regNIndex = Utils.numFitsInto_Unsigned(8) - 2;
    val regFrom = new DexRegister(regNFrom);
    val regArray = new DexRegister(regNArray);
    val regIndex = new DexRegister(regNIndex);
    val regAlloc = Utils.genRegAlloc(regFrom, regArray, regIndex);

    val insn = new DexInstruction_ArrayPut(
      null,
      regFrom,
      regArray,
      regIndex,
      Opcode_GetPut.Object);

    insn.assembleBytecode(Utils.genAsmState(regAlloc));
  }

  @Test(expected=InstructionAssemblyException.class)
  public void testAssemble_ArrayPut_RegisterIndex() {
    val regNFrom = Utils.numFitsInto_Unsigned(8);
    val regNArray = Utils.numFitsInto_Unsigned(8) - 1;
    val regNIndex = Utils.numFitsInto_Unsigned(9);
    val regFrom = new DexRegister(regNFrom);
    val regArray = new DexRegister(regNArray);
    val regIndex = new DexRegister(regNIndex);
    val regAlloc = Utils.genRegAlloc(regFrom, regArray, regIndex);

    val insn = new DexInstruction_ArrayPut(
      null,
      regFrom,
      regArray,
      regIndex,
      Opcode_GetPut.Object);

    insn.assembleBytecode(Utils.genAsmState(regAlloc));
  }
}
