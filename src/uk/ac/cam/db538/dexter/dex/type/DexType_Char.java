package uk.ac.cam.db538.dexter.dex.type;

import uk.ac.cam.db538.dexter.utils.Pair;

public class DexType_Char extends DexType_Primitive {
    DexType_Char() { }

    @Override
	public DexRegisterTypeSize getTypeSize() {
    	return DexRegisterTypeSize.SINGLE;
	}

	@Override
	public String getDescriptor() {
		return "C";
	}

	@Override
	public String getPrettyName() {
		return "char";
	}

    @Override
    public Pair<DexType_Class, String> getPrimitiveClassConstantField(DexTypeCache cache) {
      return new Pair<DexType_Class, String>(DexType_Class.parse("Ljava/lang/Character;", cache), "TYPE");
    }

    public static DexType_Char parse(String typeDescriptor, DexTypeCache cache) {
    	if (!typeDescriptor.equals("C"))
    		throw new UnknownTypeException(typeDescriptor);
    	else
    		return cache.getCachedType_Char();
    }
}