package uk.ac.cam.db538.dexter.dex.type;

import uk.ac.cam.db538.dexter.utils.Pair;

public class DexInteger extends DexPrimitiveType {
    DexInteger() { }

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
    public Pair<DexClassType, String> getPrimitiveClassConstantField(DexTypeCache cache) {
      return new Pair<DexClassType, String>(DexClassType.parse("Ljava/lang/Integer;", cache), "TYPE");
    }

    public static DexInteger parse(String typeDescriptor, DexTypeCache cache) {
    	if (!typeDescriptor.equals("I"))
    		throw new UnknownTypeException(typeDescriptor);
    	else
    		return cache.getCachedType_Integer();
    }
}