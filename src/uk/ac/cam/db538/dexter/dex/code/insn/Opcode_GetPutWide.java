package uk.ac.cam.db538.dexter.dex.code.insn;

import uk.ac.cam.db538.dexter.dex.type.DexType_Register;
import uk.ac.cam.db538.dexter.dex.type.DexType_Register.DexRegisterTypeSize;

public class Opcode_GetPutWide {

  public static void checkTypeIsWide(DexType_Register type) {
    if (type.getTypeSize() != DexRegisterTypeSize.WIDE)
      throw new InstructionArgumentException("Source/target type doesn't match the instruction's opcode");
  }
}