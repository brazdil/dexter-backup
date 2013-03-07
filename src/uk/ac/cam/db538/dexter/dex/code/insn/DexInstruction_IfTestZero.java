package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Format.Instruction21t;

import uk.ac.cam.db538.dexter.analysis.coloring.ColorRange;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_AssemblingState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.elem.DexLabel;

public class DexInstruction_IfTestZero extends DexInstruction {

  @Getter private final DexRegister reg;
  @Getter private final DexLabel target;
  @Getter private final Opcode_IfTestZero insnOpcode;

  public DexInstruction_IfTestZero(DexCode methodCode, DexRegister reg, DexLabel target, Opcode_IfTestZero opcode) {
    super(methodCode);

    this.reg = reg;
    this.target = target;
    this.insnOpcode = opcode;
  }

  public DexInstruction_IfTestZero(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException {
    super(methodCode);

    if (insn instanceof Instruction21t && Opcode_IfTestZero.convert(insn.opcode) != null) {

      val insnIfTestZero = (Instruction21t) insn;
      reg = parsingState.getRegister(insnIfTestZero.getRegisterA());
      target = parsingState.getLabel(insnIfTestZero.getTargetAddressOffset());
      insnOpcode = Opcode_IfTestZero.convert(insn.opcode);

    } else
      throw FORMAT_EXCEPTION;
  }

  @Override
  public String getOriginalAssembly() {
    return "if-" + insnOpcode.name() + " " + reg.getOriginalIndexString() +
           ", L" + target.getOriginalAbsoluteOffset();
  }

  @Override
  public Instruction[] assembleBytecode(DexCode_AssemblingState state) {
    int rTest = state.getRegisterAllocation().get(reg);
    long offset = computeRelativeOffset(target, state);

    if (!fitsIntoBits_Signed(offset, 16))
      throw new InstructionOffsetException(this);

    if (fitsIntoBits_Unsigned(rTest, 8))
      return new Instruction[] {
               new Instruction21t(Opcode_IfTestZero.convert(insnOpcode), (short) rTest, (short) offset)
             };
    else
      return throwNoSuitableFormatFound();
  }

  @Override
  public boolean cfgEndsBasicBlock() {
    return true;
  }

  @Override
  public Set<DexCodeElement> cfgGetSuccessors() {
    return createSet(
             (DexCodeElement) target,
             this.getNextCodeElement());
  }

  @Override
  public Set<DexRegister> lvaReferencedRegisters() {
    return createSet(reg);
  }

  @Override
  protected gcRegType gcReferencedRegisterType(DexRegister reg) {
    if (insnOpcode == Opcode_IfTestZero.eqz || insnOpcode == Opcode_IfTestZero.nez)
      throw new UnsupportedOperationException();
    if (reg.equals(reg))
      return gcRegType.PrimitiveSingle;
    else
      return super.gcReferencedRegisterType(reg);
  }

  @Override
  public Set<GcRangeConstraint> gcRangeConstraints() {
    return createSet(new GcRangeConstraint(reg, ColorRange.RANGE_8BIT));
  }

  @Override
  public DexCodeElement[] fixLongJump() {
    val code = this.getMethodCode();

    val labelSuccessor = new DexLabel(code);
    val labelLongJump = new DexLabel(code);

    return new DexCodeElement[] {
             new DexInstruction_IfTestZero(code, reg, labelLongJump, insnOpcode),
             new DexInstruction_Goto(code, labelSuccessor),
             labelLongJump,
             new DexInstruction_Goto(code, target),
             labelSuccessor
           };
  }

  @Override
  protected DexCodeElement gcReplaceWithTemporaries(Map<DexRegister, DexRegister> mapping, boolean toRefs, boolean toDefs) {
    val newReg = (toRefs) ? mapping.get(reg) : reg;
    return new DexInstruction_IfTestZero(getMethodCode(), newReg, target, insnOpcode);
  }

  @Override
  public void instrument(DexCode_InstrumentationState state) { }

}
