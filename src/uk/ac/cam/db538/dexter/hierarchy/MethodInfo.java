package uk.ac.cam.db538.dexter.hierarchy;

import uk.ac.cam.db538.dexter.dex.type.DexPrototype;
import lombok.Getter;

public class MethodInfo {

	@Getter private final String name;
	@Getter private final DexPrototype signature;
	private final int accessFlags;
	
	MethodInfo(String name, DexPrototype signature, int accessFlags) {
		this.name = name;
		this.signature = signature;
		this.accessFlags = accessFlags;
	}

}
