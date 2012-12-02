package uk.ac.cam.db538.dexter.dex.code;

import java.util.Collections;
import java.util.Map;

import lombok.Getter;
import uk.ac.cam.db538.dexter.dex.DexAssemblingCache;

public class DexCode_AssemblingState {

	@Getter private DexAssemblingCache cache;
	private Map<DexRegister, Integer> registerAllocation;
	
	public DexCode_AssemblingState(DexAssemblingCache cache, Map<DexRegister, Integer> regAlloc) {
		this.cache = cache;
		this.registerAllocation = regAlloc;
	}
	
	public Map<DexRegister, Integer> getRegisterAllocation() {
		return Collections.unmodifiableMap(registerAllocation);
	}
}
