package uk.ac.cam.db538.dexter.dex.code.elem;

import lombok.Getter;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.utils.Cache;
import uk.ac.cam.db538.dexter.utils.Pair;

public class DexCatch extends DexCodeElement {

  private static long CATCH_COUNTER = -1L;

  @Getter private final long originalAbsoluteOffset;
  @Getter private final DexClassType exceptionType;

  public DexCatch(long originalAbsoluteOffset, DexClassType exceptionType) {
    super();

    this.originalAbsoluteOffset = originalAbsoluteOffset;
    this.exceptionType = exceptionType;
  }

  public DexCatch(DexClassType exceptionType) {
    this(CATCH_COUNTER, exceptionType);

    CATCH_COUNTER--;
    if (CATCH_COUNTER >= 0L)
      CATCH_COUNTER = -1L;
  }

  @Override
  public String toString() {
    return "CATCH" + originalAbsoluteOffset + ":";
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
}
