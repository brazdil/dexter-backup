package uk.ac.cam.db538.dexter.dex.type;

import uk.ac.cam.db538.dexter.utils.Pair;

public class DexLong extends DexPrimitiveType {
    DexLong() { }

    @Override
	public DexRegisterTypeSize getTypeSize() {
    	return DexRegisterTypeSize.WIDE;
	}

	@Override
	public String getDescriptor() {
		return "J";
	}

	@Override
	public String getPrettyName() {
		return "long";
	}

	@Override
    public Pair<DexClassType, String> getPrimitiveClassConstantField(DexTypeCache cache) {
      return new Pair<DexClassType, String>(DexClassType.parse("Ljava/lang/Long;", cache), "TYPE");
    }

    public static DexLong parse(String typeDescriptor, DexTypeCache cache) {
    	if (!typeDescriptor.equals("J"))
    		throw new UnknownTypeException(typeDescriptor);
    	else
    		return cache.getCachedType_Long();
    }
}