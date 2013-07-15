package uk.ac.cam.db538.dexter.dex.type;

import uk.ac.cam.db538.dexter.dex.code.reg.RegisterWidth;
import uk.ac.cam.db538.dexter.utils.Pair;

public class DexBoolean extends DexPrimitiveType {
	
	private static final long serialVersionUID = 1L;

	private static String DESCRIPTOR = "Z";
	private static String NAME = "boolean";
	
	DexBoolean() { }

    @Override
	public RegisterWidth getTypeWidth() {
    	return RegisterWidth.SINGLE;
	}

	@Override
	public String getDescriptor() {
		return DESCRIPTOR;
	}

	@Override
	public String getPrettyName() {
		return NAME;
	}
    
    @Override
    public Pair<DexClassType, String> getPrimitiveClassConstantField(DexTypeCache cache) {
      return new Pair<DexClassType, String>(DexClassType.parse("Ljava/lang/Boolean;", cache), "TYPE");
    }
    
    public static DexBoolean parse(String typeDescriptor, DexTypeCache cache) {
    	if (typeDescriptor.equals(DESCRIPTOR))
    		return cache.getCachedType_Boolean();
    	else
    		throw new UnknownTypeException(typeDescriptor);
    }

	public static String jvm2dalvik(String javaName) {
		if (javaName.equals(NAME))
			return DESCRIPTOR;
		else
			throw new UnknownTypeException(javaName);
	}
}