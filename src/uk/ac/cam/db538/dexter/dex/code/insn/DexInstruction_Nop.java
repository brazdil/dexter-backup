package uk.ac.cam.db538.dexter.dex.code.insn;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;

public class DexInstruction_Nop extends DexInstruction {

  public DexInstruction_Nop() {
  }

  public DexInstruction_Nop(Instruction insn, ParsingState parsingState) throws InstructionParsingException {
    if (insn.opcode != Opcode.NOP)
      throw new InstructionParsingException("Unknown instruction format or opcode");
  }

  @Override
  public String getOriginalAssembly() {
    return "nop";
  }
}
