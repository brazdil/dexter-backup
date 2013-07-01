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
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;

public class DexInstruction_MoveWide extends DexInstruction {

  @Getter private final DexRegister regTo1;
  @Getter private final DexRegister regTo2;
  @Getter private final DexRegister regFrom1;
  @Getter private final DexRegister regFrom2;

  public DexInstruction_MoveWide(DexCode methodCode, DexRegister to1, DexRegister to2, DexRegister from1, DexRegister from2) {
    super(methodCode);

    regTo1 = to1;
    regTo2 = to2;
    regFrom1 = from1;
    regFrom2 = from2;
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
      throw FORMAT_EXCEPTION;

    regTo1 = parsingState.getRegister(regA);
    regTo2 = parsingState.getRegister(regA + 1);
    regFrom1 = parsingState.getRegister(regB);
    regFrom2 = parsingState.getRegister(regB + 1);
  }

  @Override
  public String getOriginalAssembly() {
    return "move-wide " + regTo1.getOriginalIndexString() + "|" + regTo2.getOriginalIndexString()
           + ", " + regFrom1.getOriginalIndexString() + "|" + regFrom2.getOriginalIndexString();
  }

  @Override
  public Instruction[] assembleBytecode(DexCode_AssemblingState state) {
    val regAlloc = state.getRegisterAllocation();
    int rTo1 = regAlloc.get(regTo1);
    int rTo2 = regAlloc.get(regTo2);
    int rFrom1 = regAlloc.get(regFrom1);
    int rFrom2 = regAlloc.get(regFrom2);

    if (!formWideRegister(rTo1, rTo2) || !formWideRegister(rFrom1, rFrom2))
      return throwWideRegistersExpected();

    if (fitsIntoBits_Unsigned(rTo1, 4) && fitsIntoBits_Unsigned(rFrom1, 4))
      return new Instruction[] {
               new Instruction12x(Opcode.MOVE_WIDE, (byte) rTo1, (byte) rFrom1)
             };
    else if (fitsIntoBits_Unsigned(rTo1, 8)) // rFrom1 fits into 16 bits
      return new Instruction[] {
               new Instruction22x(Opcode.MOVE_WIDE_FROM16, (short) rTo1, rFrom1)
             };
    else // both fit into 16 bits
      return new Instruction[] {
               new Instruction32x(Opcode.MOVE_WIDE_16, rTo1, rFrom1)
             };
  }

  @Override
  public Set<DexRegister> lvaDefinedRegisters() {
    return createSet(regTo1, regTo2);
  }

  @Override
  public Set<DexRegister> lvaReferencedRegisters() {
    return createSet(regFrom1, regFrom2);
  }

  @Override
  public void instrument(DexCode_InstrumentationState state) {
    val code = getMethodCode();
    code.replace(this, new DexCodeElement[] {
                   this,
                   new DexInstruction_Move(code, state.getTaintRegister(regTo1), state.getTaintRegister(regFrom1), false)
                 });
  }

  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }
}
