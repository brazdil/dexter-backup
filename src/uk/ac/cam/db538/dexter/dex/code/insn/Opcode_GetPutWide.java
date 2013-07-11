package uk.ac.cam.db538.dexter.dex.code.insn;

import uk.ac.cam.db538.dexter.dex.code.reg.RegisterWidth;
import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;

public class Opcode_GetPutWide {

  public static void checkTypeIsWide(DexRegisterType type) {
    if (type.getTypeSize() != RegisterWidth.WIDE)
      throw new InstructionArgumentException("Source/target type doesn't match the instruction's opcode");
  }
}