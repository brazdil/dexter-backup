package uk.ac.cam.db538.dexter.dex.code.elem;

import java.util.Map;

import lombok.Getter;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.utils.Cache;

public class DexCatchAll extends DexCodeElement {

  private static long CATCHALL_COUNTER = -1L;

  @Getter private final long originalAbsoluteOffset;

  public DexCatchAll(DexCode methodCode, long originalAbsoluteOffset) {
    super(methodCode);

    this.originalAbsoluteOffset = originalAbsoluteOffset;
  }

  public DexCatchAll(DexCode methodCode) {
    this(methodCode, CATCHALL_COUNTER);

    CATCHALL_COUNTER--;
    if (CATCHALL_COUNTER >= 0L)
      CATCHALL_COUNTER = -1L;
  }

  @Override
  public String getOriginalAssembly() {
    return "CATCHALL" + originalAbsoluteOffset + ":";
  }

  @Override
  public boolean cfgStartsBasicBlock() {
    return true;
  }

  public static Cache<Long, DexCatchAll> createCache(final DexCode code) {
    return new Cache<Long, DexCatchAll>() {
      @Override
      protected DexCatchAll createNewEntry(Long offset) {
        return new DexCatchAll(code, offset);
      }
    };
  }

  @Override
  protected DexCodeElement gcReplaceWithTemporaries(Map<DexRegister, DexRegister> mapping, boolean toRefs, boolean toDefs) {
    return this;
  }
}
