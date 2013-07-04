package uk.ac.cam.db538.dexter.dex.type;

import uk.ac.cam.db538.dexter.utils.Pair;

public class DexType_Long extends DexType_Primitive {
    DexType_Long() { }

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
    public Pair<DexType_Class, String> getPrimitiveClassConstantField(DexTypeCache cache) {
      return new Pair<DexType_Class, String>(DexType_Class.parse("Ljava/lang/Long;", cache), "TYPE");
    }

    public static DexType_Long parse(String typeDescriptor, DexTypeCache cache) {
    	if (!typeDescriptor.equals("J"))
    		throw new UnknownTypeException(typeDescriptor);
    	else
    		return cache.getCachedType_Long();
    }
}