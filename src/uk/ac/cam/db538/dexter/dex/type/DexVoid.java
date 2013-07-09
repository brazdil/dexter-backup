package uk.ac.cam.db538.dexter.dex.type;

public class DexVoid extends DexType {

	private static final long serialVersionUID = 1L;

	DexVoid() { }

	public static DexVoid parse(String typeDescriptor, DexTypeCache typeCache) {
		if (typeDescriptor.equals("V"))
			return typeCache.getCachedType_Void();
		else
			throw new UnknownTypeException(typeDescriptor);
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
