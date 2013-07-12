package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Format.Instruction12x;
import org.jf.dexlib.Code.Format.Instruction23x;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.CodeParserState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.insn.macro.DexMacro_SetObjectTaint;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;

public class DexInstruction_BinaryOp extends DexInstruction {

  // CAREFUL: produce /addr2 instructions if target and first
  // registers are equal; for commutative instructions,
  // check the second as well

  @Getter private final DexRegister regTarget;
  @Getter private final DexRegister regSourceA;
  @Getter private final DexRegister regSourceB;
  @Getter private final Opcode_BinaryOp insnOpcode;
  
  public DexInstruction_BinaryOp(DexCode methodCode, DexRegister target, DexRegister sourceA, DexRegister sourceB, Opcode_BinaryOp opcode) {
    super(methodCode);

    regTarget = target;
    regSourceA = sourceA;
    regSourceB = sourceB;
    insnOpcode = opcode;
  }

  public DexInstruction_BinaryOp(DexCode methodCode, Instruction insn, CodeParserState parsingState) throws InstructionParseError {
    super(methodCode);

    int regA, regB, regC;

    if (insn instanceof Instruction23x && Opcode_BinaryOp.convert(insn.opcode) != null) {

      val insnBinaryOp = (Instruction23x) insn;
      regA = insnBinaryOp.getRegisterA();
      regB = insnBinaryOp.getRegisterB();
      regC = insnBinaryOp.getRegisterC();

    } else if (insn instanceof Instruction12x && Opcode_BinaryOp.convert(insn.opcode) != null) {

      val insnBinaryOp2addr = (Instruction12x) insn;
      regA = regB = insnBinaryOp2addr.getRegisterA();
      regC = insnBinaryOp2addr.getRegisterB();

    } else
      throw FORMAT_EXCEPTION;

    regTarget = parsingState.getRegister(regA);
    regSourceA = parsingState.getRegister(regB);
    regSourceB = parsingState.getRegister(regC);
    insnOpcode = Opcode_BinaryOp.convert(insn.opcode);
  }

  @Override
  public String getOriginalAssembly() {
    return insnOpcode.getAssemblyName() + " " + regTarget.getOriginalIndexString() +
           ", " + regSourceA.getOriginalIndexString() + ", " + regSourceB.getOriginalIndexString();
  }

  @Override
  public void instrument(DexCode_InstrumentationState state) {
    val code = getMethodCode();
    val insnCombineTaint = new DexInstruction_BinaryOp(code, state.getTaintRegister(regTarget), state.getTaintRegister(regSourceA), state.getTaintRegister(regSourceB), Opcode_BinaryOp.OrInt);

    if (insnOpcode == Opcode_BinaryOp.DivInt || insnOpcode == Opcode_BinaryOp.RemInt) {
      val regException = new DexRegister();
      val insnAssignTaintToException = new DexMacro_SetObjectTaint(code, regException, state.getTaintRegister(regSourceB));

      code.replace(this, throwingInsn_GenerateSurroundingCatchBlock(
                     new DexCodeElement[] { this, insnCombineTaint },
                     new DexCodeElement[] { insnAssignTaintToException },
                     regException));
    } else
      code.replace(this, new DexCodeElement[] { this, insnCombineTaint });

  }

  @Override
  public Set<? extends DexRegister> lvaDefinedRegisters() {
    return Sets.newHashSet(regTarget);
  }

  @Override
  public Set<? extends DexRegister> lvaReferencedRegisters() {
    return Sets.newHashSet(regSourceA, regSourceB);
  }

  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }
  
  @Override
  protected DexClassType[] throwsExceptions() {
	if (insnOpcode == Opcode_BinaryOp.DivInt || insnOpcode == Opcode_BinaryOp.RemInt) {
		return this.hierarchy.getTypeCache().LIST_Error_ArithmeticException;
	} else
		return null;
  }
}
