package uk.ac.cam.db538.dexter.dex.type;

import lombok.Getter;
import uk.ac.cam.db538.dexter.dex.DexParsingCache;

public abstract class DexRegisterType extends DexType {

  public static enum DexRegisterTypeSize {
    SINGLE,
    WIDE;

    public int getRegisterCount() {
      switch (this) {
      case SINGLE:
        return 1;
      case WIDE:
        return 2;
      }
      throw new RuntimeException("Unknown register size");
    }
  }

  @Getter private final DexRegisterTypeSize typeSize;

  protected DexRegisterType(String descriptor, String prettyName, DexRegisterTypeSize typeSize) {
    super(descriptor, prettyName);
    this.typeSize = typeSize;
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

  public boolean isWide() {
    return typeSize == DexRegisterTypeSize.WIDE;
  }

  public int getRegisters() {
    return typeSize.getRegisterCount();
  }
}
