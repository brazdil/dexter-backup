package uk.ac.cam.db538.dexter.dex.code.insn;

import lombok.Getter;

import org.jf.dexlib.Code.Opcode;

import uk.ac.cam.db538.dexter.dex.type.DexPrimitiveType.DexBoolean;
import uk.ac.cam.db538.dexter.dex.type.DexPrimitiveType.DexByte;
import uk.ac.cam.db538.dexter.dex.type.DexPrimitiveType.DexChar;
import uk.ac.cam.db538.dexter.dex.type.DexPrimitiveType.DexFloat;
import uk.ac.cam.db538.dexter.dex.type.DexPrimitiveType.DexInteger;
import uk.ac.cam.db538.dexter.dex.type.DexPrimitiveType.DexShort;
import uk.ac.cam.db538.dexter.dex.type.DexReferenceType;
import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;

public enum Opcode_GetPut {
  Object("object"),
  IntFloat("int-float"),
  Boolean("boolean"),
  Byte("byte"),
  Char("char"),
  Short("short");

  @Getter private final String AssemblyName;

  private Opcode_GetPut(String assemblyName) {
    AssemblyName = assemblyName;
  }

  public static Opcode_GetPut convert_SGET(org.jf.dexlib.Code.Opcode opcode) {
    switch (opcode) {
    case SGET:
      return IntFloat;
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
    case IntFloat:
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

  public static Opcode_GetPut convert_SPUT(org.jf.dexlib.Code.Opcode opcode) {
    switch (opcode) {
    case SPUT:
      return IntFloat;
    case SPUT_OBJECT:
      return Object;
    case SPUT_BOOLEAN:
      return Boolean;
    case SPUT_BYTE:
      return Byte;
    case SPUT_CHAR:
      return Char;
    case SPUT_SHORT:
      return Short;
    default:
      return null;
    }
  }

  public static org.jf.dexlib.Code.Opcode convert_SPUT(Opcode_GetPut opcode) {
    switch (opcode) {
    case IntFloat:
      return Opcode.SPUT;
    case Object:
      return Opcode.SPUT_OBJECT;
    case Boolean:
      return Opcode.SPUT_BOOLEAN;
    case Byte:
      return Opcode.SPUT_BYTE;
    case Char:
      return Opcode.SPUT_CHAR;
    case Short:
      return Opcode.SPUT_SHORT;
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
    case IntFloat:
      typeOK = (type instanceof DexInteger) || (type instanceof DexFloat);
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

  public static Opcode_GetPut getOpcodeFromType(DexRegisterType type) {
    if (type instanceof DexReferenceType)
      return Object;
    else if (type instanceof DexInteger || type instanceof DexFloat)
      return IntFloat;
    else if (type instanceof DexBoolean)
      return Boolean;
    else if (type instanceof DexByte)
      return Byte;
    else if (type instanceof DexChar)
      return Char;
    else if (type instanceof DexShort)
      return Short;
    else
      throw new InstructionArgumentException("Type given to instruction is not supported");
  }
}