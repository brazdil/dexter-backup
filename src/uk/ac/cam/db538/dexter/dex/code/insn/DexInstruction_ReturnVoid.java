package uk.ac.cam.db538.dexter.dex.code.insn;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;

public class DexInstruction_ReturnVoid extends DexInstruction {

  public DexInstruction_ReturnVoid() {
  }

  public DexInstruction_ReturnVoid(Instruction insn, InstructionParsingState parsingState) throws DexInstructionParsingException {
    if (insn.opcode != Opcode.RETURN_VOID)
      throw new DexInstructionParsingException("Unknown instruction format or opcode");
  }

  @Override
  public String getOriginalAssembly() {
    return "return-void";
  }
}
