package uk.ac.cam.db538.dexter.hierarchy.builder;

import uk.ac.cam.db538.dexter.dex.type.DexMethodId;

public interface IMethodScanner {

	public DexMethodId getMethodId();
	public int getAccessFlags();
	
}
