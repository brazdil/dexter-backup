package uk.ac.cam.db538.dexter.dex.code.insn;


public enum Opcode_Invoke {
  Virtual,
  Super,
  Direct,
  Static,
  Interface;

  public static Opcode_Invoke convert(org.jf.dexlib.Code.Opcode opcode) {
    switch (opcode) {
    case INVOKE_VIRTUAL:
    case INVOKE_VIRTUAL_RANGE:
      return Virtual;
    case INVOKE_SUPER:
    case INVOKE_SUPER_RANGE:
      return Super;
    case INVOKE_DIRECT:
    case INVOKE_DIRECT_RANGE:
      return Direct;
    case INVOKE_STATIC:
    case INVOKE_STATIC_RANGE:
      return Static;
    case INVOKE_INTERFACE:
    case INVOKE_INTERFACE_RANGE:
      return Interface;
    default:
      return null;
    }
  }

  public boolean isStatic() {
    return this == Opcode_Invoke.Static;
  }
}