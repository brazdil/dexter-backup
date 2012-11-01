package uk.ac.cam.db538.dexter.dex.code;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction11x;

import lombok.Getter;
import lombok.val;

public class DexInstruction_MoveException extends DexInstruction {

  @Getter private final DexRegister RegTo;

  public DexInstruction_MoveException(DexRegister to) {
    RegTo = to;
  }

  public DexInstruction_MoveException(Instruction insn, InstructionParsingState parsingState) throws DexInstructionParsingException {
    if ( insn instanceof Instruction11x && insn.opcode == Opcode.MOVE_EXCEPTION) {

      val insnMoveException = (Instruction11x) insn;
      RegTo = parsingState.getRegister(insnMoveException.getRegisterA());

    } else
      throw new DexInstructionParsingException("Unknown instruction format or opcode");
  }


  @Override
  public String getOriginalAssembly() {
    return "move-exception v" + RegTo.getId();
  }
}
