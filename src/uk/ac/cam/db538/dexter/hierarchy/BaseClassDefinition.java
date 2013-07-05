package uk.ac.cam.db538.dexter.hierarchy;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import lombok.Getter;

import org.jf.dexlib.Util.AccessFlags;

import uk.ac.cam.db538.dexter.dex.type.DexClassType;

public abstract class BaseClassDefinition {

	@Getter private final DexClassType classType;
	private final int accessFlags;

	@Getter private BaseClassDefinition superclass;
	private final Set<BaseClassDefinition> _children;
	@Getter private final Set<BaseClassDefinition> children;

	private final Set<MethodDefinition> _methods;
	@Getter private final Set<MethodDefinition> methods;

	BaseClassDefinition(DexClassType classType, int accessFlags) {
		this.classType = classType;
		this.accessFlags = accessFlags;
		
		this.superclass = null;
		this._children = new HashSet<BaseClassDefinition>();
		this.children = Collections.unmodifiableSet(this._children);

		this._methods = new HashSet<MethodDefinition>();
		this.methods = Collections.unmodifiableSet(this._methods);
	}
	
	// only to be called by HierarchyBuilder
	void setSuperclassLink(BaseClassDefinition superclass) {
		this.superclass = superclass;
		this.superclass._children.add(this);
	}
	
	void addDefinedMethod(MethodDefinition method) {
		assert method.getParentClass() == this;
		
		this._methods.add(method);
	}
	
	public EnumSet<AccessFlags> getAccessFlags() {
		AccessFlags[] flags = AccessFlags.getAccessFlagsForClass(accessFlags);
		if (flags.length == 0)
			return EnumSet.noneOf(AccessFlags.class);
		else
			return EnumSet.of(flags[0], flags);
	}
}
