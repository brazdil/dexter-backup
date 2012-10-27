package uk.ac.cam.db538.dexter.dex.type;

import lombok.Getter;

public abstract class DexRegisterType extends DexType {

  @Getter private final int Registers;

  public DexRegisterType(String descriptor, String prettyName, int registers) {
    super(descriptor, prettyName);
    Registers = registers;
  }

  public static DexRegisterType parse(String typeDescriptor, TypeCache cache) throws UnknownTypeException {
    try {
      return DexPrimitive.parse(typeDescriptor);
    } catch (UnknownTypeException e) {
    }

    try {
      return DexClassType.parse(typeDescriptor, cache);
    } catch (UnknownTypeException e) {
    }

    try {
      return DexArrayType.parse(typeDescriptor, cache);
    } catch (UnknownTypeException e) {
    }

    throw new UnknownTypeException(typeDescriptor);
  }
}
