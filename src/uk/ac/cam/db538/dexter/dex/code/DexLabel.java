package uk.ac.cam.db538.dexter.dex.code;

import uk.ac.cam.db538.dexter.utils.Cache;
import lombok.Getter;

public class DexLabel extends DexCodeElement {

  @Getter private final long originalAbsoluteOffset;

  public DexLabel(DexCode methodCode, long originalAbsoluteOffset) {
    super(methodCode);

    this.originalAbsoluteOffset = originalAbsoluteOffset;
  }

  @Override
  public String getOriginalAssembly() {
    return "L" + originalAbsoluteOffset + ":";
  }

  @Override
  public boolean cfgStartsBasicBlock() {
    return true;
  }

  public static Cache<Long, DexLabel> createCache(final DexCode code) {
    return new Cache<Long, DexLabel>() {
      @Override
      protected DexLabel createNewEntry(Long absoluteOffset) {
        return new DexLabel(code, absoluteOffset);
      }
    };
  }
}
