package uk.ac.cam.db538.dexter.dex.code;

import lombok.Getter;

public class DexInstruction_ArrayLength extends DexInstruction {

  @Getter private final DexRegister RegTo;
  @Getter private final DexRegister RegFrom;

  // CAREFUL: registers can only be allocated to 0-15 !!!

  public DexInstruction_ArrayLength(DexRegister to, DexRegister from) {
    RegTo = to;
    RegFrom = from;
  }

  @Override
  public String getOriginalAssembly() {
    return "array-length v" + RegTo.getOriginalId() + ", v" + RegFrom.getOriginalId();
  }
}
