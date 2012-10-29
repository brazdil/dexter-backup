package uk.ac.cam.db538.dexter.dex.code;

import lombok.Getter;

public class DexLabel extends DexCodeElement {

  @Getter private final long OriginalOffset;

  public DexLabel(long originalOffset) {
    OriginalOffset = originalOffset;
  }

  @Override
  public String getOriginalAssembly() {
    return "L" + OriginalOffset + ":";
  }
}
