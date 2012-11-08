package uk.ac.cam.db538.dexter.dex.code;

import lombok.Getter;

public class DexLabel extends DexCodeElement {

  @Getter private final long OriginalAbsoluteOffset;

  public DexLabel(DexCode methodCode, long originalAbsoluteOffset) {
    super(methodCode);

    OriginalAbsoluteOffset = originalAbsoluteOffset;
  }

  @Override
  public String getOriginalAssembly() {
    return "L" + OriginalAbsoluteOffset + ":";
  }

  @Override
  public boolean cfgStartsBasicBlock() {
    return true;
  }
}
