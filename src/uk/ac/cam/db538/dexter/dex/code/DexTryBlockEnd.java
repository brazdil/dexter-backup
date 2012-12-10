package uk.ac.cam.db538.dexter.dex.code;

import lombok.Getter;

public class DexTryBlockEnd extends DexCodeElement {

  @Getter private final DexTryBlockStart blockStart;

  public DexTryBlockEnd(DexCode methodCode, DexTryBlockStart blockStart) {
    super(methodCode);

    this.blockStart = blockStart;
  }

  @Override
  public String getOriginalAssembly() {
    return "} // end TRY" + blockStart.getOriginalAbsoluteOffsetString();
  }

  @Override
  public boolean cfgEndsBasicBlock() {
    return true;
  }
}
