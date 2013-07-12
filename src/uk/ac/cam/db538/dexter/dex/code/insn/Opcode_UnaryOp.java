package uk.ac.cam.db538.dexter.dex.code.insn;

import lombok.Getter;

import org.jf.dexlib.Code.Opcode;

import uk.ac.cam.db538.dexter.dex.code.reg.DexStandardRegister;
import uk.ac.cam.db538.dexter.dex.code.reg.RegisterWidth;

public enum Opcode_UnaryOp {
  NegInt("neg-int", RegisterWidth.SINGLE),
  NotInt("not-int", RegisterWidth.SINGLE),
  NegFloat("neg-float", RegisterWidth.SINGLE),
  NegLong("neg-long", RegisterWidth.WIDE),
  NotLong("not-long", RegisterWidth.WIDE),
  NegDouble("neg-double", RegisterWidth.WIDE);

  @Getter private final String AssemblyName;
  @Getter private final RegisterWidth width;

  private Opcode_UnaryOp(String assemblyName, RegisterWidth width) {
    AssemblyName = assemblyName;
    this.width = width;
  }

  public static Opcode_UnaryOp convert(Opcode opcode) {
    switch (opcode) {
    case NEG_INT:
      return NegInt;
    case NOT_INT:
      return NotInt;
    case NEG_FLOAT:
      return NegFloat;
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
  
  public void checkRegisterType(DexStandardRegister reg) {
	  if (this.getWidth() != reg.getWidth())
		  throw new Error("Width of the register does not fit used opcode");
  }
}