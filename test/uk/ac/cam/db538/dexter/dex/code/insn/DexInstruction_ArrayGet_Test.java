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

public class DexInstruction_ArrayGet_Test {

  @Test
  public void testParse_ArrayGet() throws InstructionParsingException {
    Utils.parseAndCompare(
      new Instruction[] {
        new Instruction23x(Opcode.AGET_OBJECT, (short) 190, (short) 191, (short) 240),
        new Instruction23x(Opcode.AGET, (short) 192, (short) 193, (short) 241),
        new Instruction23x(Opcode.AGET, (short) 194, (short) 195, (short) 242),
        new Instruction23x(Opcode.AGET_BOOLEAN, (short) 196, (short) 197, (short) 243),
        new Instruction23x(Opcode.AGET_BYTE, (short) 198, (short) 199, (short) 244),
        new Instruction23x(Opcode.AGET_CHAR, (short) 200, (short) 201, (short) 245),
        new Instruction23x(Opcode.AGET_SHORT, (short) 202, (short) 203, (short) 246)
      }, new String[] {
        "aget-object v190, {v191}[v240]",
        "aget-int-float v192, {v193}[v241]",
        "aget-int-float v194, {v195}[v242]",
        "aget-boolean v196, {v197}[v243]",
        "aget-byte v198, {v199}[v244]",
        "aget-char v200, {v201}[v245]",
        "aget-short v202, {v203}[v246]"
      });
  }

  @Test
  public void testAssemble_ArrayGet() {
    val regNTo = Utils.numFitsInto_Unsigned(8);
    val regNArray = Utils.numFitsInto_Unsigned(8 - 1);
    val regNIndex = Utils.numFitsInto_Unsigned(8 - 2);
    val regTo = new DexRegister(regNTo);
    val regArray = new DexRegister(regNArray);
    val regIndex = new DexRegister(regNIndex);
    val regAlloc = Utils.genRegAlloc(regTo, regArray, regIndex);

    val insn = new DexInstruction_ArrayGet(
      null,
      regTo,
      regArray,
      regIndex,
      Opcode_GetPut.Object);

    val asm = insn.assembleBytecode(Utils.genAsmState(regAlloc));
    assertEquals(1, asm.length);
    assertTrue(asm[0] instanceof Instruction23x);

    val asmInsn = (Instruction23x) asm[0];
    assertEquals(regNTo, asmInsn.getRegisterA());
    assertEquals(regNArray, asmInsn.getRegisterB());
    assertEquals(regNIndex, asmInsn.getRegisterC());
    assertEquals(Opcode.AGET_OBJECT, asmInsn.opcode);
  }

  @Test(expected=InstructionAssemblyException.class)
  public void testAssemble_ArrayGet_WrongAllocation_RegisterTo() {
    val regNTo = Utils.numFitsInto_Unsigned(9);
    val regNArray = Utils.numFitsInto_Unsigned(8) - 1;
    val regNIndex = Utils.numFitsInto_Unsigned(8) - 2;
    val regTo = new DexRegister(regNTo);
    val regArray = new DexRegister(regNArray);
    val regIndex = new DexRegister(regNIndex);
    val regAlloc = Utils.genRegAlloc(regTo, regArray, regIndex);

    val insn = new DexInstruction_ArrayGet(
      null,
      regTo,
      regArray,
      regIndex,
      Opcode_GetPut.Object);

    insn.assembleBytecode(Utils.genAsmState(regAlloc));
  }

  @Test(expected=InstructionAssemblyException.class)
  public void testAssemble_ArrayGet_WrongAllocation_RegisterArray() {
    val regNTo = Utils.numFitsInto_Unsigned(8);
    val regNArray = Utils.numFitsInto_Unsigned(9);
    val regNIndex = Utils.numFitsInto_Unsigned(8) - 2;
    val regTo = new DexRegister(regNTo);
    val regArray = new DexRegister(regNArray);
    val regIndex = new DexRegister(regNIndex);
    val regAlloc = Utils.genRegAlloc(regTo, regArray, regIndex);

    val insn = new DexInstruction_ArrayGet(
      null,
      regTo,
      regArray,
      regIndex,
      Opcode_GetPut.Object);

    insn.assembleBytecode(Utils.genAsmState(regAlloc));
  }

  @Test(expected=InstructionAssemblyException.class)
  public void testAssemble_ArrayGet_WrongAllocation_RegisterIndex() {
    val regNTo = Utils.numFitsInto_Unsigned(8);
    val regNArray = Utils.numFitsInto_Unsigned(8);
    val regNIndex = Utils.numFitsInto_Unsigned(9);
    val regTo = new DexRegister(regNTo);
    val regArray = new DexRegister(regNArray);
    val regIndex = new DexRegister(regNIndex);
    val regAlloc = Utils.genRegAlloc(regTo, regArray, regIndex);

    val insn = new DexInstruction_ArrayGet(
      null,
      regTo,
      regArray,
      regIndex,
      Opcode_GetPut.Object);

    insn.assembleBytecode(Utils.genAsmState(regAlloc));
  }
}
