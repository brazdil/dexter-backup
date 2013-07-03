package uk.ac.cam.db538.dexter.dex.code.elem;

import lombok.Getter;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.type.DexType_Class;
import uk.ac.cam.db538.dexter.utils.Cache;
import uk.ac.cam.db538.dexter.utils.Pair;

public class DexCatch extends DexCodeElement {

  private static long CATCH_COUNTER = -1L;

  @Getter private final long originalAbsoluteOffset;
  @Getter private final DexType_Class exceptionType;

  public DexCatch(DexCode methodCode, long originalAbsoluteOffset, DexType_Class exceptionType) {
    super(methodCode);

    this.originalAbsoluteOffset = originalAbsoluteOffset;
    this.exceptionType = exceptionType;
  }

  public DexCatch(DexCode methodCode, DexType_Class exceptionType) {
    this(methodCode, CATCH_COUNTER, exceptionType);

    CATCH_COUNTER--;
    if (CATCH_COUNTER >= 0L)
      CATCH_COUNTER = -1L;
  }

  @Override
  public String getOriginalAssembly() {
    return "CATCH" + originalAbsoluteOffset + ":";
  }

  @Override
  public boolean cfgStartsBasicBlock() {
    return true;
  }

  public static Cache<Pair<Long, DexType_Class>, DexCatch> createCache(final DexCode code) {
    return new Cache<Pair<Long, DexType_Class>, DexCatch>() {
      @Override
      protected DexCatch createNewEntry(Pair<Long, DexType_Class> args) {
        return new DexCatch(code, args.getValA(), args.getValB());
      }
    };
  }
}
