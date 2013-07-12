package uk.ac.cam.db538.dexter.dex.code.reg;

public abstract class DexSingleRegister extends DexStandardRegister {
	@Override
	public RegisterWidth getWidth() {
		return RegisterWidth.SINGLE;
	}

	@Override
	public boolean storesType(RegisterType type) {
		return type == RegisterType.SINGLE_PRIMITIVE || type == RegisterType.REFERENCE;
	}
}
