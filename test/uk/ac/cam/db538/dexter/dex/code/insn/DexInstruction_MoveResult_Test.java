package uk.ac.cam.db538.dexter.dex.code.insn;

import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction11x;
import org.junit.Test;

import uk.ac.cam.db538.dexter.dex.code.Utils;

public class DexInstruction_MoveResult_Test {

  @Test
  public void testParse_Primitive() {
    Utils.parseAndCompare(new Instruction11x(Opcode.MOVE_RESULT, (short) 255),
                          "move-result v255");
  }

  @Test
  public void testParse_Object() {
    Utils.parseAndCompare(new Instruction11x(Opcode.MOVE_RESULT_OBJECT, (short) 254),
                          "move-result-object v254");
  }
}
