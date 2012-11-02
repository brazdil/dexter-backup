package uk.ac.cam.db538.dexter.dex.code.insn;

import lombok.Getter;

public enum Opcode_UnaryOpWide {
  NegLong("neg-long"),
  NotLong("not-long"),
  NegDouble("neg-double");

  @Getter private final String AssemblyName;

  private Opcode_UnaryOpWide(String assemblyName) {
    AssemblyName = assemblyName;
  }

  public static Opcode_UnaryOpWide convert(org.jf.dexlib.Code.Opcode opcode) {
    switch (opcode) {
    case NEG_LONG:
      return NegLong;
    case NOT_LONG:
      return NotLong;
    case NEG_DOUBLE:
      return NegDouble;
    default:
      return null;
    }
  }

  public static org.jf.dexlib.Code.Opcode convert(Opcode_UnaryOpWide opcode) {
    switch (opcode) {
    case NegLong:
      return org.jf.dexlib.Code.Opcode.NEG_LONG;
    case NotLong:
      return org.jf.dexlib.Code.Opcode.NOT_LONG;
    case NegDouble:
      return org.jf.dexlib.Code.Opcode.NEG_DOUBLE;
    default:
      return null;
    }
  }
}