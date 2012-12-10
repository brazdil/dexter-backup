package uk.ac.cam.db538.dexter.dex.code;

import lombok.Getter;

public class DexTryBlockStart extends DexCodeElement {

  @Getter private final long originalAbsoluteOffset;

  public DexTryBlockStart(DexCode methodCode, long originalAbsoluteOffset) {
    super(methodCode);

    this.originalAbsoluteOffset = originalAbsoluteOffset;
  }

  public DexTryBlockStart(DexCode methodCode) {
    super(methodCode);

    this.originalAbsoluteOffset = -1;
  }

  @Override
  public String getOriginalAssembly() {
    return "TRY" + getOriginalAbsoluteOffsetString() + " {";
  }

  public String getOriginalAbsoluteOffsetString() {
    if (originalAbsoluteOffset >= 0)
      return Long.toString(originalAbsoluteOffset);
    else
      return "???";
  }

  @Override
  public boolean cfgStartsBasicBlock() {
    return true;
  }
//
//  public static Cache<Long, DexTryBlockStart> createCache(final DexCode code) {
//    return new Cache<Long, DexTryBlockStart>() {
//      @Override
//      protected DexTryBlockStart createNewEntry(Long absoluteOffset) {
//        return new DexTryBlockStart(code, absoluteOffset);
//      }
//    };
//  }
}
