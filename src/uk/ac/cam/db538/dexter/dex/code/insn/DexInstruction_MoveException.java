package uk.ac.cam.db538.dexter.dex.code.insn;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction11x;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;

import lombok.Getter;
import lombok.val;

public class DexInstruction_MoveException extends DexInstruction {

  @Getter private final DexRegister RegTo;

  public DexInstruction_MoveException(DexCode methodCode, DexRegister to) {
    super(methodCode);

    RegTo = to;
  }

  public DexInstruction_MoveException(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException {
    super(methodCode);

    if (insn instanceof Instruction11x && insn.opcode == Opcode.MOVE_EXCEPTION) {

      val insnMoveException = (Instruction11x) insn;
      RegTo = parsingState.getRegister(insnMoveException.getRegisterA());

    } else
      throw new InstructionParsingException("Unknown instruction format or opcode");
  }


  @Override
  public String getOriginalAssembly() {
    return "move-exception v" + RegTo.getId();
  }
}
