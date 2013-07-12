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

public class DexInstruction_BinaryOpWide extends DexInstruction {

  // CAREFUL: produce /addr2 instructions if target and first
  // registers are equal; for commutative instructions,
  // check the second as well

  @Getter private final DexRegister regTarget1;
  @Getter private final DexRegister regTarget2;
  @Getter private final DexRegister regSourceA1;
  @Getter private final DexRegister regSourceA2;
  @Getter private final DexRegister regSourceB1;
  @Getter private final DexRegister regSourceB2;
  @Getter private final Opcode_BinaryOpWide insnOpcode;
  
  public DexInstruction_BinaryOpWide(DexCode methodCode,
                                     DexRegister target1, DexRegister target2,
                                     DexRegister sourceA1, DexRegister sourceA2,
                                     DexRegister sourceB1, DexRegister sourceB2,
                                     Opcode_BinaryOpWide opcode) {
    super(methodCode);

    regTarget1 = target1;
    regTarget2 = target2;
    regSourceA1 = sourceA1;
    regSourceA2 = sourceA2;
    regSourceB1 = sourceB1;
    regSourceB2 = sourceB2;
    insnOpcode = opcode;
  }

  public DexInstruction_BinaryOpWide(DexCode methodCode, Instruction insn, CodeParserState parsingState) throws InstructionParseError {
    super(methodCode);

    int regA, regB, regC;

    if (insn instanceof Instruction23x && Opcode_BinaryOpWide.convert(insn.opcode) != null) {

      val insnBinaryOpWide = (Instruction23x) insn;
      regA = insnBinaryOpWide.getRegisterA();
      regB = insnBinaryOpWide.getRegisterB();
      regC = insnBinaryOpWide.getRegisterC();

    } else if (insn instanceof Instruction12x && Opcode_BinaryOpWide.convert(insn.opcode) != null) {

      val insnBinaryOpWide2addr = (Instruction12x) insn;
      regA = regB = insnBinaryOpWide2addr.getRegisterA();
      regC = insnBinaryOpWide2addr.getRegisterB();

    } else
      throw FORMAT_EXCEPTION;

    regTarget1 = parsingState.getRegister(regA);
    regTarget2 = parsingState.getRegister(regA + 1);
    regSourceA1 = parsingState.getRegister(regB);
    regSourceA2 = parsingState.getRegister(regB + 1);
    regSourceB1 = parsingState.getRegister(regC);
    regSourceB2 = parsingState.getRegister(regC + 1);
    insnOpcode = Opcode_BinaryOpWide.convert(insn.opcode);
  }

  @Override
  public String getOriginalAssembly() {
    return insnOpcode.getAssemblyName() + " " + regTarget1.getOriginalIndexString() + "|" + regTarget2.getOriginalIndexString()
           + ", " + regSourceA1.getOriginalIndexString() + "|" + regSourceA2.getOriginalIndexString()
           + ", " + regSourceB1.getOriginalIndexString() + "|" + regSourceB2.getOriginalIndexString();
  }

  @Override
  public void instrument(DexCode_InstrumentationState state) {
    val code = getMethodCode();
    val insnCombineTaintForResult = new DexInstruction_BinaryOp(code, state.getTaintRegister(regTarget1), state.getTaintRegister(regSourceA1), state.getTaintRegister(regSourceB1), Opcode_BinaryOp.OrInt);

    if (insnOpcode == Opcode_BinaryOpWide.DivLong || insnOpcode == Opcode_BinaryOpWide.RemLong) {
      val regException = new DexRegister();
      val regExceptionTaint = new DexRegister();
      val insnCombineTaintForException = new DexInstruction_BinaryOp(code, regExceptionTaint, state.getTaintRegister(regSourceA1), state.getTaintRegister(regSourceB1), Opcode_BinaryOp.OrInt);
      val insnAssignTaintToException = new DexMacro_SetObjectTaint(code, regException, regExceptionTaint);

      code.replace(this, throwingInsn_GenerateSurroundingCatchBlock(
                     new DexCodeElement[] { this, insnCombineTaintForResult },
                     new DexCodeElement[] { insnCombineTaintForException, insnAssignTaintToException },
                     regException));
    } else
      code.replace(this, new DexCodeElement[] { this, insnCombineTaintForResult });
  }

  @Override
  public Set<? extends uk.ac.cam.db538.dexter.dex.code.reg.DexRegister> lvaDefinedRegisters() {
    return createSet(regTarget1, regTarget2);
  }

  @Override
  public Set<? extends uk.ac.cam.db538.dexter.dex.code.reg.DexRegister> lvaReferencedRegisters() {
    return createSet(regSourceA1, regSourceA2, regSourceB1, regSourceB2);
  }

  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }
  
  @Override
  protected DexClassType[] throwsExceptions() {
	if (insnOpcode == Opcode_BinaryOpWide.DivLong || insnOpcode == Opcode_BinaryOpWide.RemLong) {
		return getParentFile().getTypeCache().LIST_Error_ArithmeticException;
	} else
		return null;
  }
  
}

