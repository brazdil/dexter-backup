package uk.ac.cam.db538.dexter.dex.type;

import uk.ac.cam.db538.dexter.utils.Pair;

public class DexFloat extends DexPrimitiveType {
    DexFloat() { }

    @Override
	public DexRegisterTypeSize getTypeSize() {
    	return DexRegisterTypeSize.SINGLE;
	}

	@Override
	public String getDescriptor() {
		return "F";
	}

	@Override
	public String getPrettyName() {
		return "float";
	}

	@Override
    public Pair<DexClassType, String> getPrimitiveClassConstantField(DexTypeCache cache) {
      return new Pair<DexClassType, String>(DexClassType.parse("Ljava/lang/Float;", cache), "TYPE");
    }

    public static DexFloat parse(String typeDescriptor, DexTypeCache cache) {
    	if (!typeDescriptor.equals("F"))
    		throw new UnknownTypeException(typeDescriptor);
    	else
    		return cache.getCachedType_Float();
    }
}