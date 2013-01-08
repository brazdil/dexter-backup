package uk.ac.cam.db538.dexter.dex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class DexInstrumentationCache {

  private final Map<DexField, DexField> fieldInstrumentation;
  @Getter private final List<InstrumentationWarning> warnings;

  public DexInstrumentationCache() {
    fieldInstrumentation = new HashMap<DexField, DexField>();
    warnings = new ArrayList<InstrumentationWarning>();
  }

  public DexField getTaintField(DexField f) {
    if (!fieldInstrumentation.containsKey(f))
      fieldInstrumentation.put(f, f.instrument());
    return fieldInstrumentation.get(f);
  }

  @AllArgsConstructor
  @Getter
  public static class InstrumentationWarning {
    private final String message;
  }
}
