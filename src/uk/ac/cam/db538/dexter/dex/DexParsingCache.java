package uk.ac.cam.db538.dexter.dex;

import uk.ac.cam.db538.dexter.dex.type.DexArrayType;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.utils.Cache;

public class DexParsingCache {

  private final Cache<String, DexClassType> ClassTypes;
  private final Cache<String, DexArrayType> ArrayTypes;

  public DexParsingCache() {
    ClassTypes = DexClassType.createParsingCache();
    ArrayTypes = DexArrayType.createParsingCache(this);
  }

  public DexClassType getClassType(String desc) {
    return ClassTypes.getCachedEntry(desc);
  }

  public DexArrayType getArrayType(String desc) {
    return ArrayTypes.getCachedEntry(desc);
  }
}
