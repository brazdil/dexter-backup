package uk.ac.cam.db538.dexter.dex.code.insn;

import lombok.Getter;

import org.jf.dexlib.Code.Opcode;

import uk.ac.cam.db538.dexter.dex.type.DexType_Boolean;
import uk.ac.cam.db538.dexter.dex.type.DexType_Byte;
import uk.ac.cam.db538.dexter.dex.type.DexType_Char;
import uk.ac.cam.db538.dexter.dex.type.DexType_Float;
import uk.ac.cam.db538.dexter.dex.type.DexType_Integer;
import uk.ac.cam.db538.dexter.dex.type.DexType_Reference;
import uk.ac.cam.db538.dexter.dex.type.DexType_Register;
import uk.ac.cam.db538.dexter.dex.type.DexType_Short;

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
    default:
      return null;
    }
  }

  public static org.jf.dexlib.Code.Opcode convert_IGET(Opcode_GetPut opcode) {
    switch (opcode) {
    case IntFloat:
      return Opcode.IGET;
    case Object:
      return Opcode.IGET_OBJECT;
    case Boolean:
      return Opcode.IGET_BOOLEAN;
    case Byte:
      return Opcode.IGET_BYTE;
    case Char:
      return Opcode.IGET_CHAR;
    case Short:
      return Opcode.IGET_SHORT;
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
    default:
      return null;
    }
  }

  public static org.jf.dexlib.Code.Opcode convert_IPUT(Opcode_GetPut opcode) {
    switch (opcode) {
    case IntFloat:
      return Opcode.IPUT;
    case Object:
      return Opcode.IPUT_OBJECT;
    case Boolean:
      return Opcode.IPUT_BOOLEAN;
    case Byte:
      return Opcode.IPUT_BYTE;
    case Char:
      return Opcode.IPUT_CHAR;
    case Short:
      return Opcode.IPUT_SHORT;
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
    default:
      return null;
    }
  }

  public static org.jf.dexlib.Code.Opcode convert_AGET(Opcode_GetPut opcode) {
    switch (opcode) {
    case IntFloat:
      return Opcode.AGET;
    case Object:
      return Opcode.AGET_OBJECT;
    case Boolean:
      return Opcode.AGET_BOOLEAN;
    case Byte:
      return Opcode.AGET_BYTE;
    case Char:
      return Opcode.AGET_CHAR;
    case Short:
      return Opcode.AGET_SHORT;
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
    default:
      return null;
    }
  }

  public static org.jf.dexlib.Code.Opcode convert_APUT(Opcode_GetPut opcode) {
    switch (opcode) {
    case IntFloat:
      return Opcode.APUT;
    case Object:
      return Opcode.APUT_OBJECT;
    case Boolean:
      return Opcode.APUT_BOOLEAN;
    case Byte:
      return Opcode.APUT_BYTE;
    case Char:
      return Opcode.APUT_CHAR;
    case Short:
      return Opcode.APUT_SHORT;
    default:
      return null;
    }
  }

  public static void checkTypeAgainstOpcode(DexType_Register type, Opcode_GetPut opcode) {
    boolean typeOK;
    switch(opcode) {
    case Object:
      typeOK = type instanceof DexType_Reference;
      break;
    case IntFloat:
      typeOK = (type instanceof DexType_Integer) || (type instanceof DexType_Float);
      break;
    case Boolean:
      typeOK = type instanceof DexType_Boolean;
      break;
    case Byte:
      typeOK = type instanceof DexType_Byte;
      break;
    case Char:
      typeOK = type instanceof DexType_Char;
      break;
    case Short:
      typeOK = type instanceof DexType_Short;
      break;
    default:
      typeOK = false;
    }

    if (!typeOK)
      throw new InstructionArgumentException("Source/target type doesn't match the instruction's opcode");
  }

  public static Opcode_GetPut getOpcodeFromType(DexType_Register type) {
    if (type instanceof DexType_Reference)
      return Object;
    else if (type instanceof DexType_Integer || type instanceof DexType_Float)
      return IntFloat;
    else if (type instanceof DexType_Boolean)
      return Boolean;
    else if (type instanceof DexType_Byte)
      return Byte;
    else if (type instanceof DexType_Char)
      return Char;
    else if (type instanceof DexType_Short)
      return Short;
    else
      throw new InstructionArgumentException("Type given to instruction is not supported");
  }
}