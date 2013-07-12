package uk.ac.cam.db538.dexter.dex.code.insn;

import lombok.Getter;

import org.jf.dexlib.Code.Opcode;

import uk.ac.cam.db538.dexter.dex.type.DexBoolean;
import uk.ac.cam.db538.dexter.dex.type.DexByte;
import uk.ac.cam.db538.dexter.dex.type.DexChar;
import uk.ac.cam.db538.dexter.dex.type.DexDouble;
import uk.ac.cam.db538.dexter.dex.type.DexFloat;
import uk.ac.cam.db538.dexter.dex.type.DexInteger;
import uk.ac.cam.db538.dexter.dex.type.DexLong;
import uk.ac.cam.db538.dexter.dex.type.DexReferenceType;
import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;
import uk.ac.cam.db538.dexter.dex.type.DexShort;

public enum Opcode_GetPut {
  Object("-object"),
  IntFloat("-int-float"),
  Boolean("-boolean"),
  Byte("-byte"),
  Char("-char"),
  Short("-short"),
  Wide("-wide");

  @Getter private final String AsmSuffix;

  private Opcode_GetPut(String asmSuffix) {
	  AsmSuffix = asmSuffix;
  }

  public static Opcode_GetPut convert_SGET(Opcode opcode) {
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
    case SGET_WIDE:
      return Wide;
    default:
      return null;
    }
  }

  public static Opcode_GetPut convert_SPUT(Opcode opcode) {
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
    case SPUT_WIDE:
      return Wide;
    default:
      return null;
    }
  }

  public static Opcode_GetPut convert_IGET(org.jf.dexlib.Code.Opcode opcode) {
    switch (opcode) {
    case IGET:
      return IntFloat;
    case IGET_OBJECT:
      return Object;
    case IGET_BOOLEAN:
      return Boolean;
    case IGET_BYTE:
      return Byte;
    case IGET_CHAR:
      return Char;
    case IGET_SHORT:
      return Short;
    case IGET_WIDE:
      return Wide;
    default:
      return null;
    }
  }

  public static Opcode_GetPut convert_IPUT(org.jf.dexlib.Code.Opcode opcode) {
    switch (opcode) {
    case IPUT:
      return IntFloat;
    case IPUT_OBJECT:
      return Object;
    case IPUT_BOOLEAN:
      return Boolean;
    case IPUT_BYTE:
      return Byte;
    case IPUT_CHAR:
      return Char;
    case IPUT_SHORT:
      return Short;
    case IPUT_WIDE:
      return Wide;
    default:
      return null;
    }
  }
  
  public static Opcode_GetPut convert_AGET(org.jf.dexlib.Code.Opcode opcode) {
    switch (opcode) {
    case AGET:
      return IntFloat;
    case AGET_OBJECT:
      return Object;
    case AGET_BOOLEAN:
      return Boolean;
    case AGET_BYTE:
      return Byte;
    case AGET_CHAR:
      return Char;
    case AGET_SHORT:
      return Short;
    case AGET_WIDE:
      return Wide;
    default:
      return null;
    }
  }

  public static Opcode_GetPut convert_APUT(org.jf.dexlib.Code.Opcode opcode) {
    switch (opcode) {
    case APUT:
      return IntFloat;
    case APUT_OBJECT:
      return Object;
    case APUT_BOOLEAN:
      return Boolean;
    case APUT_BYTE:
      return Byte;
    case APUT_CHAR:
      return Char;
    case APUT_SHORT:
      return Short;
    case APUT_WIDE:
      return Wide;
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
    case Wide:
      typeOK = (type instanceof DexLong) || (type instanceof DexDouble);
      break;
    default:
      typeOK = false;
    }

    if (!typeOK)
      throw new Error("Source/target type doesn't match the instruction's opcode");
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
    else if (type instanceof DexLong || type instanceof DexDouble)
      return Wide;
    else
      throw new Error("Type given to instruction is not supported");
  }
}