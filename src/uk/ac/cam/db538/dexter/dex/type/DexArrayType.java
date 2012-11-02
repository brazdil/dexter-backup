package uk.ac.cam.db538.dexter.dex.type;

import org.jf.dexlib.DexFile;
import org.jf.dexlib.TypeIdItem;

import uk.ac.cam.db538.dexter.dex.DexAssemblingCache;
import uk.ac.cam.db538.dexter.dex.DexParsingCache;
import uk.ac.cam.db538.dexter.utils.Cache;
import lombok.Getter;
import lombok.val;

public class DexArrayType extends DexReferenceType {

  @Getter private final DexRegisterType ElementType;

  public DexArrayType(DexRegisterType elementType) {
    super("[" + elementType.getDescriptor(),
          elementType.getPrettyName() + "[]",
          1);
    ElementType = elementType;
  }

  public static DexArrayType parse(String typeDescriptor, DexParsingCache cache) {
    return cache.getArrayType(typeDescriptor);
  }

  public static Cache<String, DexArrayType> createParsingCache(final DexParsingCache cache) {
    return new Cache<String, DexArrayType>() {
      @Override
      protected DexArrayType createNewEntry(String typeDescriptor) {
        if (!typeDescriptor.startsWith("["))
          throw new UnknownTypeException(typeDescriptor);

        val elementType = DexRegisterType.parse(typeDescriptor.substring(1), cache);
        return new DexArrayType(elementType);
      }
    };
  }

  public static Cache<DexArrayType, TypeIdItem> createAssemblingCache(final DexAssemblingCache cache, final DexFile outFile) {
    return new Cache<DexArrayType, TypeIdItem>() {
      @Override
      protected TypeIdItem createNewEntry(DexArrayType key) {
        return TypeIdItem.internTypeIdItem(outFile,
                                           cache.getStringConstant(key.getDescriptor()));
      }
    };
  }
}
