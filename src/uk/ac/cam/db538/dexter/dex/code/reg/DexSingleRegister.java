package uk.ac.cam.db538.dexter.dex.code.reg;

import lombok.Getter;

public class DexSingleRegister extends DexOriginalRegister {

	@Getter private final int id;
	
	public DexSingleRegister(int id) {
		this.id = id;
	}

	@Override
	String getPlainId() {
		return Integer.toString(id);
	}
}
