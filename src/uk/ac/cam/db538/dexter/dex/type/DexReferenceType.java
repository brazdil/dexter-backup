package uk.ac.cam.db538.dexter.dex.type;

public abstract class DexReferenceType extends DexRegisterType {

  public DexReferenceType(String descriptor, String prettyName, int registers) {
    super(descriptor, prettyName, registers);
  }

  public static DexReferenceType parse(String typeDescriptor, TypeCache cache) throws UnknownTypeException {
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
