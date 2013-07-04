package uk.ac.cam.db538.dexter.dex.type;

import uk.ac.cam.db538.dexter.utils.Pair;

public class DexBoolean extends DexPrimitiveType {
    DexBoolean() { }

    @Override
	public DexRegisterTypeSize getTypeSize() {
    	return DexRegisterTypeSize.SINGLE;
	}

	@Override
	public String getDescriptor() {
		return "Z";
	}

	@Override
	public String getPrettyName() {
		return "boolean";
	}
    
    @Override
    public Pair<DexClassType, String> getPrimitiveClassConstantField(DexTypeCache cache) {
      return new Pair<DexClassType, String>(DexClassType.parse("Ljava/lang/Boolean;", cache), "TYPE");
    }
    
    public static DexBoolean parse(String typeDescriptor, DexTypeCache cache) {
    	if (!typeDescriptor.equals("Z"))
    		throw new UnknownTypeException(typeDescriptor);
    	else
    		return cache.getCachedType_Boolean();
    }
  }