package uk.ac.cam.db538.dexter.dex.code.insn;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction11x;
import org.junit.Test;

import uk.ac.cam.db538.dexter.dex.code.Utils;

public class DexInstruction_Return_Test {

  @Test
  public void testParse() {
    Utils.parseAndCompare(
      new Instruction[] {
        new Instruction11x(Opcode.RETURN, (short) 255),
        new Instruction11x(Opcode.RETURN_OBJECT, (short) 254)
      }, new String[] {
        "return v255",
        "return-object v254"
      });
  }
}
