package uk.ac.cam.db538.dexter.dex.code.insn;

import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction22c;
import org.junit.Test;

import uk.ac.cam.db538.dexter.dex.code.Utils;

public class DexInstruction_NewArray_Test {

  @Test
  public void testParse_NewArray() throws InstructionParsingException {
    Utils.parseAndCompare(
      new Instruction22c(Opcode.NEW_ARRAY, (byte) 4, (byte) 8, Utils.getTypeItem("[I")),
      "new-array v4, v8, [I");
  }
}
