package uk.ac.cam.db538.dexter.dex.type;

import lombok.Getter;

public class DexArrayType extends DexReferenceType {

	private static final long serialVersionUID = 1L;
	
	@Getter private final DexRegisterType elementType;

	private DexArrayType(DexRegisterType elementType) {
		this.elementType = elementType;
	}

	public static DexArrayType parse(String typeDescriptor, DexTypeCache cache) {
		if (!typeDescriptor.startsWith("["))
			throw new UnknownTypeException(typeDescriptor);
		
		DexArrayType type = cache.getCachedType_Array(typeDescriptor);
		if (type == null) {
			type = new DexArrayType(DexRegisterType.parse(typeDescriptor.substring(1), cache));
			cache.putCachedType_Array(typeDescriptor, type);
		}
		
		return type;
	}
	
	@Override
	public String getDescriptor() {
		return "[" + elementType.getDescriptor();
	}
	
	@Override
	public String getPrettyName() {
		return elementType.getPrettyName() + "[]";
	}

	@Override
	public String getJavaDescriptor() {
		if (elementType instanceof DexClassType)
			return "[L" + elementType.getPrettyName() + ";";
		else
			return "[" + elementType.getDescriptor();
		
	}
	
	public static String jvm2dalvik(String jvmName) {
		if (jvmName.startsWith("[")) {
			String element = jvmName.substring(1);

			// try to parse the element as a Dalvik primitive (descriptors match)
			// dirty hack: primitive will throw NullPointerException
			// if it tries to access type cache (in which case it parsed the name)
			try {
				DexPrimitiveType.parse(element, null);
				throw new Error("The previous command should have thrown an exception");
			} catch (NullPointerException ex) {
				// primitive
				return jvmName;
			} catch (UnknownTypeException ex) {
				// not primitive
				return "[" + DexReferenceType.jvm2dalvik(element);				
			}
		} else
			throw new UnknownTypeException(jvmName);
	}
}
