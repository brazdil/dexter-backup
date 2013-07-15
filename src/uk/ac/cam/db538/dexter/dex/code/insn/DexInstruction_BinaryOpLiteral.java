package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Format.Instruction22b;
import org.jf.dexlib.Code.Format.Instruction22s;

import uk.ac.cam.db538.dexter.dex.code.CodeParserState;
import uk.ac.cam.db538.dexter.dex.code.reg.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.reg.DexSingleRegister;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.hierarchy.RuntimeHierarchy;

import com.google.common.collect.Sets;

public class DexInstruction_BinaryOpLiteral extends DexInstruction {

  @Getter private final DexSingleRegister regTarget;
  @Getter private final DexSingleRegister regSource;
  @Getter private final long literal;
  @Getter private final Opcode_BinaryOpLiteral insnOpcode;
  
  public DexInstruction_BinaryOpLiteral(DexSingleRegister target, DexSingleRegister source, long literal, Opcode_BinaryOpLiteral opcode, RuntimeHierarchy hierarchy) {
    super(hierarchy);

    this.regTarget = target;
    this.regSource = source;
    this.literal = literal;
    this.insnOpcode = opcode;
  }

  public static DexInstruction_BinaryOpLiteral parse(Instruction insn, CodeParserState parsingState) {
	val opcode = Opcode_BinaryOpLiteral.convert(insn.opcode);
    int regA, regB;
    long lit;

    if (insn instanceof Instruction22s && opcode != null) {

      val insnBinaryOpLit16 = (Instruction22s) insn;
      regA = insnBinaryOpLit16.getRegisterA();
      regB = insnBinaryOpLit16.getRegisterB();
      lit = insnBinaryOpLit16.getLiteral();

    } else if (insn instanceof Instruction22b && opcode != null) {

      val insnBinaryOpLit8 = (Instruction22b) insn;
      regA = insnBinaryOpLit8.getRegisterA();
      regB = insnBinaryOpLit8.getRegisterB();
      lit = insnBinaryOpLit8.getLiteral();

    } else
      throw FORMAT_EXCEPTION;

    return new DexInstruction_BinaryOpLiteral(
    		parsingState.getSingleRegister(regA),
    		parsingState.getSingleRegister(regB),
    		lit,
    		opcode,
    		parsingState.getHierarchy());
  }

  @Override
  public String toString() {
    return insnOpcode.name().toLowerCase() + "-int/lit " + regTarget.toString() +
           ", " + regSource.toString() + ", #" + literal;
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
  public void instrument() {
//	// taint propagation into ArithmeticException is not necessary here
//	// because only taint of the denominator propagates and that's
//	// zero for constant literals
//    getMethodCode().replace(this,
//                            new DexCodeElement[] {
//                              this,
//                              new DexInstruction_Move(getMethodCode(), state.getTaintRegister(regTarget), state.getTaintRegister(regSource), false)
//                            });
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
