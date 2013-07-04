package uk.ac.cam.db538.dexter.dex.type;

import uk.ac.cam.db538.dexter.utils.Pair;

public class DexByte extends DexPrimitiveType {
    DexByte() { }
    
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
    public Pair<DexClassType, String> getPrimitiveClassConstantField(DexTypeCache cache) {
      return new Pair<DexClassType, String>(DexClassType.parse("Ljava/lang/Byte;", cache), "TYPE");
    }

    public static DexByte parse(String typeDescriptor, DexTypeCache cache) {
    	if (!typeDescriptor.equals("B"))
    		throw new UnknownTypeException(typeDescriptor);
    	else
    		return cache.getCachedType_Byte();
    }
}