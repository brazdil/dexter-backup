package uk.ac.cam.db538.dexter.dex.code;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction12x;
import org.jf.dexlib.Code.Format.Instruction22x;
import org.jf.dexlib.Code.Format.Instruction32x;

import lombok.Getter;
import lombok.val;

public class DexInstruction_MoveWide extends DexInstruction {

  @Getter private final DexRegister RegTo1;
  @Getter private final DexRegister RegTo2;
  @Getter private final DexRegister RegFrom1;
  @Getter private final DexRegister RegFrom2;

  public DexInstruction_MoveWide(DexRegister to1, DexRegister to2, DexRegister from1, DexRegister from2) {
    RegTo1 = to1;
    RegTo2 = to2;
    RegFrom1 = from1;
    RegFrom2 = from2;
  }

  public DexInstruction_MoveWide(Instruction insn, InstructionParsingState parsingState) throws DexInstructionParsingException {
    if ( insn instanceof Instruction12x && insn.opcode == Opcode.MOVE_WIDE) {

      val insnMoveWide = (Instruction12x) insn;
      RegTo1 = parsingState.getRegister(insnMoveWide.getRegisterA());
      RegTo2 = parsingState.getRegister(insnMoveWide.getRegisterA() + 1);
      RegFrom1 = parsingState.getRegister(insnMoveWide.getRegisterB());
      RegFrom2 = parsingState.getRegister(insnMoveWide.getRegisterB() + 1);

    } else if (insn instanceof Instruction22x && insn.opcode == Opcode.MOVE_WIDE_FROM16) {

      val insnMoveWideFrom16 = (Instruction22x) insn;
      RegTo1 = parsingState.getRegister(insnMoveWideFrom16.getRegisterA());
      RegTo2 = parsingState.getRegister(insnMoveWideFrom16.getRegisterA() + 1);
      RegFrom1 = parsingState.getRegister(insnMoveWideFrom16.getRegisterB());
      RegFrom2 = parsingState.getRegister(insnMoveWideFrom16.getRegisterB() + 1);

    } else if (insn instanceof Instruction32x && insn.opcode == Opcode.MOVE_WIDE_16) {

      val insnMoveWide16 = (Instruction32x) insn;
      RegTo1 = parsingState.getRegister(insnMoveWide16.getRegisterA());
      RegTo2 = parsingState.getRegister(insnMoveWide16.getRegisterA() + 1);
      RegFrom1 = parsingState.getRegister(insnMoveWide16.getRegisterB());
      RegFrom2 = parsingState.getRegister(insnMoveWide16.getRegisterB() + 1);

    } else
      throw new DexInstructionParsingException("Unknown instruction format or opcode");
  }

  @Override
  public String getOriginalAssembly() {
    return "move-wide v" + RegTo1.getId() + ", v" + RegFrom1.getId();
  }
}
