package uk.ac.cam.db538.dexter.hierarchy;

import java.util.EnumSet;

import org.jf.dexlib.Util.AccessFlags;

import uk.ac.cam.db538.dexter.dex.type.DexPrototype;
import lombok.Getter;

public class MethodInfo {

	@Getter private final String name;
	@Getter private final DexPrototype signature;
	private final int accessFlags;
	
	MethodInfo(String name, DexPrototype signature, int accessFlags) {
		this.name = name;
		this.signature = signature;
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
