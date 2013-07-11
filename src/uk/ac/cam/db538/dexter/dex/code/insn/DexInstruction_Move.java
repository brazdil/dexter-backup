package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction12x;
import org.jf.dexlib.Code.Format.Instruction22x;
import org.jf.dexlib.Code.Format.Instruction32x;

import uk.ac.cam.db538.dexter.dex.code.CodeParserState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.reg.DexRegister;

import com.google.common.collect.Sets;

public class DexInstruction_Move extends DexInstruction {

  @Getter private final DexRegister regTo;
  @Getter private final DexRegister regFrom;
  @Getter private final Opcode_Move opcode;

  public DexInstruction_Move(DexRegister regTo, DexRegister regFrom, Opcode_Move opcode) {
    this.regTo = regTo;
    this.regFrom = regFrom;
    this.opcode = opcode;

    if (this.regTo.getClass() != this.regFrom.getClass())
    	throw new Error("Cannot move between different types of registers");

    this.opcode.checkRegisterType(this.regTo);
    this.opcode.checkRegisterType(this.regFrom);
  }

  public DexInstruction_Move(Instruction insn, CodeParserState parsingState) {
    int regA, regB;

    if (insn instanceof Instruction12x &&
        (insn.opcode == Opcode.MOVE || insn.opcode == Opcode.MOVE_WIDE || insn.opcode == Opcode.MOVE_OBJECT)) {

      val insnMove = (Instruction12x) insn;
      regA = insnMove.getRegisterA();
      regB = insnMove.getRegisterB();

    } else if (insn instanceof Instruction22x &&
               (insn.opcode == Opcode.MOVE_FROM16 || insn.opcode == Opcode.MOVE_WIDE_FROM16 || insn.opcode == Opcode.MOVE_OBJECT_FROM16)) {

      val insnMoveFrom16 = (Instruction22x) insn;
      regA = insnMoveFrom16.getRegisterA();
      regB = insnMoveFrom16.getRegisterB();

    } else if (insn instanceof Instruction32x &&
               (insn.opcode == Opcode.MOVE_16 || insn.opcode == Opcode.MOVE_WIDE_16 || insn.opcode == Opcode.MOVE_OBJECT_16)) {

      val insnMove16 = (Instruction32x) insn;
      regA = insnMove16.getRegisterA();
      regB = insnMove16.getRegisterB();

    } else
      throw FORMAT_EXCEPTION;
    
    this.opcode = Opcode_Move.convert(insn.opcode);

    if (this.opcode == Opcode_Move.Wide) {
    	regTo = parsingState.getWideRegister(regA);
    	regFrom = parsingState.getWideRegister(regB);
    } else {
    	regTo = parsingState.getSingleRegister(regA);
    	regFrom = parsingState.getSingleRegister(regB);
    }
  }

  @Override
  public String toString() {
	return opcode.getAssemblyName_Standard() + " " + regTo.toString() + ", " + regFrom.toString();
  }

  @Override
  public Set<DexRegister> lvaDefinedRegisters() {
    return Sets.newHashSet(regTo);
  }

  @Override
  public Set<DexRegister> lvaReferencedRegisters() {
    return Sets.newHashSet(regFrom);
  }

  @Override
  public void instrument(DexCode_InstrumentationState state) {
//    if (!objectMoving) {
//      val code = getMethodCode();
//      val taintRegFrom = state.getTaintRegister(regFrom);
//      val taintRegTo = state.getTaintRegister(regTo);
//
//      code.replace(this,
//                   new DexCodeElement[] {
//                     this,
//                     new DexInstruction_Move(code, taintRegTo, taintRegFrom, false)
//                   });
//    }
  }

  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }
}
