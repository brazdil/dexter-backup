package uk.ac.cam.db538.dexter.dex.type;

import uk.ac.cam.db538.dexter.utils.Pair;

public class DexChar extends DexPrimitiveType {
	
	private static final long serialVersionUID = 1L;

	DexChar() { }

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
    public Pair<DexClassType, String> getPrimitiveClassConstantField(DexTypeCache cache) {
      return new Pair<DexClassType, String>(DexClassType.parse("Ljava/lang/Character;", cache), "TYPE");
    }

    public static DexChar parse(String typeDescriptor, DexTypeCache cache) {
    	if (!typeDescriptor.equals("C"))
    		throw new UnknownTypeException(typeDescriptor);
    	else
    		return cache.getCachedType_Char();
    }
}