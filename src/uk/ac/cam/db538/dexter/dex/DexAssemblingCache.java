package uk.ac.cam.db538.dexter.dex;

import java.util.List;

import org.jf.dexlib.DexFile;
import org.jf.dexlib.StringIdItem;
import org.jf.dexlib.TypeIdItem;
import org.jf.dexlib.TypeListItem;

import uk.ac.cam.db538.dexter.dex.type.DexType;
import uk.ac.cam.db538.dexter.utils.Cache;

public class DexAssemblingCache {

  private final Cache<DexType, TypeIdItem> Types;
  private final Cache<List<DexType>, TypeListItem> TypeLists;
  private final Cache<String, StringIdItem> StringConstants;

  public DexAssemblingCache(final DexFile outFile) {
    Types = DexType.createAssemblingCache(this, outFile);
    TypeLists = DexType.createAssemblingCacheForLists(this, outFile);
    StringConstants = new Cache<String, StringIdItem>() {
      @Override
      protected StringIdItem createNewEntry(String constant) {
        return StringIdItem.internStringIdItem(outFile, constant);
      }
    };
  }

  public TypeIdItem getTypeId(DexType key) {
    return Types.getCachedEntry(key);
  }

  public TypeListItem getTypeList(List<DexType> key) {
    return TypeLists.getCachedEntry(key);
  }

  public StringIdItem getStringConstant(String key) {
    return StringConstants.getCachedEntry(key);
  }

}
