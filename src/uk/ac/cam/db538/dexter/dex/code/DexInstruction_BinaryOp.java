package uk.ac.cam.db538.dexter.dex.code;

import lombok.Getter;

public class DexInstruction_BinaryOp extends DexInstruction {

  // CAREFUL: produce /addr2 instructions if target and first
  // registers are equal; for commutative instructions,
  // check the second as well

  public static enum Opcode {
    AddInt("add-int"),
    SubInt("sub-int"),
    MulInt("mul-int"),
    DivInt("div-int"),
    RemInt("rem-int"),
    AndInt("and-int"),
    OrInt("or-int"),
    XorInt("xor-int"),
    ShlInt("shl-int"),
    ShrInt("shr-int"),
    UshrInt("ushr-int"),
    AddFloat("add-float"),
    SubFloat("sub-float"),
    MulFloat("mul-float"),
    DivFloat("div-float"),
    RemFloat("rem-float");

    @Getter private final String AssemblyName;

    private Opcode(String assemblyName) {
      AssemblyName = assemblyName;
    }

    public static Opcode convert(org.jf.dexlib.Code.Opcode opcode) {
      switch (opcode) {
      case ADD_INT:
        return AddInt;
      case SUB_INT:
        return SubInt;
      case MUL_INT:
        return MulInt;
      case DIV_INT:
        return DivInt;
      case REM_INT:
        return RemInt;
      case AND_INT:
        return AndInt;
      case OR_INT:
        return OrInt;
      case XOR_INT:
        return XorInt;
      case SHL_INT:
        return ShlInt;
      case SHR_INT:
        return ShrInt;
      case USHR_INT:
        return UshrInt;
      case ADD_FLOAT:
        return AddFloat;
      case SUB_FLOAT:
        return SubFloat;
      case MUL_FLOAT:
        return MulFloat;
      case DIV_FLOAT:
        return DivFloat;
      case REM_FLOAT:
        return RemFloat;
      default:
        return null;
      }
    }

    public static org.jf.dexlib.Code.Opcode convert(Opcode opcode) {
      switch (opcode) {
      case AddInt:
        return org.jf.dexlib.Code.Opcode.ADD_INT;
      case SubInt:
        return org.jf.dexlib.Code.Opcode.SUB_INT;
      case MulInt:
        return org.jf.dexlib.Code.Opcode.MUL_INT;
      case DivInt:
        return org.jf.dexlib.Code.Opcode.DIV_INT;
      case RemInt:
        return org.jf.dexlib.Code.Opcode.REM_INT;
      case AndInt:
        return org.jf.dexlib.Code.Opcode.AND_INT;
      case OrInt:
        return org.jf.dexlib.Code.Opcode.OR_INT;
      case XorInt:
        return org.jf.dexlib.Code.Opcode.XOR_INT;
      case ShlInt:
        return org.jf.dexlib.Code.Opcode.SHL_INT;
      case ShrInt:
        return org.jf.dexlib.Code.Opcode.SHR_INT;
      case UshrInt:
        return org.jf.dexlib.Code.Opcode.USHR_INT;
      case AddFloat:
        return org.jf.dexlib.Code.Opcode.ADD_FLOAT;
      case SubFloat:
        return org.jf.dexlib.Code.Opcode.SUB_FLOAT;
      case MulFloat:
        return org.jf.dexlib.Code.Opcode.MUL_FLOAT;
      case DivFloat:
        return org.jf.dexlib.Code.Opcode.DIV_FLOAT;
      case RemFloat:
        return org.jf.dexlib.Code.Opcode.REM_FLOAT;
      default:
        return null;
      }
    }
  }

  @Getter private final DexRegister RegTarget;
  @Getter private final DexRegister RegSourceA;
  @Getter private final DexRegister RegSourceB;
  @Getter private final Opcode InsnOpcode;

  public DexInstruction_BinaryOp(DexRegister target, DexRegister sourceA, DexRegister sourceB, Opcode opcode) {
    RegTarget = target;
    RegSourceA = sourceA;
    RegSourceB = sourceB;
    InsnOpcode = opcode;
  }

  @Override
  public String getOriginalAssembly() {
    return InsnOpcode.getAssemblyName() + " v" + RegTarget.getOriginalId() +
           ", v" + RegSourceA.getOriginalId() + ", v" + RegSourceB.getOriginalId();
  }
}
