package uk.ac.cam.db538.dexter.dex.type;

public class DexVoid extends DexType {

  public DexVoid() {
    super("V", "void");
  }

  private static final DexVoid Instance = new DexVoid();

  public static DexVoid parse(String typeDescriptor) {
    if (typeDescriptor.equals(Instance.getDescriptor()))
      return Instance;
    else
      return null;
  }
}
