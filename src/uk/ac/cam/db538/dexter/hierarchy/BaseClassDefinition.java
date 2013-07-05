package uk.ac.cam.db538.dexter.hierarchy;

import java.io.Serializable;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import lombok.Getter;

import org.jf.dexlib.Util.AccessFlags;

import uk.ac.cam.db538.dexter.dex.type.DexClassType;

public abstract class BaseClassDefinition implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Getter private final DexClassType classType;
	private final int accessFlags;

	@Getter private BaseClassDefinition superclass;
	private final Set<BaseClassDefinition> _children;
	@Getter private final Set<BaseClassDefinition> children;

	private final Set<MethodDefinition> _methods;
	@Getter private final Set<MethodDefinition> methods;

	private final Set<StaticFieldDefinition> _staticFields;
	@Getter private final Set<StaticFieldDefinition> staticFields;

	BaseClassDefinition(DexClassType classType, int accessFlags) {
		this.classType = classType;
		this.accessFlags = accessFlags;
		
		this.superclass = null;
		this._children = new HashSet<BaseClassDefinition>();
		this.children = Collections.unmodifiableSet(this._children);

		this._methods = new HashSet<MethodDefinition>();
		this.methods = Collections.unmodifiableSet(this._methods);

		this._staticFields = new HashSet<StaticFieldDefinition>();
		this.staticFields = Collections.unmodifiableSet(this._staticFields);
	}
	
	// only to be called by HierarchyBuilder
	void setSuperclassLink(BaseClassDefinition superclass) {
		this.superclass = superclass;
		this.superclass._children.add(this);
	}
	
	void addDeclaredMethod(MethodDefinition method) {
		assert method.getParentClass() == this;
		
		this._methods.add(method);
	}
	
	void addDeclaredStaticField(StaticFieldDefinition field) {
		assert field.isStatic(); 
		assert field.getParentClass() == this;
		
		this._staticFields.add(field);
	}

	public EnumSet<AccessFlags> getAccessFlags() {
		AccessFlags[] flags = AccessFlags.getAccessFlagsForClass(accessFlags);
		if (flags.length == 0)
			return EnumSet.noneOf(AccessFlags.class);
		else
			return EnumSet.of(flags[0], flags);
	}
	
	public boolean isAbstract() {
		return getAccessFlags().contains(AccessFlags.ABSTRACT);
	}
}
