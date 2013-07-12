package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction11x;

import uk.ac.cam.db538.dexter.dex.code.CodeParserState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.reg.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.reg.DexSingleRegister;
import uk.ac.cam.db538.dexter.hierarchy.RuntimeHierarchy;

import com.google.common.collect.Sets;

public class DexInstruction_MoveException extends DexInstruction {

  @Getter private final DexSingleRegister regTo;

  public DexInstruction_MoveException(DexSingleRegister regTo, RuntimeHierarchy hierarchy) {
	super(hierarchy);
	  
    this.regTo = regTo;
  }

  public static DexInstruction_MoveException parse(Instruction insn, CodeParserState parsingState) {
    if (insn instanceof Instruction11x && insn.opcode == Opcode.MOVE_EXCEPTION) {

      val insnMoveException = (Instruction11x) insn;
      return new DexInstruction_MoveException(
    		  parsingState.getSingleRegister(insnMoveException.getRegisterA()),
    		  parsingState.getHierarchy());

    } else
      throw FORMAT_EXCEPTION;
  }


  @Override
  public String toString() {
    return "move-exception " + regTo.toString();
  }

  @Override
  public Set<? extends DexRegister> lvaDefinedRegisters() {
    return Sets.newHashSet(regTo);
  }

  @Override
  public void instrument(DexCode_InstrumentationState state) { }

  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }
}
