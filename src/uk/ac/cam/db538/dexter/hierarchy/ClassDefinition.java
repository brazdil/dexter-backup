package uk.ac.cam.db538.dexter.hierarchy;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import lombok.Getter;
import lombok.val;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexFieldId;

public class ClassDefinition extends BaseClassDefinition {

	private static final long serialVersionUID = 1L;
	
	private final Set<InterfaceDefinition> _interfaces;
	@Getter private final Set<InterfaceDefinition> interfaces;

	private final Set<InstanceFieldDefinition> _instanceFields;
	@Getter private final Set<InstanceFieldDefinition> instanceFields;

	ClassDefinition(DexClassType classType, int accessFlags, boolean isInternal) {
		super(classType, accessFlags, isInternal);
		
		this._interfaces = new HashSet<InterfaceDefinition>();
		this.interfaces = Collections.unmodifiableSet(this._interfaces);

		this._instanceFields = new HashSet<InstanceFieldDefinition>();
		this.instanceFields = Collections.unmodifiableSet(this._instanceFields);
	}
	
	void addImplementedInterface(InterfaceDefinition iface) {
		this._interfaces.add(iface);
		iface._implementors.add(this);
	}

	void addDeclaredInstanceField(InstanceFieldDefinition field) {
		assert !field.isStatic();
		assert field.getParentClass() == this;
		
		this._instanceFields.add(field);
	}
	
	public boolean implementsInterface(InterfaceDefinition iface) {
		BaseClassDefinition inspected = this;
		
		while (true) {
			if (inspected instanceof ClassDefinition) {
				for (val ifaceDef : ((ClassDefinition) inspected).getInterfaces()) 
					if (ifaceDef.equals(iface))
						return true;
			}
			
			if (inspected.isRoot())
				return false;
			else
				inspected = inspected.getSuperclass();
		}  
	}
	
	public InstanceFieldDefinition getInstanceField(DexFieldId fieldId) {
		for (val fieldDef : this.instanceFields)
			if (fieldDef.getFieldId().equals(fieldId))
				return fieldDef;
		return null;
	}
	
	public InstanceFieldDefinition getAccessedInstanceField(DexFieldId fieldId) {
		// Application can access an instance field on class X, but
		// the field might actually be defined in one of X's parents
		// This method will return the definition of the field 
		// in itself or the closest parent
		
		return iterateThroughParents(fieldId, extractorInstanceField, acceptorAlwaysTrue, false);
	}
	
	private static final Extractor<DexFieldId, InstanceFieldDefinition> extractorInstanceField = new Extractor<DexFieldId, InstanceFieldDefinition>() {
		@Override
		public InstanceFieldDefinition extract(BaseClassDefinition clazz, DexFieldId fieldId) {
			if (clazz instanceof ClassDefinition) {
				val fieldDef = ((ClassDefinition) clazz).getInstanceField(fieldId);
				return fieldDef;
			} else
				return null;
		}
	};
}
