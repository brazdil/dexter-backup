package uk.ac.cam.db538.dexter.dex.type;

import lombok.Getter;

public class DexType_Array extends DexType_Reference {

	@Getter private final DexType_Register elementType;

	private DexType_Array(DexType_Register elementType) {
		this.elementType = elementType;
	}

	public static DexType_Array parse(String typeDescriptor, DexTypeCache cache) {
		if (!typeDescriptor.startsWith("["))
			throw new UnknownTypeException(typeDescriptor);
		
		DexType_Array type = cache.getCachedType_Array(typeDescriptor);
		if (type == null) {
			type = new DexType_Array(DexType_Register.parse(typeDescriptor.substring(1), cache));
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
