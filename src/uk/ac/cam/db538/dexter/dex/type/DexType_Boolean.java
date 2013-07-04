package uk.ac.cam.db538.dexter.dex.type;

import uk.ac.cam.db538.dexter.utils.Pair;

public class DexType_Boolean extends DexType_Primitive {
    DexType_Boolean() { }

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
    public Pair<DexType_Class, String> getPrimitiveClassConstantField(DexTypeCache cache) {
      return new Pair<DexType_Class, String>(DexType_Class.parse("Ljava/lang/Boolean;", cache), "TYPE");
    }
    
    public static DexType_Boolean parse(String typeDescriptor, DexTypeCache cache) {
    	if (!typeDescriptor.equals("Z"))
    		throw new UnknownTypeException(typeDescriptor);
    	else
    		return cache.getCachedType_Boolean();
    }
  }