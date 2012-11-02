package uk.ac.cam.db538.dexter.dex.code.insn;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Format.Instruction12x;
import org.jf.dexlib.Code.Format.Instruction23x;

import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.reg.DexRegister;

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

  public DexInstruction_BinaryOpWide(DexRegister target1, DexRegister target2,
                                     DexRegister sourceA1, DexRegister sourceA2,
                                     DexRegister sourceB1, DexRegister sourceB2, Opcode_BinaryOpWide opcode) {
    RegTarget1 = target1;
    RegTarget2 = target2;
    RegSourceA1 = sourceA1;
    RegSourceA2 = sourceA2;
    RegSourceB1 = sourceB1;
    RegSourceB2 = sourceB2;
    InsnOpcode = opcode;
  }

  public DexInstruction_BinaryOpWide(Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException {
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
}
