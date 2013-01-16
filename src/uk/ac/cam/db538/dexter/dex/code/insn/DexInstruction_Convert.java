package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.Map;
import java.util.Set;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Format.Instruction12x;

import uk.ac.cam.db538.dexter.analysis.coloring.ColorRange;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_AssemblingState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;

import lombok.Getter;
import lombok.val;

public class DexInstruction_Convert extends DexInstruction {

  @Getter private final DexRegister regTo;
  @Getter private final DexRegister regFrom;
  @Getter private final Opcode_Convert insnOpcode;

  public DexInstruction_Convert(DexCode methodCode, DexRegister to, DexRegister from, Opcode_Convert opcode) {
    super(methodCode);

    regTo = to;
    regFrom = from;
    insnOpcode = opcode;
  }

  public DexInstruction_Convert(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException {
    super(methodCode);

    if (insn instanceof Instruction12x && Opcode_Convert.convert(insn.opcode) != null) {

      val insnConvert = (Instruction12x) insn;
      regTo = parsingState.getRegister(insnConvert.getRegisterA());
      regFrom = parsingState.getRegister(insnConvert.getRegisterB());
      insnOpcode = Opcode_Convert.convert(insn.opcode);

    } else
      throw FORMAT_EXCEPTION;
  }

  @Override
  public String getOriginalAssembly() {
    return insnOpcode.getAssemblyName() + " " + regTo.getOriginalIndexString() + ", " + regFrom.getOriginalIndexString();
  }

  @Override
  protected DexCodeElement gcReplaceWithTemporaries(Map<DexRegister, DexRegister> mapping) {
    return new DexInstruction_Convert(getMethodCode(), mapping.get(regTo), mapping.get(regFrom), insnOpcode);
  }

  @Override
  public void instrument(DexCode_InstrumentationState state) {
    // need to copy to taint across
    val code = getMethodCode();
    code.replace(this,
                 new DexCodeElement[] {
                   this,
                   new DexInstruction_Move(code, state.getTaintRegister(regTo), state.getTaintRegister(regFrom), false)
                 });
  }

  @Override
  public Instruction[] assembleBytecode(DexCode_AssemblingState state) {
    val regAlloc = state.getRegisterAllocation();
    int rTo = regAlloc.get(regTo);
    int rFrom = regAlloc.get(regFrom);

    if (fitsIntoBits_Unsigned(rTo, 4) && fitsIntoBits_Unsigned(rFrom, 4))
      return new Instruction[] { new Instruction12x(Opcode_Convert.convert(insnOpcode), (byte) rTo, (byte) rFrom) };
    else
      return throwNoSuitableFormatFound();
  }

  @Override
  public Set<GcRangeConstraint> gcRangeConstraints() {
    return createSet(new GcRangeConstraint(regTo, ColorRange.RANGE_4BIT),
                     new GcRangeConstraint(regFrom, ColorRange.RANGE_4BIT));
  }

  @Override
  public Set<DexRegister> lvaDefinedRegisters() {
    return createSet(regTo);
  }

  @Override
  public Set<DexRegister> lvaReferencedRegisters() {
    return createSet(regFrom);
  }
}
