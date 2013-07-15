package uk.ac.cam.db538.dexter.dex.code.reg;

public enum RegisterWidth {
	SINGLE,
	WIDE;

	public int getRegisterCount() {
		switch (this) {
		case SINGLE:
			return 1;
		case WIDE:
			return 2;
		}
		throw new RuntimeException("Unknown register size");
	}
}