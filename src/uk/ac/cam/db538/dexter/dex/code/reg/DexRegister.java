package uk.ac.cam.db538.dexter.dex.code.reg;

import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;

public abstract class DexRegister {
	public abstract RegisterWidth getWidth();
	public abstract boolean canStoreType(RegisterType type);
	public abstract boolean canStoreType(DexRegisterType type);

	public DexTaintRegister getTaintRegister() {
		throw new Error("Cannot get the taint register of a non-original register.");
	}
}
