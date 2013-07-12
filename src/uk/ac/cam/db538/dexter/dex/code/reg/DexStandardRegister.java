package uk.ac.cam.db538.dexter.dex.code.reg;


public abstract class DexStandardRegister extends DexRegister {

	abstract String getPlainId(); 

	@Override
	public String toString() {
		return "v" + getPlainId();
	}
}
