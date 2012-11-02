package uk.ac.cam.db538.dexter.dex;

import org.jf.dexlib.DexFile;
import org.jf.dexlib.StringIdItem;
import org.jf.dexlib.TypeIdItem;

import uk.ac.cam.db538.dexter.dex.type.DexArrayType;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.utils.Cache;

public class DexAssemblingCache {

  private final Cache<DexClassType, TypeIdItem> ClassTypes;
  private final Cache<DexArrayType, TypeIdItem> ArrayTypes;
  private final Cache<String, StringIdItem> StringConstants;

  public DexAssemblingCache(final DexFile outFile) {
    ClassTypes = DexClassType.createAssemblingCache(this, outFile);
    ArrayTypes = DexArrayType.createAssemblingCache(this, outFile);
    StringConstants = new Cache<String, StringIdItem>() {
      @Override
      protected StringIdItem createNewEntry(String constant) {
        return StringIdItem.internStringIdItem(outFile, constant);
      }
    };
  }

  public TypeIdItem getClassTypeId(DexClassType key) {
    return ClassTypes.getCachedEntry(key);
  }

  public TypeIdItem getArrayTypeId(DexArrayType key) {
    return ArrayTypes.getCachedEntry(key);
  }

  public StringIdItem getStringConstant(String key) {
    return StringConstants.getCachedEntry(key);
  }

}
