package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Format.Instruction22b;
import org.jf.dexlib.Code.Format.Instruction22s;

import uk.ac.cam.db538.dexter.analysis.coloring.ColorRange;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_AssemblingState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;

public class DexInstruction_BinaryOpLiteral extends DexInstruction {

  @Getter private final DexRegister regTarget;
  @Getter private final DexRegister regSource;
  @Getter private final long literal;
  @Getter private final Opcode_BinaryOpLiteral insnOpcode;
  
  private DexClassType arithmeticException;

  public DexInstruction_BinaryOpLiteral(DexCode methodCode, DexRegister target, DexRegister source, long literal, Opcode_BinaryOpLiteral opcode) {
    super(methodCode);

    this.regTarget = target;
    this.regSource = source;
    this.literal = literal;
    insnOpcode = opcode;
  }

  public DexInstruction_BinaryOpLiteral(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException {
    super(methodCode);

    int regA, regB;
    long lit;

    if (insn instanceof Instruction22s && Opcode_BinaryOpLiteral.convert(insn.opcode) != null) {

      val insnBinaryOpLit16 = (Instruction22s) insn;
      regA = insnBinaryOpLit16.getRegisterA();
      regB = insnBinaryOpLit16.getRegisterB();
      lit = insnBinaryOpLit16.getLiteral();

    } else if (insn instanceof Instruction22b && Opcode_BinaryOpLiteral.convert(insn.opcode) != null) {

      val insnBinaryOpLit8 = (Instruction22b) insn;
      regA = insnBinaryOpLit8.getRegisterA();
      regB = insnBinaryOpLit8.getRegisterB();
      lit = insnBinaryOpLit8.getLiteral();

    } else
      throw FORMAT_EXCEPTION;

    this.regTarget = parsingState.getRegister(regA);
    this.regSource = parsingState.getRegister(regB);
    this.literal = lit;
    this.insnOpcode = Opcode_BinaryOpLiteral.convert(insn.opcode);
  }

  @Override
  public String getOriginalAssembly() {
    return insnOpcode.name().toLowerCase() + "-int/lit " + regTarget.getOriginalIndexString() +
           ", " + regSource.getOriginalIndexString() + ", #" + literal;
  }

  private boolean isLiteral8bit() {
    return fitsIntoBits_Signed(literal, 8);
  }

  @Override
  public Instruction[] assembleBytecode(DexCode_AssemblingState state) {
    val regAlloc = state.getRegisterAllocation();
    int rTarget = regAlloc.get(regTarget);
    int rSource = regAlloc.get(regSource);

    if (isLiteral8bit()) {
      if (fitsIntoBits_Unsigned(rTarget, 8) && fitsIntoBits_Unsigned(rSource, 8)) {
        return new Instruction[] {
                 new Instruction22b(Opcode_BinaryOpLiteral.convert_lit8(insnOpcode), (short) rTarget, (short) rSource, (byte) literal)
               };
      } else
        return throwCannotAssembleException("No suitable instruction format found");
    } else {
      // note: check the opcode as well, because lit16 doesn't support SHL, SHR and USHR
      if (fitsIntoBits_Unsigned(rTarget, 4) && fitsIntoBits_Unsigned(rSource, 4) && Opcode_BinaryOpLiteral.convert_lit16(insnOpcode) != null) {
        return new Instruction[] {
                 new Instruction22s(Opcode_BinaryOpLiteral.convert_lit16(insnOpcode), (byte) rTarget, (byte) rSource, (short) literal)
               };
      } else
        return throwNoSuitableFormatFound();
    }
  }

  @Override
  public Set<DexRegister> lvaDefinedRegisters() {
    return createSet(regTarget);
  }

  @Override
  public Set<DexRegister> lvaReferencedRegisters() {
    return createSet(regSource);
  }

  @Override
  public gcRegType gcReferencedRegisterType(DexRegister reg) {
    if (reg.equals(regSource))
      return gcRegType.PrimitiveSingle;
    else
      return super.gcReferencedRegisterType(reg);
  }

  @Override
  public gcRegType gcDefinedRegisterType(DexRegister reg) {
    if (reg.equals(regTarget))
      return gcRegType.PrimitiveSingle;
    else
      return super.gcDefinedRegisterType(reg);
  }

  @Override
  public Set<GcRangeConstraint> gcRangeConstraints() {
    if (isLiteral8bit()) {
      return createSet(
               new GcRangeConstraint(regTarget, ColorRange.RANGE_8BIT),
               new GcRangeConstraint(regSource, ColorRange.RANGE_8BIT));
    } else {
      return createSet(
               new GcRangeConstraint(regTarget, ColorRange.RANGE_4BIT),
               new GcRangeConstraint(regSource, ColorRange.RANGE_4BIT));
    }
  }

  @Override
  protected DexCodeElement gcReplaceWithTemporaries(Map<DexRegister, DexRegister> mapping, boolean toRefs, boolean toDefs) {
    val newTarget = (toDefs) ? mapping.get(regTarget) : regTarget;
    val newSource = (toRefs) ? mapping.get(regSource) : regSource;
    return new DexInstruction_BinaryOpLiteral(getMethodCode(), newTarget, newSource, literal, insnOpcode);
  }

  @Override
  public void instrument(DexCode_InstrumentationState state) {
    getMethodCode().replace(this,
                            new DexCodeElement[] {
                              this,
                              new DexInstruction_Move(getMethodCode(), state.getTaintRegister(regTarget), state.getTaintRegister(regSource), false)
                            });
  }

  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }

  @Override
  protected DexClassType[] throwsExceptions() {
	if (insnOpcode == Opcode_BinaryOpLiteral.Div || insnOpcode == Opcode_BinaryOpLiteral.Rem) {
		if (arithmeticException == null)
	          arithmeticException = DexClassType.parse("Ljava/lang/ArithmeticException;", getParentFile().getParsingCache());
		return new DexClassType[] {arithmeticException};
	} else
		return null;
  }

}
