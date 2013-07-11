package uk.ac.cam.db538.dexter.dex.code.reg;


public abstract class DexOriginalRegister extends DexRegister {

	private final DexTaintRegister taintRegister;
	
	public DexOriginalRegister() {
		this.taintRegister = new DexTaintRegister(this);
	}
	
	abstract String getPlainId(); 

	@Override
	public String toString() {
		return "v" + getPlainId();
	}

	@Override
	public DexTaintRegister getTaintRegister() {
		return this.taintRegister;
	}
}
