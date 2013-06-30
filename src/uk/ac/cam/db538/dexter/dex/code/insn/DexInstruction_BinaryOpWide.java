package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Format.Instruction12x;
import org.jf.dexlib.Code.Format.Instruction23x;

import uk.ac.cam.db538.dexter.analysis.coloring.ColorRange;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_AssemblingState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.code.insn.macro.DexMacro_SetObjectTaint;

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
  
  private DexClassType arithmeticException;

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

  public DexInstruction_BinaryOpWide(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException {
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

    if (insnOpcode == Opcode_BinaryOpWide.DivLong) {
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
  public Instruction[] assembleBytecode(DexCode_AssemblingState state) {
    val regAlloc = state.getRegisterAllocation();
    int rTarget1 = regAlloc.get(regTarget1);
    int rTarget2 = regAlloc.get(regTarget2);
    int rSourceA1 = regAlloc.get(regSourceA1);
    int rSourceA2 = regAlloc.get(regSourceA2);
    int rSourceB1 = regAlloc.get(regSourceB1);
    int rSourceB2 = regAlloc.get(regSourceB2);

    if (!formWideRegister(rTarget1, rTarget2) || !formWideRegister(rSourceA1, rSourceA2) || !formWideRegister(rSourceB1, rSourceB2))
      return throwWideRegistersExpected();

    if (rTarget1 == rSourceA1 && fitsIntoBits_Unsigned(rTarget1, 4) && fitsIntoBits_Unsigned(rSourceB1, 4))
      return new Instruction[] { new Instruction12x(Opcode_BinaryOpWide.convert2addr(insnOpcode), (byte) rTarget1, (byte) rSourceB1) };
    else if (fitsIntoBits_Unsigned(rTarget1, 8) && fitsIntoBits_Unsigned(rSourceA1, 8) && fitsIntoBits_Unsigned(rSourceB1, 8))
      return new Instruction[] { new Instruction23x(Opcode_BinaryOpWide.convert(insnOpcode), (short) rTarget1, (short) rSourceA1, (short) rSourceB1)	};
    else
      return throwNoSuitableFormatFound();
  }

  @Override
  public Set<DexRegister> lvaDefinedRegisters() {
    return createSet(regTarget1, regTarget2);
  }

  @Override
  public Set<DexRegister> lvaReferencedRegisters() {
    return createSet(regSourceA1, regSourceA2, regSourceB1, regSourceB2);
  }

  @Override
  public gcRegType gcReferencedRegisterType(DexRegister reg) {
    if (reg.equals(regSourceA1) || reg.equals(regSourceB1))
      return gcRegType.PrimitiveWide_High;
    else if (reg.equals(regSourceA2) || reg.equals(regSourceB2))
      return gcRegType.PrimitiveWide_Low;
    else
      return super.gcReferencedRegisterType(reg);
  }

  @Override
  public gcRegType gcDefinedRegisterType(DexRegister reg) {
    if (reg.equals(regTarget1))
      return gcRegType.PrimitiveWide_High;
    else if (reg.equals(regTarget2))
      return gcRegType.PrimitiveWide_Low;
    else
      return super.gcDefinedRegisterType(reg);
  }

  @Override
  public Set<GcRangeConstraint> gcRangeConstraints() {
    return createSet(
             new GcRangeConstraint(regTarget1, ColorRange.RANGE_8BIT),
             new GcRangeConstraint(regSourceA1, ColorRange.RANGE_8BIT),
             new GcRangeConstraint(regSourceB1, ColorRange.RANGE_8BIT));
  }

  @Override
  public Set<GcFollowConstraint> gcFollowConstraints() {
    return createSet(
             new GcFollowConstraint(regTarget1, regTarget2),
             new GcFollowConstraint(regSourceA1, regSourceA2),
             new GcFollowConstraint(regSourceB1, regSourceB2));
  }

  @Override
  protected DexCodeElement gcReplaceWithTemporaries(Map<DexRegister, DexRegister> mapping, boolean toRefs, boolean toDefs) {
    val newTarget1 = (toDefs) ? mapping.get(regTarget1) : regTarget1;
    val newTarget2 = (toDefs) ? mapping.get(regTarget2) : regTarget2;
    val newSourceA1 = (toRefs) ? mapping.get(regSourceA1) : regSourceA1;
    val newSourceA2 = (toRefs) ? mapping.get(regSourceA2) : regSourceA2;
    val newSourceB1 = (toRefs) ? mapping.get(regSourceB1) : regSourceB1;
    val newSourceB2 = (toRefs) ? mapping.get(regSourceB2) : regSourceB2;
    return new DexInstruction_BinaryOpWide(
             getMethodCode(),
             newTarget1,
             newTarget2,
             newSourceA1,
             newSourceA2,
             newSourceB1,
             newSourceB2,
             insnOpcode);
  }

  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }
  
  @Override
  protected DexClassType[] throwsExceptions() {
	if (insnOpcode == Opcode_BinaryOpWide.DivLong || insnOpcode == Opcode_BinaryOpWide.RemLong) {
		return getParentFile().getParsingCache().LIST_Error_ArithmeticException;
	} else
		return null;
  }
  
}

