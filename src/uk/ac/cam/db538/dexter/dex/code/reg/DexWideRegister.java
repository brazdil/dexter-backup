package uk.ac.cam.db538.dexter.dex.code.reg;

import lombok.Getter;

public class DexWideRegister extends DexOriginalRegister {

	@Getter private final int id;
	
	public DexWideRegister(int id) {
		this.id = id;
	}

	@Override
	String getPlainId() {
		return Integer.toString(id) + "|" + Integer.toString(id + 1);
	}

	@Override
	public RegisterWidth getWidth() {
		return RegisterWidth.WIDE;
	}
}
