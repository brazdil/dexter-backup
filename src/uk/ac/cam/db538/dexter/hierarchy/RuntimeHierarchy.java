package uk.ac.cam.db538.dexter.hierarchy;

import java.util.Map;

import lombok.Getter;
import lombok.val;

import uk.ac.cam.db538.dexter.dex.type.DexArrayType;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexReferenceType;
import uk.ac.cam.db538.dexter.dex.type.DexTypeCache;

public class RuntimeHierarchy {

	@Getter private final DexTypeCache typeCache;
	private final Map<DexClassType, BaseClassDefinition> definedClasses;
	private final ClassDefinition root; 
	
	RuntimeHierarchy(Map<DexClassType, BaseClassDefinition> definedClasses, ClassDefinition root, DexTypeCache typeCache) {
		this.definedClasses = definedClasses;
		this.root = root;
		this.typeCache = typeCache;
	}
	
	public BaseClassDefinition getBaseClassDefinition(DexReferenceType refType) {
		if (refType instanceof DexClassType)
			return definedClasses.get((DexClassType) refType);
		else if (refType instanceof DexArrayType)
			return root;
		else
			throw new Error();
	}
	
	public ClassDefinition getClassDefinition(DexReferenceType refType) {
		val baseClass = getBaseClassDefinition(refType);
		if (baseClass instanceof ClassDefinition)
			return (ClassDefinition) baseClass;
		else
			throw new HierarchyException("Type " + refType.getPrettyName() + " is not a proper class");
	}

	public InterfaceDefinition getInterfaceDefinition(DexReferenceType refType) {
		val baseClass = getBaseClassDefinition(refType);
		if (baseClass instanceof InterfaceDefinition)
			return (InterfaceDefinition) baseClass;
		else
			throw new HierarchyException("Type " + refType.getPrettyName() + " is not an interface class");
	}
}
