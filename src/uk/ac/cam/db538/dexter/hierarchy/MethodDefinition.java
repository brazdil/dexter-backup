package uk.ac.cam.db538.dexter.hierarchy;

import java.util.EnumSet;

import lombok.Getter;

import org.jf.dexlib.Util.AccessFlags;

import uk.ac.cam.db538.dexter.dex.type.DexMethodId;

public class MethodDefinition {

	@Getter private final BaseClassDefinition parentClass;
	@Getter private final DexMethodId methodId;
	private final int accessFlags;
	
	MethodDefinition(BaseClassDefinition parentClass, DexMethodId methodId, int accessFlags) {
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
	
	public boolean hasBytecode() {
		return !isAbstract() && !isNative();
	}
}
