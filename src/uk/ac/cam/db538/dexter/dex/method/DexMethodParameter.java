package uk.ac.cam.db538.dexter.dex.method;

import lombok.Getter;

public class DexMethodParameter {

	@Getter private final DexMethod parentMethod;
	@Getter private final int paramIndex;
	
	public DexMethodParameter(DexMethod parentMethod) {
		this.parentMethod = parentMethod;
		this.paramIndex = 0;
	}

}
