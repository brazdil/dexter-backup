package uk.ac.cam.db538.dexter.dex.code.insn;

import lombok.Getter;

public enum Opcode_CompareWide {
  CmplDouble("cmpl-double"),
  CmpgDouble("cmpg-double"),
  CmpLong("cmp-long");

  @Getter private final String AssemblyName;

  private Opcode_CompareWide(String assemblyName) {
    AssemblyName = assemblyName;
  }

  public static Opcode_CompareWide convert(org.jf.dexlib.Code.Opcode opcode) {
    switch (opcode) {
    case CMPL_DOUBLE:
      return CmplDouble;
    case CMPG_DOUBLE:
      return CmpgDouble;
    case CMP_LONG:
      return CmpLong;
    default:
      return null;
    }
  }

  public static org.jf.dexlib.Code.Opcode convert(Opcode_CompareWide opcode) {
    switch (opcode) {
    case CmplDouble:
      return org.jf.dexlib.Code.Opcode.CMPL_DOUBLE;
    case CmpgDouble:
      return org.jf.dexlib.Code.Opcode.CMPG_DOUBLE;
    case CmpLong:
      return org.jf.dexlib.Code.Opcode.CMP_LONG;
    default:
      return null;
    }
  }
}