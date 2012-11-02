package uk.ac.cam.db538.dexter.dex;

import uk.ac.cam.db538.dexter.dex.code.DexStringConstant;
import uk.ac.cam.db538.dexter.dex.type.DexArrayType;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.utils.Cache;

public class DexParsingCache {

  private final Cache<String, DexClassType> ClassTypes;
  private final Cache<String, DexArrayType> ArrayTypes;
  private final Cache<String, DexStringConstant> StringConstants;

  public DexParsingCache() {
    ClassTypes = DexClassType.createCache();
    ArrayTypes = DexArrayType.createCache(this);
    StringConstants = DexStringConstant.createCache();
  }

  public DexClassType getClassType(String desc) {
    return ClassTypes.getCachedEntry(desc);
  }

  public DexArrayType getArrayType(String desc) {
    return ArrayTypes.getCachedEntry(desc);
  }

  public DexStringConstant getStringConstant(String value) {
    return StringConstants.getCachedEntry(value);
  }
}
