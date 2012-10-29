package uk.ac.cam.db538.dexter.dex;

import java.util.HashMap;
import java.util.Map;

import uk.ac.cam.db538.dexter.dex.code.DexStringConstant;
import uk.ac.cam.db538.dexter.dex.type.DexArrayType;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;

import lombok.Getter;

public class DexParsingCache {

  @Getter private final Map<String, DexClassType> ClassTypes;
  @Getter private final Map<String, DexArrayType> ArrayTypes;
  @Getter private final Map<String, DexStringConstant> StringConstants;

  public DexParsingCache() {
    ClassTypes = new HashMap<String, DexClassType>();
    ArrayTypes = new HashMap<String, DexArrayType>();
    StringConstants = new HashMap<String, DexStringConstant>();
  }


}
