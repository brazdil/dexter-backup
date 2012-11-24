package uk.ac.cam.db538.dexter.dex.code.insn;

import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;
import uk.ac.cam.db538.dexter.dex.type.DexRegisterType.DexRegisterTypeSize;

public class Opcode_GetPutWide {

  public static void checkTypeIsWide(DexRegisterType type) {
    if (type.getTypeSize() != DexRegisterTypeSize.WIDE)
      throw new InstructionArgumentException("Source/target type doesn't match the instruction's opcode");
  }
}