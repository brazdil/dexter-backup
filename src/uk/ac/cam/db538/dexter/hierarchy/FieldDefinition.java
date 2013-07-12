package uk.ac.cam.db538.dexter.hierarchy;

import java.io.Serializable;
import java.util.EnumSet;

import lombok.Getter;

import org.jf.dexlib.Util.AccessFlags;

import uk.ac.cam.db538.dexter.dex.type.DexFieldId;

public class FieldDefinition implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Getter private final BaseClassDefinition parentClass;
	@Getter private final DexFieldId fieldId;
	private final int accessFlags;
	
	public FieldDefinition(BaseClassDefinition cls, DexFieldId fieldId, int accessFlags) {
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
	
	@Override
	public String toString() {
		return parentClass.getType().getDescriptor() + "->" + fieldId.getName() + ":" + fieldId.getType().getDescriptor();
	}
}
