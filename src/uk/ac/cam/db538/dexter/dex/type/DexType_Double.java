package uk.ac.cam.db538.dexter.dex.type;

import uk.ac.cam.db538.dexter.utils.Pair;

public class DexType_Double extends DexType_Primitive {
    DexType_Double() { }

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
    public Pair<DexType_Class, String> getPrimitiveClassConstantField(DexTypeCache cache) {
      return new Pair<DexType_Class, String>(DexType_Class.parse("Ljava/lang/Double;", cache), "TYPE");
    }

    public static DexType_Double parse(String typeDescriptor, DexTypeCache cache) {
    	if (!typeDescriptor.equals("D"))
    		throw new UnknownTypeException(typeDescriptor);
    	else
    		return cache.getCachedType_Double();
    }
}