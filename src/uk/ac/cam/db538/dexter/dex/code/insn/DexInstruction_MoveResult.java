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

import com.google.common.collect.Sets;

public class DexInstruction_MoveResult extends DexInstruction {

  @Getter private final DexRegister regTo;
  @Getter private final Opcode_Move opcode;

  public DexInstruction_MoveResult(DexRegister regTo, Opcode_Move opcode) {
    this.regTo = regTo;
    this.opcode = opcode;
    
    this.opcode.checkRegisterType(this.regTo);    
  }

  public DexInstruction_MoveResult(Instruction insn, CodeParserState parsingState) {
    if (insn instanceof Instruction11x &&
        (insn.opcode == Opcode.MOVE_RESULT || insn.opcode == Opcode.MOVE_RESULT_WIDE || insn.opcode == Opcode.MOVE_RESULT_OBJECT)) {

      val insnMoveResult = (Instruction11x) insn;
      this.opcode = Opcode_Move.convert(insn.opcode);
      if (this.opcode == Opcode_Move.Wide)
    	  regTo = parsingState.getWideRegister(insnMoveResult.getRegisterA());
      else
    	  regTo = parsingState.getSingleRegister(insnMoveResult.getRegisterA());

    } else
      throw FORMAT_EXCEPTION;
  }

  public DexInstruction_MoveResult(DexInstruction_MoveResult toClone) {
    this(toClone.regTo, toClone.opcode);
  }

  @Override
  public String toString() {
	return opcode.getAssemblyName_Result() + " " + regTo.toString();
  }
  
  @Override
  public void instrument(DexCode_InstrumentationState state) {
	  throw new Error("MoveResult instruction is not meant to be instrumented directly");
  }

  @Override
  public Set<DexRegister> lvaDefinedRegisters() {
    return Sets.newHashSet(regTo);
  }

  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }
}
