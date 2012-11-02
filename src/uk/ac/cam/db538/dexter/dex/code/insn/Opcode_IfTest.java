package uk.ac.cam.db538.dexter.dex.code.insn;

public enum Opcode_IfTest {
  eq,
  ne,
  lt,
  ge,
  gt,
  le;

  public static Opcode_IfTest convert(org.jf.dexlib.Code.Opcode opcode) {
    switch (opcode) {
    case IF_EQ:
      return eq;
    case IF_NE:
      return ne;
    case IF_LT:
      return lt;
    case IF_GE:
      return ge;
    case IF_GT:
      return gt;
    case IF_LE:
      return le;
    default:
      return null;
    }
  }

  public static org.jf.dexlib.Code.Opcode convert(Opcode_IfTest opcode) {
    switch (opcode) {
    case eq:
      return org.jf.dexlib.Code.Opcode.IF_EQ;
    case ne:
      return org.jf.dexlib.Code.Opcode.IF_NE;
    case lt:
      return org.jf.dexlib.Code.Opcode.IF_LT;
    case ge:
      return org.jf.dexlib.Code.Opcode.IF_GE;
    case gt:
      return org.jf.dexlib.Code.Opcode.IF_GT;
    case le:
      return org.jf.dexlib.Code.Opcode.IF_LE;
    default:
      return null;
    }
  }
}