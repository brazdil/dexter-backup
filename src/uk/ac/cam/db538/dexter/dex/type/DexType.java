package uk.ac.cam.db538.dexter.dex.type;

import java.util.ArrayList;
import java.util.List;

import org.jf.dexlib.DexFile;
import org.jf.dexlib.TypeIdItem;
import org.jf.dexlib.TypeListItem;

import uk.ac.cam.db538.dexter.dex.DexAssemblingCache;
import uk.ac.cam.db538.dexter.dex.DexParsingCache;
import uk.ac.cam.db538.dexter.utils.Cache;
import lombok.Getter;
import lombok.val;

public abstract class DexType {

  @Getter private final String Descriptor;
  @Getter private final String PrettyName;

  public DexType(String descriptor, String prettyName) {
    Descriptor = descriptor;
    PrettyName = prettyName;
  }

  public static DexType parse(String typeDescriptor, DexParsingCache cache) throws UnknownTypeException {
    val res = DexVoid.parse(typeDescriptor);
    if (res != null)
      return res;
    else
      return DexRegisterType.parse(typeDescriptor, cache);
  }

  public static Cache<DexType, TypeIdItem> createAssemblingCache(final DexAssemblingCache cache, final DexFile outFile) {
    return new Cache<DexType, TypeIdItem>() {
      @Override
      protected TypeIdItem createNewEntry(DexType type) {
        return TypeIdItem.internTypeIdItem(outFile,
                                           cache.getStringConstant(type.getDescriptor()));
      }
    };
  }

  public static Cache<List<DexType>, TypeListItem> createAssemblingCacheForLists(final DexAssemblingCache cache, final DexFile outFile) {
    return new Cache<List<DexType>, TypeListItem>() {
      @Override
      protected TypeListItem createNewEntry(List<DexType> typeList) {
        val dexTypeList = new ArrayList<TypeIdItem>(typeList.size());
        for (val type : typeList)
          dexTypeList.add(cache.getType(type));

        return TypeListItem.internTypeListItem(outFile, dexTypeList);
      }
    };
  }
}
