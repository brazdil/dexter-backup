package uk.ac.cam.db538.dexter.dex;

import java.util.HashMap;
import java.util.Map;

import lombok.val;

import uk.ac.cam.db538.dexter.dex.type.DexArrayType;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.utils.Cache;

public class DexParsingCache {

  private final Cache<String, DexClassType> classTypes;
  private final Cache<String, DexArrayType> arrayTypes;

  private final Map<String, String> descriptorReplacements;

  public DexParsingCache() {
    classTypes = DexClassType.createParsingCache();
    arrayTypes = DexArrayType.createParsingCache(this);
    descriptorReplacements = new HashMap<String, String>();
  }

  public DexClassType getClassType(String desc) {
    return classTypes.getCachedEntry(getDesc(desc));
  }

  public DexArrayType getArrayType(String desc) {
    return arrayTypes.getCachedEntry(getDesc(desc));
  }

  public boolean classTypeExists(String desc) {
    return classTypes.contains(getDesc(desc));
  }

  public void setDescriptorReplacement(String descOld, String descNew) {
    descriptorReplacements.put(descOld, descNew);
  }

  public void removeDescriptorReplacement(String descOld) {
    descriptorReplacements.remove(descOld);
  }

  private String getDesc(String descOld) {
    val descNew = descriptorReplacements.get(descOld);
    if (descNew == null)
      return descOld;
    else
      return descNew;
  }
}
