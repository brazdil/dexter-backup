package uk.ac.cam.db538.dexter.dex.code.reg;


public abstract class DexWideRegister extends DexStandardRegister {
	@Override
	public RegisterWidth getWidth() {
		return RegisterWidth.WIDE;
	}

	@Override
	public boolean storesType(RegisterType type) {
		return type == RegisterType.WIDE_PRIMITIVE;
	}
}
