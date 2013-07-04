package uk.ac.cam.db538.dexter.dex.type;

import uk.ac.cam.db538.dexter.utils.Pair;

public class DexType_Integer extends DexType_Primitive {
    DexType_Integer() { }

    @Override
	public DexRegisterTypeSize getTypeSize() {
    	return DexRegisterTypeSize.SINGLE;
	}

	@Override
	public String getDescriptor() {
		return "I";
	}

	@Override
	public String getPrettyName() {
		return "int";
	}

	@Override
    public Pair<DexType_Class, String> getPrimitiveClassConstantField(DexTypeCache cache) {
      return new Pair<DexType_Class, String>(DexType_Class.parse("Ljava/lang/Integer;", cache), "TYPE");
    }

    public static DexType_Integer parse(String typeDescriptor, DexTypeCache cache) {
    	if (!typeDescriptor.equals("I"))
    		throw new UnknownTypeException(typeDescriptor);
    	else
    		return cache.getCachedType_Integer();
    }
}