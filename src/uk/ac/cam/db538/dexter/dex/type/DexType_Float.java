package uk.ac.cam.db538.dexter.dex.type;

import uk.ac.cam.db538.dexter.utils.Pair;

public class DexType_Float extends DexType_Primitive {
    DexType_Float() { }

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
    public Pair<DexType_Class, String> getPrimitiveClassConstantField(DexTypeCache cache) {
      return new Pair<DexType_Class, String>(DexType_Class.parse("Ljava/lang/Float;", cache), "TYPE");
    }

    public static DexType_Float parse(String typeDescriptor, DexTypeCache cache) {
    	if (!typeDescriptor.equals("F"))
    		throw new UnknownTypeException(typeDescriptor);
    	else
    		return cache.getCachedType_Float();
    }
}