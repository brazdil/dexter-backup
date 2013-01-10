package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.Map;
import java.util.Set;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction11x;

import uk.ac.cam.db538.dexter.analysis.coloring.ColorRange;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_AssemblingState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;

import lombok.Getter;
import lombok.val;

public class DexInstruction_MoveResultWide extends DexInstruction {

  @Getter private final DexRegister regTo1;
  @Getter private final DexRegister regTo2;

  public DexInstruction_MoveResultWide(DexCode methodCode, DexRegister to1, DexRegister to2) {
    super(methodCode);

    regTo1 = to1;
    regTo2 = to2;
  }

  public DexInstruction_MoveResultWide(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException {
    super(methodCode);

    if (insn instanceof Instruction11x && insn.opcode == Opcode.MOVE_RESULT_WIDE) {

      val insnMove = (Instruction11x) insn;
      regTo1 = parsingState.getRegister(insnMove.getRegisterA());
      regTo2 = parsingState.getRegister(insnMove.getRegisterA() + 1);

    } else
      throw FORMAT_EXCEPTION;
  }

  public DexInstruction_MoveResultWide(DexInstruction_MoveResultWide toClone) {
    this(toClone.getMethodCode(),
         toClone.regTo1,
         toClone.regTo2);

    this.setOriginalElement(toClone.isOriginalElement());
  }

  @Override
  public String getOriginalAssembly() {
    return "move-result-wide " + regTo1.getOriginalIndexString() + "|" + regTo2.getOriginalIndexString();
  }

  @Override
  protected DexCodeElement gcReplaceWithTemporaries(Map<DexRegister, DexRegister> mapping) {
    return new DexInstruction_MoveResultWide(getMethodCode(), mapping.get(regTo1), mapping.get(regTo2));
  }

  @Override
  public Instruction[] assembleBytecode(DexCode_AssemblingState state) {
    val regAlloc = state.getRegisterAllocation();
    int rTo1 = regAlloc.get(regTo1);
    int rTo2 = regAlloc.get(regTo2);

    if (!formWideRegister(rTo1, rTo2))
      return throwWideRegistersExpected();

    if (fitsIntoBits_Unsigned(rTo1, 8))
      return new Instruction[] {
               new Instruction11x(Opcode.MOVE_RESULT_WIDE, (short) rTo1)
             };
    else
      return throwNoSuitableFormatFound();
  }

  @Override
  public Set<DexRegister> lvaDefinedRegisters() {
    return createSet(regTo1, regTo2);
  }

  @Override
  public Set<GcRangeConstraint> gcRangeConstraints() {
    return createSet(new GcRangeConstraint(regTo1, ColorRange.RANGE_8BIT));
  }

  @Override
  public Set<GcFollowConstraint> gcFollowConstraints() {
    return createSet(new GcFollowConstraint(regTo1, regTo2));
  }

}
