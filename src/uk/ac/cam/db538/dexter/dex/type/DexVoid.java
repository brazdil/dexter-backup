package uk.ac.cam.db538.dexter.dex.type;

public class DexVoid extends DexType {

	private static final long serialVersionUID = 1L;
	
	private static String DESCRIPTOR = "V";
	private static String NAME = "void";

	DexVoid() { }

	public static DexVoid parse(String typeDescriptor, DexTypeCache typeCache) {
		if (typeDescriptor.equals(DESCRIPTOR))
			return typeCache.getCachedType_Void();
		else
			throw new UnknownTypeException(typeDescriptor);
	}

	public static String jvm2dalvik(String jvmName) {
		if (jvmName.equals(NAME))
			return DESCRIPTOR;
		else
			throw new UnknownTypeException(jvmName);
	}
	
	@Override
	public String getDescriptor() {
		return DESCRIPTOR;
	}
	
	@Override
	public String getPrettyName() {
		return NAME;
	}
}
