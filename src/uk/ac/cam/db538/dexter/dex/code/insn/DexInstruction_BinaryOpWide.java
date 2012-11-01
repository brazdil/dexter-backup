package uk.ac.cam.db538.dexter.dex.code.insn;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Format.Instruction12x;
import org.jf.dexlib.Code.Format.Instruction23x;

import uk.ac.cam.db538.dexter.dex.code.DexRegister;

import lombok.Getter;
import lombok.val;

public class DexInstruction_BinaryOpWide extends DexInstruction {

  // CAREFUL: produce /addr2 instructions if target and first
  // registers are equal; for commutative instructions,
  // check the second as well

  public static enum Opcode {
    AddLong("add-long"),
    SubLong("sub-long"),
    MulLong("mul-long"),
    DivLong("div-long"),
    RemLong("rem-long"),
    AndLong("and-long"),
    OrLong("or-long"),
    XorLong("xor-long"),
    ShlLong("shl-long"),
    ShrLong("shr-long"),
    UshrLong("ushr-long"),
    AddDouble("add-double"),
    SubDouble("sub-double"),
    MulDouble("mul-double"),
    DivDouble("div-double"),
    RemDouble("rem-double");

    @Getter private final String AssemblyName;

    private Opcode(String assemblyName) {
      AssemblyName = assemblyName;
    }

    public static Opcode convert(org.jf.dexlib.Code.Opcode opcode) {
      switch (opcode) {
      case ADD_LONG:
      case ADD_LONG_2ADDR:
        return AddLong;
      case SUB_LONG:
      case SUB_LONG_2ADDR:
        return SubLong;
      case MUL_LONG:
      case MUL_LONG_2ADDR:
        return MulLong;
      case DIV_LONG:
      case DIV_LONG_2ADDR:
        return DivLong;
      case REM_LONG:
      case REM_LONG_2ADDR:
        return RemLong;
      case AND_LONG:
      case AND_LONG_2ADDR:
        return AndLong;
      case OR_LONG:
      case OR_LONG_2ADDR:
        return OrLong;
      case XOR_LONG:
      case XOR_LONG_2ADDR:
        return XorLong;
      case SHL_LONG:
      case SHL_LONG_2ADDR:
        return ShlLong;
      case SHR_LONG:
      case SHR_LONG_2ADDR:
        return ShrLong;
      case USHR_LONG:
      case USHR_LONG_2ADDR:
        return UshrLong;
      case ADD_DOUBLE:
      case ADD_DOUBLE_2ADDR:
        return AddDouble;
      case SUB_DOUBLE:
      case SUB_DOUBLE_2ADDR:
        return SubDouble;
      case MUL_DOUBLE:
      case MUL_DOUBLE_2ADDR:
        return MulDouble;
      case DIV_DOUBLE:
      case DIV_DOUBLE_2ADDR:
        return DivDouble;
      case REM_DOUBLE:
      case REM_DOUBLE_2ADDR:
        return RemDouble;
      default:
        return null;
      }
    }

    public static org.jf.dexlib.Code.Opcode convert(Opcode opcode) {
      switch (opcode) {
      case AddLong:
        return org.jf.dexlib.Code.Opcode.ADD_LONG;
      case SubLong:
        return org.jf.dexlib.Code.Opcode.SUB_LONG;
      case MulLong:
        return org.jf.dexlib.Code.Opcode.MUL_LONG;
      case DivLong:
        return org.jf.dexlib.Code.Opcode.DIV_LONG;
      case RemLong:
        return org.jf.dexlib.Code.Opcode.REM_LONG;
      case AndLong:
        return org.jf.dexlib.Code.Opcode.AND_LONG;
      case OrLong:
        return org.jf.dexlib.Code.Opcode.OR_LONG;
      case XorLong:
        return org.jf.dexlib.Code.Opcode.XOR_LONG;
      case ShlLong:
        return org.jf.dexlib.Code.Opcode.SHL_LONG;
      case ShrLong:
        return org.jf.dexlib.Code.Opcode.SHR_LONG;
      case UshrLong:
        return org.jf.dexlib.Code.Opcode.USHR_LONG;
      case AddDouble:
        return org.jf.dexlib.Code.Opcode.ADD_DOUBLE;
      case SubDouble:
        return org.jf.dexlib.Code.Opcode.SUB_DOUBLE;
      case MulDouble:
        return org.jf.dexlib.Code.Opcode.MUL_DOUBLE;
      case DivDouble:
        return org.jf.dexlib.Code.Opcode.DIV_DOUBLE;
      case RemDouble:
        return org.jf.dexlib.Code.Opcode.REM_DOUBLE;
      default:
        return null;
      }
    }

