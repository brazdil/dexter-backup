package uk.ac.cam.db538.dexter.dex.code;

import lombok.Getter;

public class DexInstruction_Const extends DexInstruction {

  @Getter private final DexRegister RegTo;
  @Getter private final long Value;

  // CAREFUL: if Value is 32-bit and bottom 16-bits are zero,
  //          turn it into const/high16 instruction

  public DexInstruction_Const(DexRegister to, long value) {
    RegTo = to;
    Value = value;
  }

  @Override
  public String getOriginalAssembly() {
    return "const v" + RegTo.getOriginalId() + ", #" + Value;
  }
}
