package uk.ac.cam.db538.dexter.dex.code.insn;

import lombok.val;

import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction10x;
import org.junit.Test;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.Utils;

public class DexInstruction_ReturnVoid_Test {

  @Test
  public void testParse() {
    Utils.parseAndCompare(
      new Instruction10x(Opcode.RETURN_VOID),
      "return-void");
  }

  @Test
  public void testInstrument() {
    val code = new DexCode();
    code.add(new DexInstruction_ReturnVoid(null));

    Utils.instrumentAndCompare(
      code,
      new String[] {
        "return-void"
      });
  }
}
