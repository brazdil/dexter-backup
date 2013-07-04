package uk.ac.cam.db538.dexter.dex.type;

import uk.ac.cam.db538.dexter.utils.Pair;

public class DexDouble extends DexPrimitiveType {
    DexDouble() { }

    @Override
	public DexRegisterTypeSize getTypeSize() {
    	return DexRegisterTypeSize.WIDE;
	}

	@Override
	public String getDescriptor() {
		return "D";
	}

	@Override
	public String getPrettyName() {
		return "double";
	}

	@Override
    public Pair<DexClassType, String> getPrimitiveClassConstantField(DexTypeCache cache) {
      return new Pair<DexClassType, String>(DexClassType.parse("Ljava/lang/Double;", cache), "TYPE");
    }

    public static DexDouble parse(String typeDescriptor, DexTypeCache cache) {
    	if (!typeDescriptor.equals("D"))
    		throw new UnknownTypeException(typeDescriptor);
    	else
    		return cache.getCachedType_Double();
    }
}