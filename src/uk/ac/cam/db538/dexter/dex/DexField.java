package uk.ac.cam.db538.dexter.dex;

import org.jf.dexlib.ClassDataItem.EncodedField;
import org.jf.dexlib.StringIdItem;

import lombok.Getter;

public class DexField {
	
	@Getter private final DexClass ParentClass;
	@Getter private final String Name;
	@Getter private final boolean Static;
	
	public DexField(DexClass parent, String name, boolean isStatic) {
		ParentClass = parent;
		Name = name;
		Static = isStatic;
	}
	
	public DexField(DexClass parent, EncodedField fieldInfo) {
		this(parent, 
		     StringIdItem.getStringValue(fieldInfo.field.getFieldName()),
		     fieldInfo.isStatic());
	}
}
