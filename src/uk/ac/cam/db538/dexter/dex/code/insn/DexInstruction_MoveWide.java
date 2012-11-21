package uk.ac.cam.db538.dexter.dex.code.insn;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction12x;
import org.jf.dexlib.Code.Format.Instruction22x;
import org.jf.dexlib.Code.Format.Instruction32x;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;

import lombok.Getter;
import lombok.val;

public class DexInstruction_MoveWide extends DexInstruction {

  @Getter private final DexRegister RegTo1;
  @Getter private final DexRegister RegTo2;
  @Getter private final DexRegister RegFrom1;
  @Getter private final DexRegister RegFrom2;

  public DexInstruction_MoveWide(DexCode methodCode, DexRegister to1, DexRegister to2, DexRegister from1, DexRegister from2) {
    super(methodCode);

    RegTo1 = to1;
    RegTo2 = to2;
    RegFrom1 = from1;
    RegFrom2 = from2;
  }

  public DexInstruction_MoveWide(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException {
    super(methodCode);

    int regA, regB;

    if (insn instanceof Instruction12x && insn.opcode == Opcode.MOVE_WIDE) {

      val insnMoveWide = (Instruction12x) insn;
      regA = insnMoveWide.getRegisterA();
      regB = insnMoveWide.getRegisterB();

    } else if (insn instanceof Instruction22x && insn.opcode == Opcode.MOVE_WIDE_FROM16) {

      val insnMoveWideFrom16 = (Instruction22x) insn;
      regA = insnMoveWideFrom16.getRegisterA();
      regB = insnMoveWideFrom16.getRegisterB();

    } else if (insn instanceof Instruction32x && insn.opcode == Opcode.MOVE_WIDE_16) {

      val insnMoveWide16 = (Instruction32x) insn;
      regA = insnMoveWide16.getRegisterA();
      regB = insnMoveWide16.getRegisterB();

    } else
      throw new InstructionParsingException("Unknown instruction format or opcode");

    RegTo1 = parsingState.getRegister(regA);
    RegTo2 = parsingState.getRegister(regA + 1);
    RegFrom1 = parsingState.getRegister(regB);
    RegFrom2 = parsingState.getRegister(regB + 1);
  }

  @Override
  public String getOriginalAssembly() {
    return "move-wide v" + RegTo1.getId() + ", v" + RegFrom1.getId();
  }
}
