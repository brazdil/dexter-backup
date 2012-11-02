package uk.ac.cam.db538.dexter.dex;

import java.util.HashMap;
import java.util.Map;

import org.jf.dexlib.StringIdItem;
import org.jf.dexlib.TypeIdItem;

import uk.ac.cam.db538.dexter.dex.code.DexStringConstant;
import uk.ac.cam.db538.dexter.dex.type.DexArrayType;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;

import lombok.Getter;

public class DexAssemblingCache {

  @Getter private final Map<DexClassType, TypeIdItem> ClassTypes;
  @Getter private final Map<DexArrayType, TypeIdItem> ArrayTypes;
  @Getter private final Map<DexStringConstant, StringIdItem> StringConstants;

  public DexAssemblingCache() {
    ClassTypes = new HashMap<DexClassType, TypeIdItem>();
    ArrayTypes = new HashMap<DexArrayType, TypeIdItem>();
    StringConstants = new HashMap<DexStringConstant, StringIdItem>();
  }


}
