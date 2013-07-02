package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction11x;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;

public class DexInstruction_MoveResult extends DexInstruction {

  @Getter private final DexRegister regTo;
  @Getter private final boolean objectMoving;

  public DexInstruction_MoveResult(DexCode methodCode, DexRegister to, boolean objectMoving) {
    super(methodCode);

    this.regTo = to;
    this.objectMoving = objectMoving;
  }

  public DexInstruction_MoveResult(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException {
    super(methodCode);

    if (insn instanceof Instruction11x &&
        (insn.opcode == Opcode.MOVE_RESULT || insn.opcode == Opcode.MOVE_RESULT_OBJECT)) {

      val insnMoveResult = (Instruction11x) insn;
      regTo = parsingState.getRegister(insnMoveResult.getRegisterA());
      objectMoving = insn.opcode == Opcode.MOVE_RESULT_OBJECT;

    } else
      throw FORMAT_EXCEPTION;
  }

  public DexInstruction_MoveResult(DexInstruction_MoveResult toClone) {
    this(toClone.getMethodCode(),
         toClone.regTo,
         toClone.objectMoving);

    this.setOriginalElement(toClone.isOriginalElement());
  }

  @Override
  public String getOriginalAssembly() {
    return "move-result" + (objectMoving ? "-object" : "") +
           " " + regTo.getOriginalIndexString();
  }

  @Override
  public Set<DexRegister> lvaDefinedRegisters() {
    return createSet(regTo);
  }

  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }
}
