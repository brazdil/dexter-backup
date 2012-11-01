package uk.ac.cam.db538.dexter.dex.code;

import lombok.Getter;

public class DexInstruction_UnaryOp extends DexInstruction {

  public static enum Opcode {
    NegInt("neg-int"),
    NotInt("not-int"),
    NegFloat("neg-float");

    @Getter private final String AssemblyName;

    private Opcode(String assemblyName) {
      AssemblyName = assemblyName;
    }

    public static Opcode convert(org.jf.dexlib.Code.Opcode opcode) {
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

    public static org.jf.dexlib.Code.Opcode convert(Opcode opcode) {
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

  @Getter private final DexRegister RegTo;
  @Getter private final DexRegister RegFrom;
  @Getter private final Opcode InsnOpcode;

  public DexInstruction_UnaryOp(DexRegister to, DexRegister from, Opcode opcode) {
    RegTo = to;
    RegFrom = from;
    InsnOpcode = opcode;
  }

  @Override
  public String getOriginalAssembly() {
    return InsnOpcode.getAssemblyName() + " v" + RegTo.getId() + ", v" + RegFrom.getId();
  }
}
