package uk.ac.cam.db538.dexter.dex.code.insn;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Format.Instruction12x;
import org.jf.dexlib.Code.Format.Instruction23x;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.reg.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.reg.RegisterAllocation;

import lombok.Getter;
import lombok.val;

public class DexInstruction_BinaryOpWide extends DexInstruction {

  // CAREFUL: produce /addr2 instructions if target and first
  // registers are equal; for commutative instructions,
  // check the second as well

  @Getter private final DexRegister RegTarget1;
  @Getter private final DexRegister RegTarget2;
  @Getter private final DexRegister RegSourceA1;
  @Getter private final DexRegister RegSourceA2;
  @Getter private final DexRegister RegSourceB1;
  @Getter private final DexRegister RegSourceB2;
  @Getter private final Opcode_BinaryOpWide InsnOpcode;

  public DexInstruction_BinaryOpWide(DexCode methodCode,
                                     DexRegister target1, DexRegister target2,
                                     DexRegister sourceA1, DexRegister sourceA2,
                                     DexRegister sourceB1, DexRegister sourceB2,
                                     Opcode_BinaryOpWide opcode) {
    super(methodCode);

    RegTarget1 = target1;
    RegTarget2 = target2;
    RegSourceA1 = sourceA1;
    RegSourceA2 = sourceA2;
    RegSourceB1 = sourceB1;
    RegSourceB2 = sourceB2;
    InsnOpcode = opcode;
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

    RegTarget1 = parsingState.getRegister(regA);
    RegTarget2 = parsingState.getRegister(regA + 1);
    RegSourceA1 = parsingState.getRegister(regB);
    RegSourceA2 = parsingState.getRegister(regB + 1);
    RegSourceB1 = parsingState.getRegister(regC);
    RegSourceB2 = parsingState.getRegister(regC + 1);
    InsnOpcode = Opcode_BinaryOpWide.convert(insn.opcode);
  }

  @Override
  public String getOriginalAssembly() {
    return InsnOpcode.getAssemblyName() + " v" + RegTarget1.getId() +
           ", v" + RegSourceA1.getId() + ", v" + RegSourceB1.getId();
  }

  @Override
  public DexRegister[] getReferencedRegisters() {
    return new DexRegister[] { RegTarget1, RegTarget2, RegSourceA1, RegSourceA2, RegSourceB1, RegSourceB2 };
  }

  @Override
  public DexCodeElement[] instrument(DexCode_InstrumentationState mapping) {
    val taintTarget1 = mapping.getTaintRegister(RegTarget1);
    val taintTarget2 = mapping.getTaintRegister(RegTarget2);
    val taintSourceA1 = mapping.getTaintRegister(RegSourceA1);
    val taintSourceA2 = mapping.getTaintRegister(RegSourceA2);
    val taintSourceB1 = mapping.getTaintRegister(RegSourceB1);
    val taintSourceB2 = mapping.getTaintRegister(RegSourceB2);

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
  public Instruction[] assembleBytecode(RegisterAllocation regAlloc)
  throws InstructionAssemblyException {
    int rTarget1 = regAlloc.get(RegTarget1);
    int rTarget2 = regAlloc.get(RegTarget2);
    int rSourceA1 = regAlloc.get(RegSourceA1);
    int rSourceA2 = regAlloc.get(RegSourceA2);
    int rSourceB1 = regAlloc.get(RegSourceB1);
    int rSourceB2 = regAlloc.get(RegSourceB2);

    if (!formWideRegister(rTarget1, rTarget2) || !formWideRegister(rSourceA1, rSourceA2) || !formWideRegister(rSourceB1, rSourceB2))
      return throwWideRegistersExpected();

    if (rTarget1 == rSourceA1 && fitsIntoBits_Unsigned(rTarget1, 4) && fitsIntoBits_Unsigned(rSourceB1, 4))
      return new Instruction[] { new Instruction12x(Opcode_BinaryOpWide.convert2addr(InsnOpcode), (byte) rTarget1, (byte) rSourceB1) };
    else if (fitsIntoBits_Unsigned(rTarget1, 8) && fitsIntoBits_Unsigned(rSourceA1, 8) && fitsIntoBits_Unsigned(rSourceB1, 8))
      return new Instruction[] { new Instruction23x(Opcode_BinaryOpWide.convert(InsnOpcode), (short) rTarget1, (short) rSourceA1, (short) rSourceB1)	};
    else
      return throwCannotAssembleException();
  }

  @Override
  public DexRegister[] lvaDefinedRegisters() {
    return new DexRegister[] { RegTarget1, RegTarget2 };
  }

  @Override
  public DexRegister[] lvaReferencedRegisters() {
    return new DexRegister[] { RegSourceA1, RegSourceA2, RegSourceB1, RegSourceB2 };
  }
}
