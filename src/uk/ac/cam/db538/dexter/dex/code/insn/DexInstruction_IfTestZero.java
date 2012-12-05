package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.HashSet;
import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Format.Instruction21t;

import uk.ac.cam.db538.dexter.analysis.coloring.ColorRange;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.DexCode_AssemblingState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexLabel;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;

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
      throw new InstructionParsingException("Unknown instruction format or opcode");
  }

  @Override
  public String getOriginalAssembly() {
    return "if-" + insnOpcode.name() + " v" + reg.getOriginalIndexString() +
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
  public DexCodeElement[] cfgGetSuccessors() {
    return new DexCodeElement[] {
             this.getNextCodeElement(),
             this.target
           };
  }

  @Override
  public Set<DexRegister> lvaReferencedRegisters() {
    val set = new HashSet<DexRegister>();
    set.add(reg);
    return set;
  }

  @Override
  public Set<GcRangeConstraint> gcRangeConstraints() {
    val set = new HashSet<GcRangeConstraint>();
    set.add(new GcRangeConstraint(reg, ColorRange.RANGE_8BIT));
    return set;
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
}
