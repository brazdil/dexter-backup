package uk.ac.cam.db538.dexter.dex;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

import org.jf.dexlib.ClassDataItem.EncodedField;
import org.jf.dexlib.StringIdItem;
import org.jf.dexlib.Util.AccessFlags;

import lombok.Getter;
import lombok.Setter;

public class DexField {
	
	@Getter @Setter private DexClass ParentClass;
	@Getter private final String Name;
	@Getter private final Set<AccessFlags> AccessFlagSet; 
	
	public DexField(DexClass parent, String name, Set<AccessFlags> accessFlags) {
		ParentClass = parent;
		Name = name;
		AccessFlagSet = (accessFlags == null) ? EnumSet.noneOf(AccessFlags.class) : accessFlags;
	}
	
	public DexField(DexClass parent, EncodedField fieldInfo) {
		this(parent, 
		     StringIdItem.getStringValue(fieldInfo.field.getFieldName()),
		     null);
		
		AccessFlagSet.addAll(
			Arrays.asList(AccessFlags.getAccessFlagsForField(fieldInfo.accessFlags)));
	}
	
	public boolean isStatic() {
		return AccessFlagSet.contains(AccessFlags.STATIC);
	}
}
