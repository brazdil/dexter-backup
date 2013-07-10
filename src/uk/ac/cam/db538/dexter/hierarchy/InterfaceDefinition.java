package uk.ac.cam.db538.dexter.hierarchy;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import lombok.Getter;

import org.jf.dexlib.Util.AccessFlags;

import uk.ac.cam.db538.dexter.dex.type.DexClassType;

public class InterfaceDefinition extends BaseClassDefinition {

	private static final long serialVersionUID = 1L;
	
	final Set<ClassDefinition> _implementors;
	@Getter private final Set<ClassDefinition> implementors;

	public InterfaceDefinition(DexClassType classType, int accessFlags, boolean isInternal) {
		super(classType, accessFlags, isInternal);
		
		if (!getAccessFlags().contains(AccessFlags.INTERFACE))
			throw new HierarchyException("Class is not an interface");

		this._implementors = new HashSet<ClassDefinition>();
		this.implementors = Collections.unmodifiableSet(this._implementors);
	}
	
	public boolean isAnnotation() {
		return getAccessFlags().contains(AccessFlags.ANNOTATION);
	}
}
