package uk.ac.cam.db538.dexter.dex.code.insn;

import lombok.Getter;

public enum Opcode_ConvertFromWide {
  LongToInt("long-to-int"),
  LongToFloat("long-to-float"),
  DoubleToInt("double-to-int"),
  DoubleToFloat("double-to-float");

  @Getter private final String AssemblyName;

  private Opcode_ConvertFromWide(String assemblyName) {
    AssemblyName = assemblyName;
  }

  public static Opcode_ConvertFromWide convert(org.jf.dexlib.Code.Opcode opcode) {
    switch (opcode) {
    case LONG_TO_INT:
      return LongToInt;
    case LONG_TO_FLOAT:
      return LongToFloat;
    case DOUBLE_TO_INT:
      return DoubleToInt;
    case DOUBLE_TO_FLOAT:
      return DoubleToFloat;
    default:
      return null;
    }
  }

  public static org.jf.dexlib.Code.Opcode convert(Opcode_ConvertFromWide opcode) {
    switch (opcode) {
    case LongToInt:
      return org.jf.dexlib.Code.Opcode.LONG_TO_INT;
    case LongToFloat:
      return org.jf.dexlib.Code.Opcode.LONG_TO_FLOAT;
    case DoubleToInt:
      return org.jf.dexlib.Code.Opcode.DOUBLE_TO_INT;
    case DoubleToFloat:
      return org.jf.dexlib.Code.Opcode.DOUBLE_TO_FLOAT;
    default:
      return null;
    }
  }
}