package uk.ac.cam.db538.dexter.dex.code.insn;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction12x;
import org.jf.dexlib.Code.Format.Instruction22x;
import org.jf.dexlib.Code.Format.Instruction32x;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.reg.DexRegister;

import lombok.Getter;
import lombok.val;

public class DexInstruction_Move extends DexInstruction {

  @Getter private final DexRegister RegTo;
  @Getter private final DexRegister RegFrom;
  @Getter private final boolean ObjectMoving;

  // CAREFUL: registers can only be allocated to 0-15 regular move !!!

  public DexInstruction_Move(DexCode methodCode, DexRegister to, DexRegister from, boolean objectMoving) {
    super(methodCode);

    RegTo = to;
    RegFrom = from;
    ObjectMoving = objectMoving;
  }

  public DexInstruction_Move(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException {
    super(methodCode);

    int regA, regB;

    if (insn instanceof Instruction12x &&
        (insn.opcode == Opcode.MOVE || insn.opcode == Opcode.MOVE_OBJECT)) {

      val insnMove = (Instruction12x) insn;
      regA = insnMove.getRegisterA();
      regB = insnMove.getRegisterB();
      ObjectMoving = insn.opcode == Opcode.MOVE_OBJECT;

    } else if (insn instanceof Instruction22x &&
               (insn.opcode == Opcode.MOVE_FROM16 || insn.opcode == Opcode.MOVE_OBJECT_FROM16)) {

      val insnMoveFrom16 = (Instruction22x) insn;
      regA = insnMoveFrom16.getRegisterA();
      regB = insnMoveFrom16.getRegisterB();
      ObjectMoving = insn.opcode == Opcode.MOVE_OBJECT_FROM16;

    } else if (insn instanceof Instruction32x &&
               (insn.opcode == Opcode.MOVE_16 || insn.opcode == Opcode.MOVE_OBJECT_16)) {

      val insnMove16 = (Instruction32x) insn;
      regA = insnMove16.getRegisterA();
      regB = insnMove16.getRegisterB();
      ObjectMoving = insn.opcode == Opcode.MOVE_OBJECT_16;

    } else
      throw new InstructionParsingException("Unknown instruction format or opcode");

    RegTo = parsingState.getRegister(regA);
    RegFrom = parsingState.getRegister(regB);
  }

  @Override
  public String getOriginalAssembly() {
    return "move" + (ObjectMoving ? "-object" : "") +
           " v" + RegTo.getId() + ", v" + RegFrom.getId();
  }
}
