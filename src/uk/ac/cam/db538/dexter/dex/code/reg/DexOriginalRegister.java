package uk.ac.cam.db538.dexter.dex.code.reg;

import lombok.Getter;

public abstract class DexOriginalRegister extends DexRegister {

	@Getter private final DexTaintRegister taintRegister;
	
	public DexOriginalRegister() {
		this.taintRegister = new DexTaintRegister(this);
	}
	
	abstract String getPlainId(); 

	@Override
	public String toString() {
		return "v" + getPlainId();
	}
}
