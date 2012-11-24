package uk.ac.cam.db538.dexter.dex.code.insn;

import org.jf.dexlib.Code.Opcode;

import uk.ac.cam.db538.dexter.dex.type.DexPrimitiveType.DexBoolean;
import uk.ac.cam.db538.dexter.dex.type.DexPrimitiveType.DexByte;
import uk.ac.cam.db538.dexter.dex.type.DexPrimitiveType.DexChar;
import uk.ac.cam.db538.dexter.dex.type.DexPrimitiveType.DexInteger;
import uk.ac.cam.db538.dexter.dex.type.DexPrimitiveType.DexShort;
import uk.ac.cam.db538.dexter.dex.type.DexReferenceType;
import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;

public enum Opcode_GetPut {
  Object,
  Int,
  Boolean,
  Byte,
  Char,
  Short;

  public static Opcode_GetPut convert_SGET(org.jf.dexlib.Code.Opcode opcode) {
    switch (opcode) {
    case SGET:
      return Int;
    case SGET_OBJECT:
      return Object;
    case SGET_BOOLEAN:
      return Boolean;
    case SGET_BYTE:
      return Byte;
    case SGET_CHAR:
      return Char;
    case SGET_SHORT:
      return Short;
    default:
      return null;
    }
  }

  public static org.jf.dexlib.Code.Opcode convert_SGET(Opcode_GetPut opcode) {
    switch (opcode) {
    case Int:
      return Opcode.SGET;
    case Object:
      return Opcode.SGET_OBJECT;
    case Boolean:
      return Opcode.SGET_BOOLEAN;
    case Byte:
      return Opcode.SGET_BYTE;
    case Char:
      return Opcode.SGET_CHAR;
    case Short:
      return Opcode.SGET_SHORT;
    default:
      return null;
    }
  }

  public static void checkTypeAgainstOpcode(DexRegisterType type, Opcode_GetPut opcode) {
    boolean typeOK;
    switch(opcode) {
    case Object:
      typeOK = type instanceof DexReferenceType;
      break;
    case Int:
      typeOK = type instanceof DexInteger;
      break;
    case Boolean:
      typeOK = type instanceof DexBoolean;
      break;
    case Byte:
      typeOK = type instanceof DexByte;
      break;
    case Char:
      typeOK = type instanceof DexChar;
      break;
    case Short:
      typeOK = type instanceof DexShort;
      break;
    default:
      typeOK = false;
    }

    if (!typeOK)
      throw new InstructionArgumentException("Source/target type doesn't match the instruction's opcode");
  }
}