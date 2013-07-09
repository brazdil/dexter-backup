package uk.ac.cam.db538.dexter.dex.type;

import uk.ac.cam.db538.dexter.utils.Pair;

public class DexLong extends DexPrimitiveType {

	private static final long serialVersionUID = 1L;

	private static String DESCRIPTOR = "J";
	private static String NAME = "long";

	DexLong() { }

    @Override
	public DexRegisterTypeSize getTypeSize() {
    	return DexRegisterTypeSize.WIDE;
	}

	@Override
	public String getDescriptor() {
		return DESCRIPTOR;
	}

	@Override
	public String getPrettyName() {
		return NAME;
	}

	@Override
    public Pair<DexClassType, String> getPrimitiveClassConstantField(DexTypeCache cache) {
      return new Pair<DexClassType, String>(DexClassType.parse("Ljava/lang/Long;", cache), "TYPE");
    }

    public static DexLong parse(String typeDescriptor, DexTypeCache cache) {
    	if (!typeDescriptor.equals(DESCRIPTOR))
    		throw new UnknownTypeException(typeDescriptor);
    	else
    		return cache.getCachedType_Long();
    }
    
	public static String jvm2dalvik(String javaName) {
		if (javaName.equals(NAME))
			return DESCRIPTOR;
		else
			throw new UnknownTypeException(javaName);
	}
}