package uk.ac.cam.db538.dexter.dex.code;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction12x;
import org.jf.dexlib.Code.Format.Instruction22x;
import org.jf.dexlib.Code.Format.Instruction32x;

import lombok.Getter;
import lombok.val;

public class DexInstruction_Move extends DexInstruction {

  @Getter private final DexRegister RegTo;
  @Getter private final DexRegister RegFrom;
  @Getter private final boolean ObjectMoving;

  // CAREFUL: registers can only be allocated to 0-15 regular move !!!

  public DexInstruction_Move(DexRegister to, DexRegister from, boolean objectMoving) {
    RegTo = to;
    RegFrom = from;
    ObjectMoving = objectMoving;
  }

  public DexInstruction_Move(Instruction insn, InstructionParsingState parsingState) throws DexInstructionParsingException {
    if ( insn instanceof Instruction12x &&
         (insn.opcode == Opcode.MOVE || insn.opcode == Opcode.MOVE_OBJECT)) {

      val insnMove = (Instruction12x) insn;
      RegTo = parsingState.getRegister(insnMove.getRegisterA());
      RegFrom = parsingState.getRegister(insnMove.getRegisterB());
      ObjectMoving = insn.opcode == Opcode.MOVE_OBJECT;

    } else if (insn instanceof Instruction22x &&
               (insn.opcode == Opcode.MOVE_FROM16 || insn.opcode == Opcode.MOVE_OBJECT_FROM16)) {

      val insnMoveFrom16 = (Instruction22x) insn;
      RegTo = parsingState.getRegister(insnMoveFrom16.getRegisterA());
      RegFrom = parsingState.getRegister(insnMoveFrom16.getRegisterB());
      ObjectMoving = insn.opcode == Opcode.MOVE_OBJECT_FROM16;

    } else if (insn instanceof Instruction32x &&
               (insn.opcode == Opcode.MOVE_16 || insn.opcode == Opcode.MOVE_OBJECT_16)) {

      val insnMove16 = (Instruction32x) insn;
      RegTo = parsingState.getRegister(insnMove16.getRegisterA());
      RegFrom = parsingState.getRegister(insnMove16.getRegisterB());
      ObjectMoving = insn.opcode == Opcode.MOVE_OBJECT_16;

    } else
      throw new DexInstructionParsingException("Unknown instruction format or opcode");
  }

  @Override
  public String getOriginalAssembly() {
    return "move" + (ObjectMoving ? "-object" : "") +
           " v" + RegTo.getId() + ", v" + RegFrom.getId();
  }
}
