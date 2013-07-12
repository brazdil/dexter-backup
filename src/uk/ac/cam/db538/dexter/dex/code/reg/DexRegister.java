package uk.ac.cam.db538.dexter.dex.code.reg;

public abstract class DexRegister {
	public abstract RegisterWidth getWidth();
	public abstract boolean storesType(RegisterType type);

	public DexTaintRegister getTaintRegister() {
		throw new Error("Cannot get the taint register of a non-original register.");
	}
}
