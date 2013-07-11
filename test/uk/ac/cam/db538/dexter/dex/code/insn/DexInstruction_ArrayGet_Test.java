package uk.ac.cam.db538.dexter.dex.code.insn;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction23x;
import org.junit.Test;

import uk.ac.cam.db538.dexter.dex.code.Utils;

public class DexInstruction_ArrayGet_Test {

  @Test
  public void testParse_ArrayGet() throws InstructionParseError {
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
}
