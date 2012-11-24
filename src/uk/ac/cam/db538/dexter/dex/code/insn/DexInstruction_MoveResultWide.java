package uk.ac.cam.db538.dexter.dex.code.insn;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction11x;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;

import lombok.Getter;
import lombok.val;

public class DexInstruction_MoveResultWide extends DexInstruction {

  @Getter private final DexRegister regTo1;
  @Getter private final DexRegister regTo2;

  public DexInstruction_MoveResultWide(DexCode methodCode, DexRegister to1, DexRegister to2) {
    super(methodCode);

    regTo1 = to1;
    regTo2 = to2;
  }

  public DexInstruction_MoveResultWide(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException {
    super(methodCode);

    if (insn instanceof Instruction11x && insn.opcode == Opcode.MOVE_RESULT_WIDE) {

      val insnMove = (Instruction11x) insn;
      regTo1 = parsingState.getRegister(insnMove.getRegisterA());
      regTo2 = parsingState.getRegister(insnMove.getRegisterA() + 1);

    } else
      throw new InstructionParsingException("Unknown instruction format or opcode");
  }

  @Override
  public String getOriginalAssembly() {
    return "move-result-wide v" + regTo1.getOriginalIndexString();
  }
}
