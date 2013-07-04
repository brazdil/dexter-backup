package uk.ac.cam.db538.dexter.dex.type;

public class DexType_Void extends DexType {

  private DexType_Void() { }

  private static final DexType_Void VOID_INSTANCE = new DexType_Void();

  public static DexType_Void parse(String typeDescriptor) {
    if (typeDescriptor.equals(VOID_INSTANCE.getDescriptor()))
      return VOID_INSTANCE;
    else
      return null;
  }

	@Override
	public String getDescriptor() {
		return "V";
	}
	
	@Override
	public String getPrettyName() {
		return "void";
	}
}
