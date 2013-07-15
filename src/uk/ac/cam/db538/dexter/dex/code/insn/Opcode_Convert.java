package uk.ac.cam.db538.dexter.dex.code.insn;

import uk.ac.cam.db538.dexter.dex.code.reg.RegisterWidth;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum Opcode_Convert {
  IntToFloat("int-to-float", RegisterWidth.SINGLE, RegisterWidth.SINGLE),
  FloatToInt("float-to-int", RegisterWidth.SINGLE, RegisterWidth.SINGLE),
  IntToByte("int-to-byte", RegisterWidth.SINGLE, RegisterWidth.SINGLE),
  IntToChar("int-to-char", RegisterWidth.SINGLE, RegisterWidth.SINGLE),
  IntToShort("int-to-short", RegisterWidth.SINGLE, RegisterWidth.SINGLE),
  LongToDouble("long-to-double", RegisterWidth.WIDE, RegisterWidth.WIDE),
  DoubleToLong("double-to-long", RegisterWidth.WIDE, RegisterWidth.WIDE),
  LongToInt("long-to-int", RegisterWidth.WIDE, RegisterWidth.SINGLE),
  LongToFloat("long-to-float", RegisterWidth.WIDE, RegisterWidth.SINGLE),
  DoubleToInt("double-to-int", RegisterWidth.WIDE, RegisterWidth.SINGLE),
  DoubleToFloat("double-to-float", RegisterWidth.WIDE, RegisterWidth.SINGLE),
  IntToLong("int-to-long", RegisterWidth.SINGLE, RegisterWidth.WIDE),
  IntToDouble("int-to-double", RegisterWidth.SINGLE, RegisterWidth.WIDE),
  FloatToLong("float-to-long", RegisterWidth.SINGLE, RegisterWidth.WIDE),
  FloatToDouble("float-to-double", RegisterWidth.SINGLE, RegisterWidth.WIDE);
  
  @Getter private final String AssemblyName;
  @Getter private final RegisterWidth widthFrom;
  @Getter private final RegisterWidth widthTo;

  public static Opcode_Convert convert(org.jf.dexlib.Code.Opcode opcode) {
    switch (opcode) {
    case INT_TO_FLOAT:
      return IntToFloat;
    case FLOAT_TO_INT:
      return FloatToInt;
    case INT_TO_BYTE:
      return IntToByte;
    case INT_TO_CHAR:
      return IntToChar;
    case INT_TO_SHORT:
      return IntToShort;
    case LONG_TO_DOUBLE:
      return LongToDouble;
    case DOUBLE_TO_LONG:
      return DoubleToLong;
    case LONG_TO_INT:
      return LongToInt;
    case LONG_TO_FLOAT:
      return LongToFloat;
    case DOUBLE_TO_INT:
      return DoubleToInt;
    case DOUBLE_TO_FLOAT:
      return DoubleToFloat;
    case INT_TO_LONG:
      return IntToLong;
    case INT_TO_DOUBLE:
      return IntToDouble;
    case FLOAT_TO_LONG:
      return FloatToLong;
    case FLOAT_TO_DOUBLE:
      return FloatToDouble;
    default:
      return null;
    }
  }
}