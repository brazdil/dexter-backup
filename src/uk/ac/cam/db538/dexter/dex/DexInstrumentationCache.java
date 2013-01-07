package uk.ac.cam.db538.dexter.dex;

import java.util.HashMap;
import java.util.Map;

public class DexInstrumentationCache {

  private final Map<DexField, DexField> fieldInstrumentation;

  public DexInstrumentationCache() {
    fieldInstrumentation = new HashMap<DexField, DexField>();
  }

  public DexField getTaintField(DexField f) {
    if (!fieldInstrumentation.containsKey(f))
      fieldInstrumentation.put(f, f.instrument());
    return fieldInstrumentation.get(f);
  }
}
