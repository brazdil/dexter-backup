package uk.ac.cam.db538.dexter.hierarchy.builder;

import java.util.Collection;

import uk.ac.cam.db538.dexter.dex.type.DexClassType;

public interface IClassScanner {

	public String getClassDescriptor();
	public String getSuperclassDescriptor(); // null if root
	
	public boolean isInterface();
	public int getAccessFlags();
	
	public Collection<DexClassType> getInterfaces();

	public Collection<IMethodScanner> getMethodScanners();
	public Collection<IFieldScanner> getStaticFieldScanners();
	public Collection<IFieldScanner> getInstanceFieldScanners();
}
