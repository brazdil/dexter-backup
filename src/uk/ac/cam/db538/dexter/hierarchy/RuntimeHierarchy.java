package uk.ac.cam.db538.dexter.hierarchy;

import java.util.Map;

import uk.ac.cam.db538.dexter.dex.type.DexClassType;

public class RuntimeHierarchy {

	private final Map<DexClassType, BaseClassInfo> definedClasses;
	
	RuntimeHierarchy(Map<DexClassType, BaseClassInfo> definedClasses) {
		this.definedClasses = definedClasses;
	}
	
	public BaseClassInfo getClassInfo(DexClassType clsType) {
		return definedClasses.get(clsType);
	}
}
