package uk.ac.cam.db538.dexter.dex.code.insn;

import lombok.Getter;

public enum Opcode_Convert {
  IntToFloat("int-to-float"),
  FloatToInt("float-to-int"),
  IntToByte("int-to-byte"),
  IntToChar("int-to-char"),
  IntToShort("int-to-short");

  @Getter private final String AssemblyName;

  private Opcode_Convert(String assemblyName) {
    AssemblyName = assemblyName;
  }

  public static Opcode_Convert convert(org.jf.dexlib.Code.Opcode opcode) {
    switch (opcode) {
    case INT_TO_FLOAT:
      return IntToFloat;
    case FLOAT_TO_INT:
      return FloatToInt;
    case INT_TO_BYTE:
      return IntToByte;
    case INT_TO_CHAR:
      return IntToChar;
    case INT_TO_SHORT:
      return IntToShort;
    default:
      return null;
    }
  }

  public static org.jf.dexlib.Code.Opcode convert(Opcode_Convert opcode) {
    switch (opcode) {
    case IntToFloat:
      return org.jf.dexlib.Code.Opcode.INT_TO_FLOAT;
    case FloatToInt:
      return org.jf.dexlib.Code.Opcode.FLOAT_TO_INT;
    case IntToByte:
      return org.jf.dexlib.Code.Opcode.INT_TO_BYTE;
    case IntToChar:
      return org.jf.dexlib.Code.Opcode.INT_TO_CHAR;
    case IntToShort:
      return org.jf.dexlib.Code.Opcode.INT_TO_SHORT;
    default:
      return null;
    }
  }
}