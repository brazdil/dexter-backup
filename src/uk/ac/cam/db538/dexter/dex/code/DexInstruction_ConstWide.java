package uk.ac.cam.db538.dexter.dex.code;

import lombok.Getter;

public class DexInstruction_ConstWide extends DexInstruction {

  @Getter private final DexRegister RegTo1;
  @Getter private final DexRegister RegTo2;
  @Getter private final long Value;

  // CAREFUL: if Value is 64-bit and bottom 48-bits are zero,
  //          turn it into const-wide/high16 instruction

  public DexInstruction_ConstWide(DexRegister to1, DexRegister to2, long value) {
    RegTo1 = to1;
    RegTo2 = to2;
    Value = value;
  }

  @Override
  public String getOriginalAssembly() {
    return "const-wide v" + RegTo1.getId() + ", #" + Value;
  }

  @Override
  public DexInstruction[] instrument(TaintRegisterMap mapping) {
    return new DexInstruction[] { this };
  }
}
