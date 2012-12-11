package uk.ac.cam.db538.dexter.dex.code.elem;

import java.util.Map;

import lombok.Getter;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.utils.Cache;

public class DexCatchAll extends DexCodeElement {

  @Getter private final long originalAbsoluteOffset;

  public DexCatchAll(DexCode methodCode, long originalAbsoluteOffset) {
    super(methodCode);

    this.originalAbsoluteOffset = originalAbsoluteOffset;
  }

  public DexCatchAll(DexCode methodCode) {
    this(methodCode, -1);
  }

  @Override
  public String getOriginalAssembly() {
    if (originalAbsoluteOffset >= 0)
      return "CATCHALL" + originalAbsoluteOffset + ":";
    else
      return "CATCHALL???:";
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
  protected DexCodeElement gcReplaceWithTemporaries(Map<DexRegister, DexRegister> mapping) {
    return this;
  }
}
