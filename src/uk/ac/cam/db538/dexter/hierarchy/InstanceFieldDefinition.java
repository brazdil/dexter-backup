package uk.ac.cam.db538.dexter.hierarchy;

import java.util.EnumSet;

import lombok.Getter;

import org.jf.dexlib.Util.AccessFlags;

import uk.ac.cam.db538.dexter.dex.type.DexFieldId;

public class InstanceFieldDefinition {

	@Getter private final ClassDefinition parentClass;
	@Getter private final DexFieldId fieldId;
	private final int accessFlags;
	
	InstanceFieldDefinition(ClassDefinition cls, DexFieldId fieldId, int accessFlags) {
		this.parentClass = cls;
		this.fieldId = fieldId;
		this.accessFlags = accessFlags;
	}

	public EnumSet<AccessFlags> getAccessFlags() {
		AccessFlags[] flags = AccessFlags.getAccessFlagsForField(accessFlags);
		if (flags.length == 0)
			return EnumSet.noneOf(AccessFlags.class);
		else
			return EnumSet.of(flags[0], flags);
	}
	
	public boolean isStatic() {
		return getAccessFlags().contains(AccessFlags.STATIC);
	}
}
