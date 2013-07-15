package uk.ac.cam.db538.dexter.dex.code.insn;

import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction21c;
import org.junit.Test;

import uk.ac.cam.db538.dexter.dex.code.Utils;

public class DexInstruction_NewInstance_Test {

  @Test
  public void testParse_NewInstance() throws InstructionParseError {
    Utils.parseAndCompare(
      new Instruction21c(Opcode.NEW_INSTANCE, (short) 236, Utils.getTypeItem("Ljava/lang/String;")),
      "new-instance v236, Ljava/lang/String;");
  }
}
