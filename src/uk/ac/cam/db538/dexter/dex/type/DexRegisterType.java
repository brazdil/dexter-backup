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
		val res = DexPrimitive.parse(typeDescriptor);
		if (res != null)
			return res;
		else
			return DexClassType.parse(typeDescriptor, cache);
	}
}
