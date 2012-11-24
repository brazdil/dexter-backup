package uk.ac.cam.db538.dexter.dex.type;

public class DexVoid extends DexType {

  private DexVoid() {
    super("V", "void");
  }

  private static final DexVoid VOID_INSTANCE = new DexVoid();

  public static DexVoid parse(String typeDescriptor) {
    if (typeDescriptor.equals(VOID_INSTANCE.getDescriptor()))
      return VOID_INSTANCE;
    else
      return null;
  }
}
