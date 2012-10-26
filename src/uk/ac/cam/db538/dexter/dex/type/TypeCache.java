package uk.ac.cam.db538.dexter.dex.type;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

public class TypeCache {

  @Getter private final Map<String, DexClassType> ClassTypes;
  @Getter private final Map<String, DexArrayType> ArrayTypes;

  public TypeCache() {
    ClassTypes = new HashMap<String, DexClassType>();
    ArrayTypes = new HashMap<String, DexArrayType>();
  }


}
