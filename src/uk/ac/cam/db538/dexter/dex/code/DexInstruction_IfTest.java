package uk.ac.cam.db538.dexter.dex.code;

import lombok.Getter;

public class DexInstruction_IfTest extends DexInstruction {

  public static enum Opcode {
    eq,
    ne,
    lt,
    ge,
    gt,
    le;

    public static Opcode convert(org.jf.dexlib.Code.Opcode opcode) {
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

    public static org.jf.dexlib.Code.Opcode convert(Opcode opcode) {
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

  @Getter private final DexRegister RegA;
  @Getter private final DexRegister RegB;
  @Getter private final DexLabel Target;
  @Getter private final Opcode InsnOpcode;

  public DexInstruction_IfTest(DexRegister regA, DexRegister regB, DexLabel target, Opcode opcode) {
    RegA = regA;
    RegB = regB;
    Target = target;
    InsnOpcode = opcode;
  }

  @Override
  public String getOriginalAssembly() {
    return "if-" + InsnOpcode.name() + " v" + RegA.getId() +
           ", v" + RegB.getId() + ", L" + Target.getOriginalOffset();
  }
}
