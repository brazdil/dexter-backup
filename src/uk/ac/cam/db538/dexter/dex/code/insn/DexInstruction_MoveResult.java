package uk.ac.cam.db538.dexter.dex.code.insn;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction11x;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;

import lombok.Getter;
import lombok.val;

public class DexInstruction_MoveResult extends DexInstruction {

  @Getter private final DexRegister RegTo;
  @Getter private final boolean ObjectMoving;

  public DexInstruction_MoveResult(DexCode methodCode, DexRegister to, boolean objectMoving) {
    super(methodCode);

    RegTo = to;
    ObjectMoving = objectMoving;
  }

  public DexInstruction_MoveResult(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException {
    super(methodCode);

    if (insn instanceof Instruction11x &&
        (insn.opcode == Opcode.MOVE_RESULT || insn.opcode == Opcode.MOVE_RESULT_OBJECT)) {

      val insnMoveResult = (Instruction11x) insn;
      RegTo = parsingState.getRegister(insnMoveResult.getRegisterA());
      ObjectMoving = insn.opcode == Opcode.MOVE_RESULT_OBJECT;

    } else
      throw new InstructionParsingException("Unknown instruction format or opcode");
  }

  @Override
  public String getOriginalAssembly() {
    return "move-result" + (ObjectMoving ? "-object" : "") +
           " v" + RegTo.getOriginalIndexString();
  }
}
