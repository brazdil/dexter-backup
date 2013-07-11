package uk.ac.cam.db538.dexter.dex.code.reg;


public abstract class DexRegister {
	public abstract RegisterWidth getWidth();
	public abstract DexTaintRegister getTaintRegister();
}
