package uk.ac.cam.db538.dexter.dex.code.insn;

import lombok.Getter;

public enum Opcode_BinaryOp {
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

  private Opcode_BinaryOp(String assemblyName) {
    AssemblyName = assemblyName;
  }

  public static Opcode_BinaryOp convert(org.jf.dexlib.Code.Opcode opcode) {
    switch (opcode) {
    case ADD_INT:
    case ADD_INT_2ADDR:
      return AddInt;
    case SUB_INT:
    case SUB_INT_2ADDR:
      return SubInt;
    case MUL_INT:
    case MUL_INT_2ADDR:
      return MulInt;
    case DIV_INT:
    case DIV_INT_2ADDR:
      return DivInt;
    case REM_INT:
    case REM_INT_2ADDR:
      return RemInt;
    case AND_INT:
    case AND_INT_2ADDR:
      return AndInt;
    case OR_INT:
    case OR_INT_2ADDR:
      return OrInt;
    case XOR_INT:
    case XOR_INT_2ADDR:
      return XorInt;
    case SHL_INT:
    case SHL_INT_2ADDR:
      return ShlInt;
    case SHR_INT:
    case SHR_INT_2ADDR:
      return ShrInt;
    case USHR_INT:
    case USHR_INT_2ADDR:
      return UshrInt;
    case ADD_FLOAT:
    case ADD_FLOAT_2ADDR:
      return AddFloat;
    case SUB_FLOAT:
    case SUB_FLOAT_2ADDR:
      return SubFloat;
    case MUL_FLOAT:
    case MUL_FLOAT_2ADDR:
      return MulFloat;
    case DIV_FLOAT:
    case DIV_FLOAT_2ADDR:
      return DivFloat;
    case REM_FLOAT:
    case REM_FLOAT_2ADDR:
      return RemFloat;
    default:
      return null;
    }
  }

  public static org.jf.dexlib.Code.Opcode convert(Opcode_BinaryOp opcode) {
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

  public static org.jf.dexlib.Code.Opcode convert2addr(Opcode_BinaryOp opcode) {
    switch (opcode) {
    case AddInt:
      return org.jf.dexlib.Code.Opcode.ADD_INT_2ADDR;
    case SubInt:
      return org.jf.dexlib.Code.Opcode.SUB_INT_2ADDR;
    case MulInt:
      return org.jf.dexlib.Code.Opcode.MUL_INT_2ADDR;
    case DivInt:
      return org.jf.dexlib.Code.Opcode.DIV_INT_2ADDR;
    case RemInt:
      return org.jf.dexlib.Code.Opcode.REM_INT_2ADDR;
    case AndInt:
      return org.jf.dexlib.Code.Opcode.AND_INT_2ADDR;
    case OrInt:
      return org.jf.dexlib.Code.Opcode.OR_INT_2ADDR;
    case XorInt:
      return org.jf.dexlib.Code.Opcode.XOR_INT_2ADDR;
    case ShlInt:
      return org.jf.dexlib.Code.Opcode.SHL_INT_2ADDR;
    case ShrInt:
      return org.jf.dexlib.Code.Opcode.SHR_INT_2ADDR;
    case UshrInt:
      return org.jf.dexlib.Code.Opcode.USHR_INT_2ADDR;
    case AddFloat:
      return org.jf.dexlib.Code.Opcode.ADD_FLOAT_2ADDR;
    case SubFloat:
      return org.jf.dexlib.Code.Opcode.SUB_FLOAT_2ADDR;
    case MulFloat:
      return org.jf.dexlib.Code.Opcode.MUL_FLOAT_2ADDR;
    case DivFloat:
      return org.jf.dexlib.Code.Opcode.DIV_FLOAT_2ADDR;
    case RemFloat:
      return org.jf.dexlib.Code.Opcode.REM_FLOAT_2ADDR;
    default:
      return null;
    }
  }
}