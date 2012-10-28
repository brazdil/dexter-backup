package uk.ac.cam.db538.dexter.dex.type;

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

  public static DexArrayType parse(String typeDescriptor, TypeCache cache) throws UnknownTypeException {
    if (!typeDescriptor.startsWith("["))
      throw new UnknownTypeException(typeDescriptor);

    if (cache != null) {
      val res = cache.getArrayTypes().get(typeDescriptor);
      if (res != null)
        return res;
    }

    val elementType = DexRegisterType.parse(typeDescriptor.substring(1), cache);
    val newType = new DexArrayType(elementType);
    if (cache != null)
      cache.getArrayTypes().put(typeDescriptor, newType);
    return newType;
  }

}
