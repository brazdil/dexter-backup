package uk.ac.cam.db538.dexter.dex.code;

import uk.ac.cam.db538.dexter.utils.Cache;
import lombok.Getter;

public class DexLabel extends DexCodeElement {

  @Getter private final long OriginalAbsoluteOffset;

  public DexLabel(long originalAbsoluteOffset) {
    OriginalAbsoluteOffset = originalAbsoluteOffset;
  }

  @Override
  public String getOriginalAssembly() {
    return "L" + OriginalAbsoluteOffset + ":";
  }

  public static Cache<Long, DexLabel> createCache() {
    return new Cache<Long, DexLabel>() {
      @Override
      protected DexLabel createNewEntry(Long absoluteOffset) {
        return new DexLabel(absoluteOffset);
      }
    };
  }
}
