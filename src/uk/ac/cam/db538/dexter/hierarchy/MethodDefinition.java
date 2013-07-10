package uk.ac.cam.db538.dexter.hierarchy;

import java.io.Serializable;
import java.util.EnumSet;

import lombok.Getter;

import org.jf.dexlib.Util.AccessFlags;

import uk.ac.cam.db538.dexter.dex.type.DexMethodId;

public class MethodDefinition implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Getter private final BaseClassDefinition parentClass;
	@Getter private final DexMethodId methodId;
	private final int accessFlags;
	
	public MethodDefinition(BaseClassDefinition parentClass, DexMethodId methodId, int accessFlags) {
		this.parentClass = parentClass;
		this.methodId = methodId;
		this.accessFlags = accessFlags;
	}

	public EnumSet<AccessFlags> getAccessFlags() {
		AccessFlags[] flags = AccessFlags.getAccessFlagsForMethod(accessFlags);
		if (flags.length == 0)
			return EnumSet.noneOf(AccessFlags.class);
		else
			return EnumSet.of(flags[0], flags);
	}
	
	public boolean isAbstract() {
		return getAccessFlags().contains(AccessFlags.ABSTRACT);
	}
	
	public boolean isNative() {
		return getAccessFlags().contains(AccessFlags.NATIVE);
	}
	
	public boolean isStatic() {
		return getAccessFlags().contains(AccessFlags.STATIC);
	}

	public boolean isPublic() {
		return getAccessFlags().contains(AccessFlags.PUBLIC);
	}
	
	public boolean isPrivate() {
		return getAccessFlags().contains(AccessFlags.PRIVATE);
	}
	
	public boolean isConstructor() {
		return getAccessFlags().contains(AccessFlags.CONSTRUCTOR);
	}

	public boolean hasBytecode() {
		return !isAbstract() && !isNative();
	}

	@Override
	public String toString() {
		return parentClass.getType().getDescriptor() + "->" + methodId.getName() + methodId.getPrototype().getDescriptor();
	}
}
