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
import uk.ac.cam.db538.dexter.dex.DexAssemblingCache;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;

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
      throw new InstructionParsingException("Unknown instruction format or opcode");

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
    return insnOpcode.getAssemblyName() + " v" + regTarget1.getOriginalIndexString() +
           ", v" + regSourceA1.getOriginalIndexString() + ", v" + regSourceB1.getOriginalIndexString();
  }

  @Override
  public DexCodeElement[] instrument(DexCode_InstrumentationState mapping) {
    val taintTarget1 = mapping.getTaintRegister(regTarget1);
    val taintTarget2 = mapping.getTaintRegister(regTarget2);
    val taintSourceA1 = mapping.getTaintRegister(regSourceA1);
    val taintSourceA2 = mapping.getTaintRegister(regSourceA2);
    val taintSourceB1 = mapping.getTaintRegister(regSourceB1);
    val taintSourceB2 = mapping.getTaintRegister(regSourceB2);

    return new DexCodeElement[] {
             this,
             new DexInstruction_BinaryOp(
               this.getMethodCode(),
               taintTarget1,
               taintSourceA1,
               taintSourceA2,
               Opcode_BinaryOp.OrInt),
             new DexInstruction_BinaryOp(
               this.getMethodCode(),
               taintTarget1,
               taintTarget1,
               taintSourceB1,
               Opcode_BinaryOp.OrInt),
             new DexInstruction_BinaryOp(
               this.getMethodCode(),
               taintTarget1,
               taintTarget1,
               taintSourceB2,
               Opcode_BinaryOp.OrInt),
             new DexInstruction_Move(
               this.getMethodCode(),
               taintTarget2,
               taintTarget1,
               false)
           };
  }

  @Override
  public Instruction[] assembleBytecode(Map<DexRegister, Integer> regAlloc, DexAssemblingCache cache) {
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
      return throwCannotAssembleException("No suitable instruction format found");
  }

  @Override
  public Set<DexRegister> lvaDefinedRegisters() {
    val regs = new HashSet<DexRegister>();
    regs.add(regTarget1);
    regs.add(regTarget2);
    return regs;
  }

  @Override
  public Set<DexRegister> lvaReferencedRegisters() {
    val regs = new HashSet<DexRegister>();
    regs.add(regSourceA1);
    regs.add(regSourceA2);
    regs.add(regSourceB1);
    regs.add(regSourceB2);
    return regs;
  }

  @Override
  public Set<GcRangeConstraint> gcRangeConstraints() {
    val set = new HashSet<GcRangeConstraint>();
    set.add(new GcRangeConstraint(regTarget1, ColorRange.RANGE_8BIT));
    set.add(new GcRangeConstraint(regSourceA1, ColorRange.RANGE_8BIT));
    set.add(new GcRangeConstraint(regSourceB1, ColorRange.RANGE_8BIT));
    return set;
  }

  @Override
  public Set<GcFollowConstraint> gcFollowConstraints() {
    val set = new HashSet<GcFollowConstraint>();
    set.add(new GcFollowConstraint(regTarget1, regTarget2));
    set.add(new GcFollowConstraint(regSourceA1, regSourceA2));
    set.add(new GcFollowConstraint(regSourceB1, regSourceB2));
    return set;
  }

  @Override
  protected DexCodeElement gcReplaceWithTemporaries(Map<DexRegister, DexRegister> mapping) {
    return new DexInstruction_BinaryOpWide(
             getMethodCode(),
             mapping.get(regTarget1),
             mapping.get(regTarget2),
             mapping.get(regSourceA1),
             mapping.get(regSourceA2),
             mapping.get(regSourceB1),
             mapping.get(regSourceB2),
             insnOpcode);
  }
}

