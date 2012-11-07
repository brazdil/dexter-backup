package uk.ac.cam.db538.dexter.dex.code.insn;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction10x;

import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.reg.RegisterAllocation;

public class DexInstruction_Nop extends DexInstruction {

  public DexInstruction_Nop() {
  }

  public DexInstruction_Nop(Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException {
    if (!(insn instanceof Instruction10x) || insn.opcode != Opcode.NOP) {
      System.out.println(insn.getClass().getName());
      System.out.println(insn.opcode.name());
      throw new InstructionParsingException("Unknown instruction format or opcode");
    }
  }

  @Override
  public String getOriginalAssembly() {
    return "nop";
  }

  @Override
  public Instruction[] assembleBytecode(RegisterAllocation regAlloc)
  throws InstructionAssemblyException {
    return new Instruction[] {
             new Instruction10x(Opcode.NOP)
           };
  }
}
