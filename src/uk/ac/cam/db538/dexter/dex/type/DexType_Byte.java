package uk.ac.cam.db538.dexter.dex.type;

import uk.ac.cam.db538.dexter.utils.Pair;

public class DexType_Byte extends DexType_Primitive {
    DexType_Byte() { }
    
    @Override
	public DexRegisterTypeSize getTypeSize() {
    	return DexRegisterTypeSize.SINGLE;
	}

	@Override
	public String getDescriptor() {
		return "B";
	}

	@Override
	public String getPrettyName() {
		return "byte";
	}

	@Override
    public Pair<DexType_Class, String> getPrimitiveClassConstantField(DexTypeCache cache) {
      return new Pair<DexType_Class, String>(DexType_Class.parse("Ljava/lang/Byte;", cache), "TYPE");
    }

    public static DexType_Byte parse(String typeDescriptor, DexTypeCache cache) {
    	if (!typeDescriptor.equals("B"))
    		throw new UnknownTypeException(typeDescriptor);
    	else
    		return cache.getCachedType_Byte();
    }
}