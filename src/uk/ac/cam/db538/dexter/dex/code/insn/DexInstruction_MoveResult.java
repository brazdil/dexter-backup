package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction11x;

import uk.ac.cam.db538.dexter.analysis.coloring.ColorRange;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_AssemblingState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;

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
      throw new InstructionParsingException("Unknown instruction format or opcode");
  }

  @Override
  public String getOriginalAssembly() {
    return "move-result" + (objectMoving ? "-object" : "") +
           " v" + regTo.getOriginalIndexString();
  }

  @Override
  public Instruction[] assembleBytecode(DexCode_AssemblingState state) {
    int rTo = state.getRegisterAllocation().get(regTo);
    val opcode = objectMoving ? Opcode.MOVE_RESULT_OBJECT : Opcode.MOVE_RESULT;

    if (fitsIntoBits_Unsigned(rTo, 8))
      return new Instruction[] {
               new Instruction11x(opcode, (short) rTo)
             };
    else
      return throwNoSuitableFormatFound();
  }

  @Override
  public Set<DexRegister> lvaDefinedRegisters() {
    return createSet(regTo);
  }

  @Override
  public Set<GcRangeConstraint> gcRangeConstraints() {
    return createSet(new GcRangeConstraint(regTo, ColorRange.RANGE_8BIT));
  }

  @Override
  protected DexCodeElement gcReplaceWithTemporaries(Map<DexRegister, DexRegister> mapping) {
    return new DexInstruction_MoveResult(getMethodCode(), mapping.get(regTo), objectMoving);
  }
}
