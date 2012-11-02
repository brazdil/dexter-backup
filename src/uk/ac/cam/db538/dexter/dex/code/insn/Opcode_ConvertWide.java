package uk.ac.cam.db538.dexter.dex.code.insn;

import lombok.Getter;

public enum Opcode_ConvertWide {
  LongToDouble("long-to-double"),
  DoubleToLong("double-to-long");

  @Getter private final String AssemblyName;

  private Opcode_ConvertWide(String assemblyName) {
    AssemblyName = assemblyName;
  }

  public static Opcode_ConvertWide convert(org.jf.dexlib.Code.Opcode opcode) {
    switch (opcode) {
    case LONG_TO_DOUBLE:
      return LongToDouble;
    case DOUBLE_TO_LONG:
      return DoubleToLong;
    default:
      return null;
    }
  }

  public static org.jf.dexlib.Code.Opcode convert(Opcode_ConvertWide opcode) {
    switch (opcode) {
    case LongToDouble:
      return org.jf.dexlib.Code.Opcode.LONG_TO_DOUBLE;
    case DoubleToLong:
      return org.jf.dexlib.Code.Opcode.DOUBLE_TO_LONG;
    default:
      return null;
    }
  }
}