package uk.ac.cam.db538.dexter.dex.type;

import uk.ac.cam.db538.dexter.utils.Pair;

public class DexChar extends DexPrimitiveType {
	
	private static final long serialVersionUID = 1L;

	private static String DESCRIPTOR = "C";
	private static String NAME = "char";
	
	DexChar() { }

    @Override
	public DexRegisterTypeSize getTypeSize() {
    	return DexRegisterTypeSize.SINGLE;
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
      return new Pair<DexClassType, String>(DexClassType.parse("Ljava/lang/Character;", cache), "TYPE");
    }

    public static DexChar parse(String typeDescriptor, DexTypeCache cache) {
    	if (!typeDescriptor.equals(DESCRIPTOR))
    		throw new UnknownTypeException(typeDescriptor);
    	else
    		return cache.getCachedType_Char();
    }

	public static String jvm2dalvik(String javaName) {
		if (javaName.equals(NAME))
			return DESCRIPTOR;
		else
			throw new UnknownTypeException(javaName);
	}
}