package uk.ac.cam.db538.dexter.dex.code.insn;

import lombok.Getter;

public enum Opcode_UnaryOp {
  NegInt("neg-int"),
  NotInt("not-int"),
  NegFloat("neg-float");

  @Getter private final String AssemblyName;

  private Opcode_UnaryOp(String assemblyName) {
    AssemblyName = assemblyName;
  }

  public static Opcode_UnaryOp convert(org.jf.dexlib.Code.Opcode opcode) {
    switch (opcode) {
    case NEG_INT:
      return NegInt;
    case NOT_INT:
      return NotInt;
    case NEG_FLOAT:
      return NegFloat;
    default:
      return null;
    }
  }

  public static org.jf.dexlib.Code.Opcode convert(Opcode_UnaryOp opcode) {
    switch (opcode) {
    case NegInt:
      return org.jf.dexlib.Code.Opcode.NEG_INT;
    case NotInt:
      return org.jf.dexlib.Code.Opcode.NOT_INT;
    case NegFloat:
      return org.jf.dexlib.Code.Opcode.NEG_FLOAT;
    default:
      return null;
    }
  }
}