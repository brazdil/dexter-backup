package uk.ac.cam.db538.dexter.dex.type;


public abstract class DexType_Register extends DexType {

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
  
  protected DexType_Register() { }

  public static DexType_Register parse(String typeDescriptor, DexTypeCache cache) throws UnknownTypeException {
    try {
      return DexType_Primitive.parse(typeDescriptor, cache);
    } catch (UnknownTypeException e) {
    }

    try {
      return DexType_Reference.parse(typeDescriptor, cache);
    } catch (UnknownTypeException e) {
    }

    throw new UnknownTypeException(typeDescriptor);
  }
  
  public abstract DexRegisterTypeSize getTypeSize();

  public boolean isWide() {
    return getTypeSize() == DexRegisterTypeSize.WIDE;
  }

  public int getRegisters() {
    return getTypeSize().getRegisterCount();
  }
}
