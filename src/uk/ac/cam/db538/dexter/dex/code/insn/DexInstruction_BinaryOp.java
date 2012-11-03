package uk.ac.cam.db538.dexter.dex.code.insn;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Format.Instruction12x;
import org.jf.dexlib.Code.Format.Instruction23x;

import uk.ac.cam.db538.dexter.dex.code.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.reg.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.reg.RegisterAllocation;

import lombok.Getter;
import lombok.val;

public class DexInstruction_BinaryOp extends DexInstruction {

  // CAREFUL: produce /addr2 instructions if target and first
  // registers are equal; for commutative instructions,
  // check the second as well

  @Getter private final DexRegister RegTarget;
  @Getter private final DexRegister RegSourceA;
  @Getter private final DexRegister RegSourceB;
  @Getter private final Opcode_BinaryOp InsnOpcode;

  public DexInstruction_BinaryOp(DexRegister target, DexRegister sourceA, DexRegister sourceB, Opcode_BinaryOp opcode) {
    RegTarget = target;
    RegSourceA = sourceA;
    RegSourceB = sourceB;
    InsnOpcode = opcode;
  }

  public DexInstruction_BinaryOp(Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException {
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
      throw new InstructionParsingException("Unknown instruction format or opcode");

    RegTarget = parsingState.getRegister(regA);
    RegSourceA = parsingState.getRegister(regB);
    RegSourceB = parsingState.getRegister(regC);
    InsnOpcode = Opcode_BinaryOp.convert(insn.opcode);
  }

  @Override
  public String getOriginalAssembly() {
    return InsnOpcode.getAssemblyName() + " v" + RegTarget.getId() +
           ", v" + RegSourceA.getId() + ", v" + RegSourceB.getId();
  }

  @Override
  public DexCodeElement[] instrument(TaintRegisterMap mapping) {
    return new DexCodeElement[] {
             this,
             new DexInstruction_BinaryOp(
               mapping.getTaintRegister(RegTarget),
               mapping.getTaintRegister(RegSourceA),
               mapping.getTaintRegister(RegSourceB),
               Opcode_BinaryOp.OrInt)
           };
  }

  @Override
  public DexRegister[] getReferencedRegisters() {
    return new DexRegister[] { RegTarget, RegSourceA, RegSourceB };
  }

  @Override
  public Instruction[] assembleBytecode(RegisterAllocation regAlloc)
  throws InstructionAssemblyException {
    int rTarget = regAlloc.get(RegTarget);
    int rSourceA = regAlloc.get(RegSourceA);
    int rSourceB = regAlloc.get(RegSourceB);

    if (rTarget == rSourceA && fitsIntoBits_Unsigned(rTarget, 4) && fitsIntoBits_Unsigned(rSourceB, 4))
      return new Instruction[] { new Instruction12x(Opcode_BinaryOp.convert2addr(InsnOpcode), (byte) rTarget, (byte) rSourceB) };
    else if (fitsIntoBits_Unsigned(rTarget, 8) && fitsIntoBits_Unsigned(rSourceA, 8) && fitsIntoBits_Unsigned(rSourceB, 8))
      return new Instruction[] { new Instruction23x(Opcode_BinaryOp.convert(InsnOpcode), (short) rTarget, (short) rSourceA, (short) rSourceB)	};
    else
      return throwCannotAssembleException();
  }


}
