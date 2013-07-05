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

	private final Set<InstanceFieldDefinition> _instanceFields;
	@Getter private final Set<InstanceFieldDefinition> instanceFields;

	ClassDefinition(DexClassType classType, int accessFlags, boolean isRoot) {
		super(classType, accessFlags);
		this.root = isRoot;
		
		this._interfaces = new HashSet<InterfaceDefinition>();
		this.interfaces = Collections.unmodifiableSet(this._interfaces);

		this._instanceFields = new HashSet<InstanceFieldDefinition>();
		this.instanceFields = Collections.unmodifiableSet(this._instanceFields);
	}
	
	void addImplementedInterface(InterfaceDefinition iface) {
		this._interfaces.add(iface);
		iface._implementers.add(this);
	}

	void addDeclaredInstanceField(InstanceFieldDefinition field) {
		assert !field.isStatic();
		assert field.getParentClass() == this;
		
		this._instanceFields.add(field);
	}
}
