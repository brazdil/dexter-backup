package uk.ac.cam.db538.dexter.dex.code.insn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import lombok.val;

import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction10x;
import org.junit.Test;

import uk.ac.cam.db538.dexter.dex.DexParsingCache;
import uk.ac.cam.db538.dexter.dex.code.DexCode;

public class DexInstruction_ReturnVoid_Test {

  @Test
  public void testParse() {
    Utils.parseAndCompare(
      new Instruction10x(Opcode.RETURN_VOID),
      "return-void");
  }

  @Test
  public void testAssemble() {
    val insn = new DexInstruction_ReturnVoid(null);

    val asm = insn.assembleBytecode(null);
    assertEquals(1, asm.length);
    assertTrue(asm[0] instanceof Instruction10x);

    val asmInsn = (Instruction10x) asm[0];
    assertEquals(Opcode.RETURN_VOID, asmInsn.opcode);
  }

  @Test
  public void testInstrument() {
    val code = new DexCode(new DexParsingCache());
    code.add(new DexInstruction_ReturnVoid(null));

    Utils.instrumentAndCompare(
      code,
      new String[] {
        "return-void"
      });
  }
}
