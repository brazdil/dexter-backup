package uk.ac.cam.db538.dexter.dex.code;

import lombok.Getter;

public class DexReturnWideInstruction extends DexInstruction {

  @Getter private final DexRegister RegFrom1;
  @Getter private final DexRegister RegFrom2;

  public DexReturnWideInstruction(DexRegister from1, DexRegister from2) {
    RegFrom1 = from1;
    RegFrom2 = from2;
  }

  @Override
  public String getOriginalInstruction() {
    return "return-wise r" + RegFrom1.getOriginalId();
  }
}
