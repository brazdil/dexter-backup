package uk.ac.cam.db538.dexter.dex.code.reg;

import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;


public abstract class DexWideRegister extends DexStandardRegister {
	@Override
	public RegisterWidth getWidth() {
		return RegisterWidth.WIDE;
	}

	@Override
	public boolean canStoreType(RegisterType type) {
		return type == RegisterType.WIDE_PRIMITIVE;
	}

	@Override
	public boolean canStoreType(DexRegisterType type) {
		return type.isWide(); 
	}
}
