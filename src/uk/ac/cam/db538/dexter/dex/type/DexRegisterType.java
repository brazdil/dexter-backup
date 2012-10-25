package uk.ac.cam.db538.dexter.dex.type;

import lombok.Getter;
import lombok.val;

public abstract class DexRegisterType extends DexType {
	
	@Getter private final int Registers;

	public DexRegisterType(String descriptor, String prettyName, int registers) {
		super(descriptor, prettyName);
		Registers = registers;
	}
	
	public static DexRegisterType parse(String typeDescriptor, TypeCache cache) {
		val primitive = DexPrimitive.parse(typeDescriptor);
		if (primitive != null)
			return primitive;

		val classtype = DexClassType.parse(typeDescriptor, cache);
		if (classtype != null)
			return classtype;
		
		val array = DexArrayType.parse(typeDescriptor, cache);
		if (array != null)
			return array;
		
		return null;
	}
}
