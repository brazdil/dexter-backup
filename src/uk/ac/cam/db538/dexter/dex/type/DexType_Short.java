package uk.ac.cam.db538.dexter.dex.type;

import uk.ac.cam.db538.dexter.utils.Pair;

public class DexType_Short extends DexType_Primitive {
    DexType_Short() { }

    @Override
	public DexRegisterTypeSize getTypeSize() {
    	return DexRegisterTypeSize.SINGLE;
	}

	@Override
	public String getDescriptor() {
		return "S";
	}

	@Override
	public String getPrettyName() {
		return "short";
	}
    
    @Override
    public Pair<DexType_Class, String> getPrimitiveClassConstantField(DexTypeCache cache) {
      return new Pair<DexType_Class, String>(DexType_Class.parse("Ljava/lang/Short;", cache), "TYPE");
    }

    public static DexType_Short parse(String typeDescriptor, DexTypeCache cache) {
    	if (!typeDescriptor.equals("S"))
    		throw new UnknownTypeException(typeDescriptor);
    	else
    		return cache.getCachedType_Short();
    }
}