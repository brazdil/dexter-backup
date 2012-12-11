package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction12x;
import org.jf.dexlib.Code.Format.Instruction22x;
import org.jf.dexlib.Code.Format.Instruction32x;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_AssemblingState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;

public class DexInstruction_Move extends DexInstruction {

  @Getter private final DexRegister regTo;
  @Getter private final DexRegister regFrom;
  @Getter private final boolean objectMoving;

  // CAREFUL: registers can only be allocated to 0-15 regular move !!!

  public DexInstruction_Move(DexCode methodCode, DexRegister to, DexRegister from, boolean objectMoving) {
    super(methodCode);

    this.regTo = to;
    this.regFrom = from;
    this.objectMoving = objectMoving;
  }

  public DexInstruction_Move(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException {
    super(methodCode);

    int regA, regB;

    if (insn instanceof Instruction12x &&
        (insn.opcode == Opcode.MOVE || insn.opcode == Opcode.MOVE_OBJECT)) {

      val insnMove = (Instruction12x) insn;
      regA = insnMove.getRegisterA();
      regB = insnMove.getRegisterB();
      objectMoving = insn.opcode == Opcode.MOVE_OBJECT;

    } else if (insn instanceof Instruction22x &&
               (insn.opcode == Opcode.MOVE_FROM16 || insn.opcode == Opcode.MOVE_OBJECT_FROM16)) {

      val insnMoveFrom16 = (Instruction22x) insn;
      regA = insnMoveFrom16.getRegisterA();
      regB = insnMoveFrom16.getRegisterB();
      objectMoving = insn.opcode == Opcode.MOVE_OBJECT_FROM16;

    } else if (insn instanceof Instruction32x &&
               (insn.opcode == Opcode.MOVE_16 || insn.opcode == Opcode.MOVE_OBJECT_16)) {

      val insnMove16 = (Instruction32x) insn;
      regA = insnMove16.getRegisterA();
      regB = insnMove16.getRegisterB();
      objectMoving = insn.opcode == Opcode.MOVE_OBJECT_16;

    } else
      throw new InstructionParsingException("Unknown instruction format or opcode");

    regTo = parsingState.getRegister(regA);
    regFrom = parsingState.getRegister(regB);
  }

  @Override
  public String getOriginalAssembly() {
    return "move" + (objectMoving ? "-object" : "") +
           " v" + regTo.getOriginalIndexString() + ", v" + regFrom.getOriginalIndexString();
  }

  @Override
  public Set<DexRegister> lvaDefinedRegisters() {
    return createSet(regTo);
  }

  @Override
  public Set<DexRegister> lvaReferencedRegisters() {
    return createSet(regFrom);
  }

  @Override
  public Instruction[] assembleBytecode(DexCode_AssemblingState state) {
    val regAlloc = state.getRegisterAllocation();
    int rTo = regAlloc.get(regTo);
    int rFrom = regAlloc.get(regFrom);

    if (rTo == rFrom)
      return new Instruction[0];

    if (fitsIntoBits_Unsigned(rTo, 4) && fitsIntoBits_Unsigned(rFrom, 4))
      return new Instruction[] {
               objectMoving ?
               new Instruction12x(Opcode.MOVE_OBJECT, (byte) rTo, (byte) rFrom) :
               new Instruction12x(Opcode.MOVE, (byte) rTo, (byte) rFrom)
             };
    else if (fitsIntoBits_Unsigned(rTo, 8))
      return new Instruction[] {
               objectMoving ?
               new Instruction22x(Opcode.MOVE_OBJECT_FROM16, (short) rTo, rFrom) :
               new Instruction22x(Opcode.MOVE_FROM16, (short) rTo, rFrom)
             };
    else
      return new Instruction[] {
               objectMoving ?
               new Instruction32x(Opcode.MOVE_OBJECT_16, rTo, rFrom) :
               new Instruction32x(Opcode.MOVE_16, rTo, rFrom)
             };
  }
}
