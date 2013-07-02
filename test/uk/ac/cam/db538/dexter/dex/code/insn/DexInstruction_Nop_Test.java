package uk.ac.cam.db538.dexter.dex.code.insn;

import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction10x;
import org.junit.Test;

import uk.ac.cam.db538.dexter.dex.code.Utils;

public class DexInstruction_Nop_Test {

  @Test
  public void testParse() {
    Utils.parseAndCompare(
      new Instruction10x(Opcode.NOP),
      "nop");
  }

}
