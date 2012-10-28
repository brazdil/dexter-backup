package uk.ac.cam.db538.dexter.dex.code;

import lombok.Getter;

public class DexInstruction_ReturnWide extends DexInstruction {

  @Getter private final DexRegister RegFrom1;
  @Getter private final DexRegister RegFrom2;

  public DexInstruction_ReturnWide(DexRegister from1, DexRegister from2) {
    RegFrom1 = from1;
    RegFrom2 = from2;
  }

  @Override
  public String getOriginalAssembly() {
    return "return-wide v" + RegFrom1.getOriginalId();
  }
}
