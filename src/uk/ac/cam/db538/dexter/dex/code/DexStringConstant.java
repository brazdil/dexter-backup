package uk.ac.cam.db538.dexter.dex.code;

import lombok.Getter;

import org.jf.dexlib.StringIdItem;

import uk.ac.cam.db538.dexter.dex.DexParsingCache;
import uk.ac.cam.db538.dexter.utils.Cache;

public class DexStringConstant {
  @Getter private final String Value;

  private DexStringConstant(String value) {
    Value = value;
  }

  public static DexStringConstant parse(StringIdItem item, DexParsingCache cache) {
    return parse(item.getStringValue(), cache);
  }

  public static DexStringConstant parse(String value, DexParsingCache cache) {
    return cache.getStringConstant(value);
  }

  public static Cache<String, DexStringConstant> createCache() {
    return new Cache<String, DexStringConstant>() {
      @Override
      protected DexStringConstant createNewEntry(String value) {
        return new DexStringConstant(value);
      }
    };
  }
}
