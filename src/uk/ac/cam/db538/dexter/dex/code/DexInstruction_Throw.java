package uk.ac.cam.db538.dexter.dex.code;

import lombok.Getter;

public class DexInstruction_Throw extends DexInstruction {

  @Getter private final DexRegister RegFrom;

  public DexInstruction_Throw(DexRegister from) {
    RegFrom = from;
  }

  @Override
  public String getOriginalAssembly() {
    return "throw v" + RegFrom.getOriginalId();
  }
}
