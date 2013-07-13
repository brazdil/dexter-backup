package uk.ac.cam.db538.dexter.dex.code.reg;

import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;

public abstract class DexSingleRegister extends DexStandardRegister {
	@Override
	public RegisterWidth getWidth() {
		return RegisterWidth.SINGLE;
	}

	@Override
	public boolean canStoreType(RegisterType type) {
		return type == RegisterType.SINGLE_PRIMITIVE || type == RegisterType.REFERENCE;
	}

	@Override
	public boolean canStoreType(DexRegisterType type) {
		return !type.isWide(); 
	}
}
