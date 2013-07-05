package uk.ac.cam.db538.dexter.dex.type;

import uk.ac.cam.db538.dexter.utils.Pair;

public class DexShort extends DexPrimitiveType {

	private static final long serialVersionUID = 1L;

	DexShort() { }

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
    public Pair<DexClassType, String> getPrimitiveClassConstantField(DexTypeCache cache) {
      return new Pair<DexClassType, String>(DexClassType.parse("Ljava/lang/Short;", cache), "TYPE");
    }

    public static DexShort parse(String typeDescriptor, DexTypeCache cache) {
    	if (!typeDescriptor.equals("S"))
    		throw new UnknownTypeException(typeDescriptor);
    	else
    		return cache.getCachedType_Short();
    }
}