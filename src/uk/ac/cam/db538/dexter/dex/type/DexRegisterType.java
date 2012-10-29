package uk.ac.cam.db538.dexter.dex.type;

import uk.ac.cam.db538.dexter.dex.DexParsingCache;
import lombok.Getter;

public abstract class DexRegisterType extends DexType {

  @Getter private final int Registers;

  public DexRegisterType(String descriptor, String prettyName, int registers) {
    super(descriptor, prettyName);
    Registers = registers;
  }

  public static DexRegisterType parse(String typeDescriptor, DexParsingCache cache) throws UnknownTypeException {
    try {
      return DexPrimitiveType.parse(typeDescriptor);
    } catch (UnknownTypeException e) {
    }

    try {
      return DexReferenceType.parse(typeDescriptor, cache);
    } catch (UnknownTypeException e) {
    }

    throw new UnknownTypeException(typeDescriptor);
  }
}
