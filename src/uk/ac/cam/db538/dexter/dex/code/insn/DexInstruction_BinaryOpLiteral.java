package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Format.Instruction22b;
import org.jf.dexlib.Code.Format.Instruction22s;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.CodeParserState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;

public class DexInstruction_BinaryOpLiteral extends DexInstruction {

  @Getter private final DexRegister regTarget;
  @Getter private final DexRegister regSource;
  @Getter private final long literal;
  @Getter private final Opcode_BinaryOpLiteral insnOpcode;
  
  public DexInstruction_BinaryOpLiteral(DexCode methodCode, DexRegister target, DexRegister source, long literal, Opcode_BinaryOpLiteral opcode) {
    super(methodCode);

    this.regTarget = target;
    this.regSource = source;
    this.literal = literal;
    insnOpcode = opcode;
  }

  public DexInstruction_BinaryOpLiteral(DexCode methodCode, Instruction insn, CodeParserState parsingState) throws InstructionParseError {
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

  @Override
  public Set<? extends DexRegister> lvaDefinedRegisters() {
    return Sets.newHashSet(regTarget);
  }

  @Override
  public Set<? extends DexRegister> lvaReferencedRegisters() {
    return Sets.newHashSet(regSource);
  }

  @Override
  public void instrument(DexCode_InstrumentationState state) {
	// taint propagation into ArithmeticException is not necessary here
	// because only taint of the denominator propagates and that's
	// zero for constant literals
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
		return this.hierarchy.getTypeCache().LIST_Error_ArithmeticException;
	} else
		return null;
  }

}
