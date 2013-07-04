package uk.ac.cam.db538.dexter.dex.type;

import lombok.Getter;

public class DexArrayType extends DexReferenceType {

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
}
