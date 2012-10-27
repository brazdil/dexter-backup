package uk.ac.cam.db538.dexter.dex.type;

import lombok.Getter;
import lombok.val;

public abstract class DexType {

  @Getter private final String Descriptor;
  @Getter private final String PrettyName;

  public DexType(String descriptor, String prettyName) {
    Descriptor = descriptor;
    PrettyName = prettyName;
  }

  public static DexType parse(String typeDescriptor, TypeCache cache) throws UnknownTypeException {
    val res = DexVoid.parse(typeDescriptor);
    if (res != null)
      return res;
    else
      return DexRegisterType.parse(typeDescriptor, cache);
  }
}
