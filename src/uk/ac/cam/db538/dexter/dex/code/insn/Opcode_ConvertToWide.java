package uk.ac.cam.db538.dexter.dex.code.insn;

import lombok.Getter;

public enum Opcode_ConvertToWide {
  IntToLong("int-to-long"),
  IntToDouble("int-to-double"),
  FloatToLong("float-to-long"),
  FloatToDouble("float-to-double");

  @Getter private final String AssemblyName;

  private Opcode_ConvertToWide(String assemblyName) {
    AssemblyName = assemblyName;
  }

  public static Opcode_ConvertToWide convert(org.jf.dexlib.Code.Opcode opcode) {
    switch (opcode) {
    case INT_TO_LONG:
      return IntToLong;
    case INT_TO_DOUBLE:
      return IntToDouble;
    case FLOAT_TO_LONG:
      return FloatToLong;
    case FLOAT_TO_DOUBLE:
      return FloatToDouble;
    default:
      return null;
    }
  }

  public static org.jf.dexlib.Code.Opcode convert(Opcode_ConvertToWide opcode) {
    switch (opcode) {
    case IntToLong:
      return org.jf.dexlib.Code.Opcode.INT_TO_LONG;
    case IntToDouble:
      return org.jf.dexlib.Code.Opcode.INT_TO_DOUBLE;
    case FloatToLong:
      return org.jf.dexlib.Code.Opcode.FLOAT_TO_LONG;
    case FloatToDouble:
      return org.jf.dexlib.Code.Opcode.FLOAT_TO_DOUBLE;
    default:
      return null;
    }
  }
}