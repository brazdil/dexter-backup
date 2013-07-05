package uk.ac.cam.db538.dexter.hierarchy;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import lombok.Getter;

import org.jf.dexlib.Util.AccessFlags;

import uk.ac.cam.db538.dexter.dex.type.DexClassType;

public class InterfaceDefinition extends BaseClassDefinition {

	private static final long serialVersionUID = 1L;
	
	final Set<BaseClassDefinition> _implementers;
	@Getter private final Set<BaseClassDefinition> implementers;

	public InterfaceDefinition(DexClassType classType, int accessFlags) {
		super(classType, accessFlags);
		
		if (!getAccessFlags().contains(AccessFlags.INTERFACE))
			throw new HierarchyException("Class is not an interface");

		this._implementers = new HashSet<BaseClassDefinition>();
		this.implementers = Collections.unmodifiableSet(this._implementers);
	}
	
	
}
