package uk.ac.cam.db538.dexter.dex.code;

import lombok.Getter;

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
        return AddLong;
      case SUB_LONG:
        return SubLong;
      case MUL_LONG:
        return MulLong;
      case DIV_LONG:
        return DivLong;
      case REM_LONG:
        return RemLong;
      case AND_LONG:
        return AndLong;
      case OR_LONG:
        return OrLong;
      case XOR_LONG:
        return XorLong;
      case SHL_LONG:
        return ShlLong;
      case SHR_LONG:
        return ShrLong;
      case USHR_LONG:
        return UshrLong;
      case ADD_DOUBLE:
        return AddDouble;
      case SUB_DOUBLE:
        return SubDouble;
      case MUL_DOUBLE:
        return MulDouble;
      case DIV_DOUBLE:
        return DivDouble;
      case REM_DOUBLE:
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

  @Override
  public String getOriginalAssembly() {
    return InsnOpcode.getAssemblyName() + " v" + RegTarget1.getOriginalId() +
           ", v" + RegSourceA1.getOriginalId() + ", v" + RegSourceB1.getOriginalId();
  }
}
