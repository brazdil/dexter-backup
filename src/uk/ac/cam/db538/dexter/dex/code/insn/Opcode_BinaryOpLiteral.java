package uk.ac.cam.db538.dexter.dex.code.insn;


public enum Opcode_BinaryOpLiteral {
  Add,
  Rsub,
  Mul,
  Div,
  Rem,
  And,
  Or,
  Xor,
  Shl,
  Shr,
  Ushr;

  public static Opcode_BinaryOpLiteral convert(org.jf.dexlib.Code.Opcode opcode) {
    switch (opcode) {
    case ADD_INT_LIT16:
    case ADD_INT_LIT8:
      return Add;
    case RSUB_INT:
    case RSUB_INT_LIT8:
      return Rsub;
    case MUL_INT_LIT16:
    case MUL_INT_LIT8:
      return Mul;
    case DIV_INT_LIT16:
    case DIV_INT_LIT8:
      return Div;
    case REM_INT_LIT16:
    case REM_INT_LIT8:
      return Rem;
    case AND_INT_LIT16:
    case AND_INT_LIT8:
      return And;
    case OR_INT_LIT16:
    case OR_INT_LIT8:
      return Or;
    case XOR_INT_LIT16:
    case XOR_INT_LIT8:
      return Xor;
    case SHL_INT_LIT8:
      return Shl;
    case SHR_INT_LIT8:
      return Shr;
    case USHR_INT_LIT8:
      return Ushr;
    default:
      return null;
    }
  }

  public static org.jf.dexlib.Code.Opcode convert_lit16(Opcode_BinaryOpLiteral opcode) {
    switch (opcode) {
    case Add:
      return org.jf.dexlib.Code.Opcode.ADD_INT_LIT16;
    case Rsub:
      return org.jf.dexlib.Code.Opcode.RSUB_INT;
    case Mul:
      return org.jf.dexlib.Code.Opcode.MUL_INT_LIT16;
    case Div:
      return org.jf.dexlib.Code.Opcode.DIV_INT_LIT16;
    case Rem:
      return org.jf.dexlib.Code.Opcode.REM_INT_LIT16;
    case And:
      return org.jf.dexlib.Code.Opcode.AND_INT_LIT16;
    case Or:
      return org.jf.dexlib.Code.Opcode.OR_INT_LIT16;
    case Xor:
      return org.jf.dexlib.Code.Opcode.XOR_INT_LIT16;
    default:
      return null;
    }
  }

  public static org.jf.dexlib.Code.Opcode convert_lit8(Opcode_BinaryOpLiteral opcode) {
    switch (opcode) {
    case Add:
      return org.jf.dexlib.Code.Opcode.ADD_INT_LIT8;
    case Rsub:
      return org.jf.dexlib.Code.Opcode.RSUB_INT_LIT8;
    case Mul:
      return org.jf.dexlib.Code.Opcode.MUL_INT_LIT8;
    case Div:
      return org.jf.dexlib.Code.Opcode.DIV_INT_LIT8;
    case Rem:
      return org.jf.dexlib.Code.Opcode.REM_INT_LIT8;
    case And:
      return org.jf.dexlib.Code.Opcode.AND_INT_LIT8;
    case Or:
      return org.jf.dexlib.Code.Opcode.OR_INT_LIT8;
    case Xor:
      return org.jf.dexlib.Code.Opcode.XOR_INT_LIT8;
    case Shl:
      return org.jf.dexlib.Code.Opcode.SHL_INT_LIT8;
    case Shr:
      return org.jf.dexlib.Code.Opcode.SHR_INT_LIT8;
    case Ushr:
      return org.jf.dexlib.Code.Opcode.USHR_INT_LIT8;
    default:
      return null;
    }
  }
}