    public static org.jf.dexlib.Code.Opcode convert2addr(Opcode opcode) {
      switch (opcode) {
      case AddLong:
        return org.jf.dexlib.Code.Opcode.ADD_LONG_2ADDR;
      case SubLong:
        return org.jf.dexlib.Code.Opcode.SUB_LONG_2ADDR;
      case MulLong:
        return org.jf.dexlib.Code.Opcode.MUL_LONG_2ADDR;
      case DivLong:
        return org.jf.dexlib.Code.Opcode.DIV_LONG_2ADDR;
      case RemLong:
        return org.jf.dexlib.Code.Opcode.REM_LONG_2ADDR;
      case AndLong:
        return org.jf.dexlib.Code.Opcode.AND_LONG_2ADDR;
      case OrLong:
        return org.jf.dexlib.Code.Opcode.OR_LONG_2ADDR;
      case XorLong:
        return org.jf.dexlib.Code.Opcode.XOR_LONG_2ADDR;
      case ShlLong:
        return org.jf.dexlib.Code.Opcode.SHL_LONG_2ADDR;
      case ShrLong:
        return org.jf.dexlib.Code.Opcode.SHR_LONG_2ADDR;
      case UshrLong:
        return org.jf.dexlib.Code.Opcode.USHR_LONG_2ADDR;
      case AddDouble:
        return org.jf.dexlib.Code.Opcode.ADD_DOUBLE_2ADDR;
      case SubDouble:
        return org.jf.dexlib.Code.Opcode.SUB_DOUBLE_2ADDR;
      case MulDouble:
        return org.jf.dexlib.Code.Opcode.MUL_DOUBLE_2ADDR;
      case DivDouble:
        return org.jf.dexlib.Code.Opcode.DIV_DOUBLE_2ADDR;
      case RemDouble:
        return org.jf.dexlib.Code.Opcode.REM_DOUBLE_2ADDR;
      default:
        return null;
      }
    }
  }

  @Getter private final DexRegister RegTarget1;
  @Getter private final DexRegister RegTarget2;
  @Getter private final DexRegister RegSourceA1;
  @Getter private final DexRegister RegSourceA2;
  @Getter private final DexRegister RegSourceB1;
  @Getter private final DexRegister RegSourceB2;
  @Getter private final Opcode InsnOpcode;

  public DexInstruction_BinaryOpWide(DexRegister target1, DexRegister target2,
                                     DexRegister sourceA1, DexRegister sourceA2,
                                     DexRegister sourceB1, DexRegister sourceB2, Opcode opcode) {
    RegTarget1 = target1;
    RegTarget2 = target2;
    RegSourceA1 = sourceA1;
    RegSourceA2 = sourceA2;
    RegSourceB1 = sourceB1;
    RegSourceB2 = sourceB2;
    InsnOpcode = opcode;
  }

  public DexInstruction_BinaryOpWide(Instruction insn, InstructionParsingState parsingState) throws DexInstructionParsingException {
    if (insn instanceof Instruction23x && Opcode.convert(insn.opcode) != null) {

      val insnBinaryOpWide = (Instruction23x) insn;
      RegTarget1 = parsingState.getRegister(insnBinaryOpWide.getRegisterA());
      RegTarget2 = parsingState.getRegister(insnBinaryOpWide.getRegisterA() + 1);
      RegSourceA1 = parsingState.getRegister(insnBinaryOpWide.getRegisterB());
      RegSourceA2 = parsingState.getRegister(insnBinaryOpWide.getRegisterB() + 1);
      RegSourceB1 = parsingState.getRegister(insnBinaryOpWide.getRegisterC());
      RegSourceB2 = parsingState.getRegister(insnBinaryOpWide.getRegisterC() + 1);
      InsnOpcode = Opcode.convert(insn.opcode);

    } else if (insn instanceof Instruction12x && Opcode.convert(insn.opcode) != null) {

      val insnBinaryOpWide2addr = (Instruction12x) insn;
      RegTarget1 = RegSourceA1 = parsingState.getRegister(insnBinaryOpWide2addr.getRegisterA());
      RegTarget2 = RegSourceA2 = parsingState.getRegister(insnBinaryOpWide2addr.getRegisterA() + 1);
      RegSourceB1 = parsingState.getRegister(insnBinaryOpWide2addr.getRegisterB());
      RegSourceB2 = parsingState.getRegister(insnBinaryOpWide2addr.getRegisterB() + 1);
      InsnOpcode = Opcode.convert(insn.opcode);

    } else
      throw new DexInstructionParsingException("Unknown instruction format or opcode");
  }

  @Override
  public String getOriginalAssembly() {
    return InsnOpcode.getAssemblyName() + " v" + RegTarget1.getId() +
           ", v" + RegSourceA1.getId() + ", v" + RegSourceB1.getId();
  }
}
