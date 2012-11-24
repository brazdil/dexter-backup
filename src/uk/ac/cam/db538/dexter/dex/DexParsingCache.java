package uk.ac.cam.db538.dexter.dex;

import uk.ac.cam.db538.dexter.dex.type.DexArrayType;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.utils.Cache;

public class DexParsingCache {

  private final Cache<String, DexClassType> classTypes;
  private final Cache<String, DexArrayType> arrayTypes;

  public DexParsingCache() {
    classTypes = DexClassType.createParsingCache();
    arrayTypes = DexArrayType.createParsingCache(this);
  }

  public DexClassType getClassType(String desc) {
    return classTypes.getCachedEntry(desc);
  }

  public DexArrayType getArrayType(String desc) {
    return arrayTypes.getCachedEntry(desc);
  }

  public boolean classTypeExists(String desc) {
    return classTypes.contains(desc);
  }
}
