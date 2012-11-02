package uk.ac.cam.db538.dexter.dex.code.insn;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction11x;

import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.reg.DexRegister;

import lombok.Getter;
import lombok.val;

public class DexInstruction_Throw extends DexInstruction {

  @Getter private final DexRegister RegFrom;

  public DexInstruction_Throw(DexRegister from) {
    RegFrom = from;
  }

  public DexInstruction_Throw(Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException {
    if (insn instanceof Instruction11x && insn.opcode == Opcode.THROW) {

      val insnThrow = (Instruction11x) insn;
      RegFrom = parsingState.getRegister(insnThrow.getRegisterA());

    } else
      throw new InstructionParsingException("Unknown instruction format or opcode");
  }

  @Override
  public String getOriginalAssembly() {
    return "throw v" + RegFrom.getId();
  }
}
