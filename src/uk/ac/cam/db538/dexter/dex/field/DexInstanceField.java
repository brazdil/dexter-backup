package uk.ac.cam.db538.dexter.dex.field;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.AnnotationDirectoryItem;
import org.jf.dexlib.ClassDataItem.EncodedField;

import uk.ac.cam.db538.dexter.dex.DexClass;
import uk.ac.cam.db538.dexter.dex.type.DexFieldId;
import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;
import uk.ac.cam.db538.dexter.hierarchy.ClassDefinition;
import uk.ac.cam.db538.dexter.hierarchy.FieldDefinition;
import uk.ac.cam.db538.dexter.hierarchy.InstanceFieldDefinition;

public class DexInstanceField extends DexField {

	@Getter private final InstanceFieldDefinition fieldDef;
	
	public DexInstanceField(DexClass parentClass, InstanceFieldDefinition fieldDef) {
		super(parentClass);
		this.fieldDef = fieldDef;
	}
	
	public DexInstanceField(DexClass parentClass, EncodedField fieldItem, AnnotationDirectoryItem annoDir) {
		super(parentClass, fieldItem, annoDir);
		this.fieldDef = init_FindFieldDefinition(parentClass, fieldItem);
	}

	private static InstanceFieldDefinition init_FindFieldDefinition(DexClass parentClass, EncodedField fieldItem) {
		val hierarchy = parentClass.getParentFile().getHierarchy();

		val classDef_Temp = parentClass.getClassDef();
		if (!(classDef_Temp instanceof ClassDefinition))
			throw new Error("Interfaces can't have instance fields");
		val classDef = (ClassDefinition) classDef_Temp;
		
		val name = fieldItem.field.getFieldName().getStringValue();
		val type = DexRegisterType.parse(fieldItem.field.getFieldType().getTypeDescriptor(), hierarchy.getTypeCache()); 
		
		val fieldId = DexFieldId.parseFieldId(name, type, hierarchy.getTypeCache());
		return classDef.getInstanceField(fieldId);
	}

	@Override
	protected FieldDefinition internal_GetFieldDef() {
		return this.fieldDef;
	}
}
