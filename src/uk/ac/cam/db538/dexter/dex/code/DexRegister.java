package uk.ac.cam.db538.dexter.dex.code;

import lombok.Getter;

public class DexRegister {

  @Getter private final int OriginalId;

  public DexRegister(int originalId) {
    OriginalId = originalId;
  }
}
