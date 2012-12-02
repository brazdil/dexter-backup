package uk.ac.cam.db538.dexter.dex.code.insn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import lombok.val;

import org.jf.dexlib.TypeIdItem;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction22c;
import org.junit.Test;

import uk.ac.cam.db538.dexter.dex.DexParsingCache;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.Utils;
import uk.ac.cam.db538.dexter.dex.type.DexArrayType;

public class DexInstruction_NewArray_Test {

  @Test
  public void testParse_NewArray() throws InstructionParsingException {
    Utils.parseAndCompare(
      new Instruction22c(Opcode.NEW_ARRAY, (byte) 4, (byte) 8, Utils.getTypeItem("[I")),
      "new-array v4, v8, [I");
  }

  @Test
  public void testAssemble_NewArray() {
    val cache = new DexParsingCache();

    val regNTo = Utils.numFitsInto_Unsigned(4);
    val regNSize = Utils.numFitsInto_Unsigned(4) - 1;
    val regTo = new DexRegister(regNTo);
    val regSize = new DexRegister(regNSize);
    val regAlloc = Utils.genRegAlloc(regTo, regSize);

    val insn = new DexInstruction_NewArray(null, regTo, regSize, DexArrayType.parse("[I", cache));

    val asm = insn.assembleBytecode(Utils.genAsmState(regAlloc));
    assertEquals(1, asm.length);
    assertTrue(asm[0] instanceof Instruction22c);

    val asmInsn = (Instruction22c) asm[0];
    assertEquals(regNTo, asmInsn.getRegisterA());
    assertEquals(regNSize, asmInsn.getRegisterB());
    assertEquals("[I", ((TypeIdItem) asmInsn.getReferencedItem()).getTypeDescriptor());
  }

  @Test(expected=InstructionAssemblyException.class)
  public void testAssemble_NewInstance_WrongAllocation_RegTo() {
    val cache = new DexParsingCache();

    val regNTo = Utils.numFitsInto_Unsigned(5);
    val regNSize = Utils.numFitsInto_Unsigned(4) - 1;
    val regTo = new DexRegister(regNTo);
    val regSize = new DexRegister(regNSize);
    val regAlloc = Utils.genRegAlloc(regTo, regSize);

    val insn = new DexInstruction_NewArray(null, regTo, regSize, DexArrayType.parse("[I", cache));

    insn.assembleBytecode(Utils.genAsmState(regAlloc));
  }

  @Test(expected=InstructionAssemblyException.class)
  public void testAssemble_NewInstance_WrongAllocation_RegSize() {
    val cache = new DexParsingCache();

    val regNTo = Utils.numFitsInto_Unsigned(4);
    val regNSize = Utils.numFitsInto_Unsigned(5) - 1;
    val regTo = new DexRegister(regNTo);
    val regSize = new DexRegister(regNSize);
    val regAlloc = Utils.genRegAlloc(regTo, regSize);

    val insn = new DexInstruction_NewArray(null, regTo, regSize, DexArrayType.parse("[I", cache));

    insn.assembleBytecode(Utils.genAsmState(regAlloc));
  }
}
