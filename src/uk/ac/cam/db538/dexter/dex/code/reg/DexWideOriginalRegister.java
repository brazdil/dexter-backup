package uk.ac.cam.db538.dexter.dex.code.reg;

import lombok.Getter;

public class DexWideOriginalRegister extends DexWideRegister {

	@Getter private final int id;
	private final DexTaintRegister taintRegister;
	
	public DexWideOriginalRegister(int id) {
		this.id = id;
		this.taintRegister = new DexTaintRegister(this);
	}

	@Override
	String getPlainId() {
		return Integer.toString(id) + "|" + Integer.toString(id + 1);
	}

	@Override
	public DexTaintRegister getTaintRegister() {
		return this.taintRegister;
	}
}
