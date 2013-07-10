package uk.ac.cam.db538.dexter.hierarchy.builder;

import uk.ac.cam.db538.dexter.dex.type.DexFieldId;

public interface IFieldScanner {

	public DexFieldId getFieldId();
	public int getAccessFlags();
	
}
