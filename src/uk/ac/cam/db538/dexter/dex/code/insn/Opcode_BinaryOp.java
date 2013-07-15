package uk.ac.cam.db538.dexter.dex.code.insn;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.ac.cam.db538.dexter.dex.code.reg.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.reg.DexTaintRegister;
import uk.ac.cam.db538.dexter.dex.code.reg.RegisterWidth;

@AllArgsConstructor
public enum Opcode_BinaryOp {
  AddInt("add-int", RegisterWidth.SINGLE),
  SubInt("sub-int", RegisterWidth.SINGLE),
  MulInt("mul-int", RegisterWidth.SINGLE),
  DivInt("div-int", RegisterWidth.SINGLE),
  RemInt("rem-int", RegisterWidth.SINGLE),
  AndInt("and-int", RegisterWidth.SINGLE),
  OrInt("or-int", RegisterWidth.SINGLE),
  XorInt("xor-int", RegisterWidth.SINGLE),
  ShlInt("shl-int", RegisterWidth.SINGLE),
  ShrInt("shr-int", RegisterWidth.SINGLE),
  UshrInt("ushr-int", RegisterWidth.SINGLE),
  AddFloat("add-float", RegisterWidth.SINGLE),
  SubFloat("sub-float", RegisterWidth.SINGLE),
  MulFloat("mul-float", RegisterWidth.SINGLE),
  DivFloat("div-float", RegisterWidth.SINGLE),
  RemFloat("rem-float", RegisterWidth.SINGLE),
  AddLong("add-long", RegisterWidth.WIDE),
  SubLong("sub-long", RegisterWidth.WIDE),
  MulLong("mul-long", RegisterWidth.WIDE),
  DivLong("div-long", RegisterWidth.WIDE),
  RemLong("rem-long", RegisterWidth.WIDE),
  AndLong("and-long", RegisterWidth.WIDE),
  OrLong("or-long", RegisterWidth.WIDE),
  XorLong("xor-long", RegisterWidth.WIDE),
  ShlLong("shl-long", RegisterWidth.WIDE),
  ShrLong("shr-long", RegisterWidth.WIDE),
  UshrLong("ushr-long", RegisterWidth.WIDE),
  AddDouble("add-double", RegisterWidth.WIDE),
  SubDouble("sub-double", RegisterWidth.WIDE),
  MulDouble("mul-double", RegisterWidth.WIDE),
  DivDouble("div-double", RegisterWidth.WIDE),
  RemDouble("rem-double", RegisterWidth.WIDE);

  @Getter private final String AssemblyName;
  @Getter private final RegisterWidth width;
  
  public static Opcode_BinaryOp convert(org.jf.dexlib.Code.Opcode opcode) {
    switch (opcode) {
    case ADD_INT:
    case ADD_INT_2ADDR:
      return AddInt;
    case SUB_INT:
    case SUB_INT_2ADDR:
      return SubInt;
    case MUL_INT:
    case MUL_INT_2ADDR:
      return MulInt;
    case DIV_INT:
    case DIV_INT_2ADDR:
      return DivInt;
    case REM_INT:
    case REM_INT_2ADDR:
      return RemInt;
    case AND_INT:
    case AND_INT_2ADDR:
      return AndInt;
    case OR_INT:
    case OR_INT_2ADDR:
      return OrInt;
    case XOR_INT:
    case XOR_INT_2ADDR:
      return XorInt;
    case SHL_INT:
    case SHL_INT_2ADDR:
      return ShlInt;
    case SHR_INT:
    case SHR_INT_2ADDR:
      return ShrInt;
    case USHR_INT:
    case USHR_INT_2ADDR:
      return UshrInt;
    case ADD_FLOAT:
    case ADD_FLOAT_2ADDR:
      return AddFloat;
    case SUB_FLOAT:
    case SUB_FLOAT_2ADDR:
      return SubFloat;
    case MUL_FLOAT:
    case MUL_FLOAT_2ADDR:
      return MulFloat;
    case DIV_FLOAT:
    case DIV_FLOAT_2ADDR:
      return DivFloat;
    case REM_FLOAT:
    case REM_FLOAT_2ADDR:
      return RemFloat;
    case ADD_LONG:
    case ADD_LONG_2ADDR:
      return AddLong;
    case SUB_LONG:
    case SUB_LONG_2ADDR:
      return SubLong;
    case MUL_LONG:
    case MUL_LONG_2ADDR:
      return MulLong;
    case DIV_LONG:
    case DIV_LONG_2ADDR:
      return DivLong;
    case REM_LONG:
    case REM_LONG_2ADDR:
      return RemLong;
    case AND_LONG:
    case AND_LONG_2ADDR:
      return AndLong;
    case OR_LONG:
    case OR_LONG_2ADDR:
      return OrLong;
    case XOR_LONG:
    case XOR_LONG_2ADDR:
      return XorLong;
    case SHL_LONG:
    case SHL_LONG_2ADDR:
      return ShlLong;
    case SHR_LONG:
    case SHR_LONG_2ADDR:
      return ShrLong;
    case USHR_LONG:
    case USHR_LONG_2ADDR:
      return UshrLong;
    case ADD_DOUBLE:
    case ADD_DOUBLE_2ADDR:
      return AddDouble;
    case SUB_DOUBLE:
    case SUB_DOUBLE_2ADDR:
      return SubDouble;
    case MUL_DOUBLE:
    case MUL_DOUBLE_2ADDR:
      return MulDouble;
    case DIV_DOUBLE:
    case DIV_DOUBLE_2ADDR:
      return DivDouble;
    case REM_DOUBLE:
    case REM_DOUBLE_2ADDR:
      return RemDouble;
    default:
      return null;
    }
  }
  
  public void checkRegisterType(DexRegister reg) {
	  if (this.getWidth() != reg.getWidth())
		  throw new Error("Register width does not match instruction opcode");
	  else if (reg instanceof DexTaintRegister && this != OrInt)
		  throw new Error("Only OR operation is allowed on taint registers");
  }
}