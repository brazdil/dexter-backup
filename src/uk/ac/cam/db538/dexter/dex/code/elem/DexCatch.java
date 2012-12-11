package uk.ac.cam.db538.dexter.dex.code.elem;

import java.util.Map;

import lombok.Getter;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.utils.Cache;
import uk.ac.cam.db538.dexter.utils.Pair;

public class DexCatch extends DexCodeElement {

  @Getter private final long originalAbsoluteOffset;
  @Getter private final DexClassType exceptionType;

  public DexCatch(DexCode methodCode, long originalAbsoluteOffset, DexClassType exceptionType) {
    super(methodCode);

    this.originalAbsoluteOffset = originalAbsoluteOffset;
    this.exceptionType = exceptionType;
  }

  public DexCatch(DexCode methodCode, DexClassType exceptionType) {
    this(methodCode, -1, exceptionType);
  }

  @Override
  public String getOriginalAssembly() {
    if (originalAbsoluteOffset >= 0)
      return "CATCH" + originalAbsoluteOffset + ":";
    else
      return "CATCH???:";
  }

  @Override
  public boolean cfgStartsBasicBlock() {
    return true;
  }

  public static Cache<Pair<Long, DexClassType>, DexCatch> createCache(final DexCode code) {
    return new Cache<Pair<Long, DexClassType>, DexCatch>() {
      @Override
      protected DexCatch createNewEntry(Pair<Long, DexClassType> args) {
        return new DexCatch(code, args.getValA(), args.getValB());
      }
    };
  }

  @Override
  protected DexCodeElement gcReplaceWithTemporaries(Map<DexRegister, DexRegister> mapping) {
    return this;
  }
}
