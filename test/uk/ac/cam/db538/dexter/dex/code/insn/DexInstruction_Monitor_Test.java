package uk.ac.cam.db538.dexter.dex.code.insn;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction11x;
import org.junit.Test;

import uk.ac.cam.db538.dexter.dex.code.Utils;

public class DexInstruction_Monitor_Test {

  @Test
  public void testParse() {
    Utils.parseAndCompare(
      new Instruction[] {
        new Instruction11x(Opcode.MONITOR_ENTER, (short) 255),
        new Instruction11x(Opcode.MONITOR_EXIT, (short) 254)
      }, new String[] {
        "monitor-enter v255",
        "monitor-exit v254"
      });
  }
}
