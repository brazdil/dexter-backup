package uk.ac.cam.db538.dexter.hierarchy;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import lombok.Getter;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;

public class ClassDefinition extends BaseClassDefinition {

	@Getter private boolean root;

	private final Set<InterfaceDefinition> _interfaces;
	@Getter private final Set<InterfaceDefinition> interfaces;

	ClassDefinition(DexClassType classType, int accessFlags, boolean isRoot) {
		super(classType, accessFlags);
		this.root = isRoot;
		
		this._interfaces = new HashSet<InterfaceDefinition>();
		this.interfaces = Collections.unmodifiableSet(this._interfaces);
	}
	
	void setImplementsInterface(InterfaceDefinition iface) {
		this._interfaces.add(iface);
		iface._implementers.add(this);
	}
}
