package uk.ac.cam.db538.dexter.hierarchy;

import java.util.Map;

import uk.ac.cam.db538.dexter.dex.type.DexClassType;

public class RuntimeHierarchy {

	private final Map<DexClassType, BaseClassDefinition> definedClasses;
	
	RuntimeHierarchy(Map<DexClassType, BaseClassDefinition> definedClasses) {
		this.definedClasses = definedClasses;
	}
	
	public BaseClassDefinition getClassInfo(DexClassType clsType) {
		return definedClasses.get(clsType);
	}
}
