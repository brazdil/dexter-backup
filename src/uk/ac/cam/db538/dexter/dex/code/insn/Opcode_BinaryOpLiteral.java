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
}