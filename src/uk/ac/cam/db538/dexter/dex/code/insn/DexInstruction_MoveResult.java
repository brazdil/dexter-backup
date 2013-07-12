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
import uk.ac.cam.db538.dexter.dex.code.reg.DexWideRegister;
import uk.ac.cam.db538.dexter.dex.code.reg.RegisterType;
import uk.ac.cam.db538.dexter.hierarchy.RuntimeHierarchy;

import com.google.common.collect.Sets;

public class DexInstruction_MoveResult extends DexInstruction {

  @Getter private final DexRegister regTo;
  @Getter private final RegisterType type;

  public DexInstruction_MoveResult(DexSingleRegister regTo, boolean objectMoving, RuntimeHierarchy hierarchy) {
	super(hierarchy);
	
    this.regTo = regTo;
    this.type = objectMoving ? RegisterType.REFERENCE : RegisterType.SINGLE_PRIMITIVE;
  }

  public DexInstruction_MoveResult(DexWideRegister regTo, RuntimeHierarchy hierarchy) {
	super(hierarchy);
	
    this.regTo = regTo;
    this.type = RegisterType.WIDE_PRIMITIVE;
  }

  public DexInstruction_MoveResult(DexInstruction_MoveResult toClone) {
    super(toClone.hierarchy);
	  
    this.regTo = toClone.regTo;
    this.type = toClone.type;
  }

  public static DexInstruction_MoveResult parse(Instruction insn, CodeParserState parsingState) {
    if (insn instanceof Instruction11x &&
        (insn.opcode == Opcode.MOVE_RESULT || insn.opcode == Opcode.MOVE_RESULT_WIDE || insn.opcode == Opcode.MOVE_RESULT_OBJECT)) {

      val insnMoveResult = (Instruction11x) insn;
      val opcode = RegisterType.fromOpcode(insn.opcode);
      if (opcode == RegisterType.WIDE_PRIMITIVE)
    	  return new DexInstruction_MoveResult(
    			  parsingState.getWideRegister(insnMoveResult.getRegisterA()),
    			  parsingState.getHierarchy());
      else
    	  return new DexInstruction_MoveResult(
    			  parsingState.getSingleRegister(insnMoveResult.getRegisterA()),
    			  opcode == RegisterType.REFERENCE,
    			  parsingState.getHierarchy());

    } else
      throw FORMAT_EXCEPTION;
  }

  @Override
  public String toString() {
	return "move-result" + type.getAsmSuffix() + " " + regTo.toString();
  }
  
  @Override
  public void instrument(DexCode_InstrumentationState state) {
	  throw new Error("MoveResult instruction is not meant to be instrumented directly");
  }

  @Override
  public Set<? extends DexRegister> lvaDefinedRegisters() {
    return Sets.newHashSet(regTo);
  }

  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }
}
