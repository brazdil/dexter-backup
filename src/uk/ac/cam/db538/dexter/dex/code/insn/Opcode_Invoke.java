package uk.ac.cam.db538.dexter.dex.code.insn;

import org.jf.dexlib.Code.Opcode;

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

  public static org.jf.dexlib.Code.Opcode convertStandard(Opcode_Invoke opcode) {
    switch (opcode) {
    case Virtual:
      return Opcode.INVOKE_VIRTUAL;
    case Super:
      return Opcode.INVOKE_SUPER;
    case Direct:
      return Opcode.INVOKE_DIRECT;
    case Static:
      return Opcode.INVOKE_STATIC;
    case Interface:
      return Opcode.INVOKE_INTERFACE;
    default:
      return null;
    }
  }

  public static org.jf.dexlib.Code.Opcode convertRange(Opcode_Invoke opcode) {
    switch (opcode) {
    case Virtual:
      return Opcode.INVOKE_VIRTUAL_RANGE;
    case Super:
      return Opcode.INVOKE_SUPER_RANGE;
    case Direct:
      return Opcode.INVOKE_DIRECT_RANGE;
    case Static:
      return Opcode.INVOKE_STATIC_RANGE;
    case Interface:
      return Opcode.INVOKE_INTERFACE_RANGE;
    default:
      return null;
    }
  }
}