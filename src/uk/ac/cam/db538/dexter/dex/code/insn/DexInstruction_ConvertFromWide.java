package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Format.Instruction12x;

import uk.ac.cam.db538.dexter.analysis.coloring.ColorRange;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_AssemblingState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;

public class DexInstruction_ConvertFromWide extends DexInstruction {

  @Getter private final DexRegister regTo;
  @Getter private final DexRegister regFrom1;
  @Getter private final DexRegister regFrom2;
  @Getter private final Opcode_ConvertFromWide insnOpcode;

  public DexInstruction_ConvertFromWide(DexCode methodCode, DexRegister to, DexRegister from1, DexRegister from2, Opcode_ConvertFromWide opcode) {
    super(methodCode);

    regTo = to;
    regFrom1 = from1;
    regFrom2 = from2;
    insnOpcode = opcode;
  }

  public DexInstruction_ConvertFromWide(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException {
    super(methodCode);

    if (insn instanceof Instruction12x && Opcode_ConvertFromWide.convert(insn.opcode) != null) {

      val insnConvert = (Instruction12x) insn;
      regTo = parsingState.getRegister(insnConvert.getRegisterA());
      regFrom1 = parsingState.getRegister(insnConvert.getRegisterB());
      regFrom2 = parsingState.getRegister(insnConvert.getRegisterB() + 1);
      insnOpcode = Opcode_ConvertFromWide.convert(insn.opcode);

    } else
      throw FORMAT_EXCEPTION;
  }

  @Override
  public String getOriginalAssembly() {
    return insnOpcode.getAssemblyName() + " " + regTo.getOriginalIndexString() + ", " + regFrom1.getOriginalIndexString() + "|" + regFrom2.getOriginalIndexString();
  }

  @Override
  protected DexCodeElement gcReplaceWithTemporaries(Map<DexRegister, DexRegister> mapping, boolean toRefs, boolean toDefs) {
    val newTo = (toDefs) ? mapping.get(regTo) : regTo;
    val newFrom1 = (toRefs) ? mapping.get(regFrom1) : regFrom1;
    val newFrom2 = (toRefs) ? mapping.get(regFrom2) : regFrom2;
    return new DexInstruction_ConvertFromWide(getMethodCode(), newTo, newFrom1, newFrom2, insnOpcode);
  }

  @Override
  public void instrument(DexCode_InstrumentationState state) {
    // need to combine the taint of the two wide registers and assign it the operation result
    val code = getMethodCode();
    code.replace(this,
                 new DexCodeElement[] {
                   this,
                   new DexInstruction_Move(code, state.getTaintRegister(regTo), state.getTaintRegister(regFrom1), false)
                 });
  }

  @Override
  public Instruction[] assembleBytecode(DexCode_AssemblingState state) {
    val regAlloc = state.getRegisterAllocation();
    int rTo = regAlloc.get(regTo);
    int rFrom1 = regAlloc.get(regFrom1);
    int rFrom2 = regAlloc.get(regFrom2);

    if (!formWideRegister(rFrom1, rFrom2))
      return throwWideRegistersExpected();

    if (fitsIntoBits_Unsigned(rTo, 4) && fitsIntoBits_Unsigned(rFrom1, 4))
      return new Instruction[] { new Instruction12x(Opcode_ConvertFromWide.convert(insnOpcode), (byte) rTo, (byte) rFrom1) };
    else
      return throwNoSuitableFormatFound();
  }

  @Override
  public Set<GcRangeConstraint> gcRangeConstraints() {
    return createSet(new GcRangeConstraint(regTo, ColorRange.RANGE_4BIT),
                     new GcRangeConstraint(regFrom1, ColorRange.RANGE_4BIT));
  }

  @Override
  public Set<GcFollowConstraint> gcFollowConstraints() {
    return createSet(new GcFollowConstraint(regFrom1, regFrom2));
  }

  @Override
  public Set<DexRegister> lvaDefinedRegisters() {
    return createSet(regTo);
  }

  @Override
  public Set<DexRegister> lvaReferencedRegisters() {
    return createSet(regFrom1, regFrom2);
  }

  @Override
  public gcRegType gcReferencedRegisterType(DexRegister reg) {
    if (reg.equals(regFrom1))
      return gcRegType.PrimitiveWide_High;
    else if (reg.equals(regFrom2))
      return gcRegType.PrimitiveWide_Low;
    else
      return super.gcReferencedRegisterType(reg);
  }

  @Override
  public gcRegType gcDefinedRegisterType(DexRegister reg) {
    if (reg.equals(regTo))
      return gcRegType.PrimitiveSingle;
    else
      return super.gcDefinedRegisterType(reg);
  }

}
