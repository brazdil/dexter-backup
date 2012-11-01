package uk.ac.cam.db538.dexter.dex.code;

import lombok.Getter;

public class DexInstruction_UnaryOpWide extends DexInstruction {

  public static enum Opcode {
    NegLong("neg-long"),
    NotLong("not-long"),
    NegDouble("neg-double");

    @Getter private final String AssemblyName;

    private Opcode(String assemblyName) {
      AssemblyName = assemblyName;
    }

    public static Opcode convert(org.jf.dexlib.Code.Opcode opcode) {
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

    public static org.jf.dexlib.Code.Opcode convert(Opcode opcode) {
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

  @Getter private final DexRegister RegTo1;
  @Getter private final DexRegister RegTo2;
  @Getter private final DexRegister RegFrom1;
  @Getter private final DexRegister RegFrom2;
  @Getter private final Opcode InsnOpcode;

  public DexInstruction_UnaryOpWide(DexRegister to1, DexRegister to2, DexRegister from1, DexRegister from2, Opcode opcode) {
    RegTo1 = to1;
    RegTo2 = to2;
    RegFrom1 = from1;
    RegFrom2 = from2;
    InsnOpcode = opcode;
  }

  @Override
  public String getOriginalAssembly() {
    return InsnOpcode.getAssemblyName() + " v" + RegTo1.getId() + ", v" + RegFrom1.getId();
  }
}
