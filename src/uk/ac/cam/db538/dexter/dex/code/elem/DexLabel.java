package uk.ac.cam.db538.dexter.dex.code.elem;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.utils.Cache;

public class DexLabel extends DexCodeElement {

  private static long LABEL_COUNTER = -1L;

  @Getter private final long originalAbsoluteOffset;
  @Getter @Setter private boolean evenAligned = false;

  public DexLabel(DexCode methodCode, long originalAbsoluteOffset) {
    super(methodCode);

    this.originalAbsoluteOffset = originalAbsoluteOffset;
  }

  public DexLabel(DexCode methodCode) {
    this(methodCode, LABEL_COUNTER);

    LABEL_COUNTER--;
    if (LABEL_COUNTER >= 0L)
      LABEL_COUNTER = -1L;
  }

  @Override
  public String getOriginalAssembly() {
//    if (originalAbsoluteOffset >= 0)
    return "L" + originalAbsoluteOffset + ":";
//    else
//      return "L???:";
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

  @Override
  protected DexCodeElement gcReplaceWithTemporaries(Map<DexRegister, DexRegister> mapping) {
    return this;
  }
}
