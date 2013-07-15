package uk.ac.cam.db538.dexter.dex.field;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.AnnotationDirectoryItem;
import org.jf.dexlib.ClassDataItem.EncodedField;
import org.jf.dexlib.ClassDefItem;
import org.jf.dexlib.EncodedValue.EncodedValue;

import uk.ac.cam.db538.dexter.dex.DexClass;
import uk.ac.cam.db538.dexter.dex.type.DexFieldId;
import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;
import uk.ac.cam.db538.dexter.hierarchy.FieldDefinition;
import uk.ac.cam.db538.dexter.hierarchy.StaticFieldDefinition;

public class DexStaticField extends DexField {

	@Getter private final StaticFieldDefinition fieldDef;
	@Getter private final EncodedValue initialValue; 

	public DexStaticField(DexClass parentClass, StaticFieldDefinition fieldDef, EncodedValue initialValue) {
		super(parentClass);
		this.initialValue = initialValue;
		this.fieldDef = fieldDef;
	}
	
	public DexStaticField(DexClass parentClass, ClassDefItem classItem, EncodedField fieldItem, AnnotationDirectoryItem annoDir) {
		super(parentClass, fieldItem, annoDir);
		
		this.initialValue = init_ParseInitialValue(classItem, fieldItem);
		this.fieldDef = init_FindFieldDefinition(parentClass, fieldItem);
	}
	
	private static StaticFieldDefinition init_FindFieldDefinition(DexClass parentClass, EncodedField fieldItem) {
		val hierarchy = parentClass.getParentFile().getHierarchy();
		val classDef = parentClass.getClassDef();
		
		val name = fieldItem.field.getFieldName().getStringValue();
		val type = DexRegisterType.parse(fieldItem.field.getFieldType().getTypeDescriptor(), hierarchy.getTypeCache()); 
		
		val fieldId = DexFieldId.parseFieldId(name, type, hierarchy.getTypeCache());
		return classDef.getStaticField(fieldId);
	}
	
	private static EncodedValue init_ParseInitialValue(ClassDefItem classItem, EncodedField fieldItem) {
		// extract data
		
		if (classItem.getClassData() == null)
			return null;
		
		val initValuesItem = classItem.getStaticFieldInitializers();
		val staticFields = classItem.getClassData().getStaticFields();
		
		if (initValuesItem == null || staticFields == null)
			return null;
		
		val initValues = initValuesItem.getEncodedArray().values;
		
		// find the field in the staticFields array
		
		val fieldIndex = staticFields.indexOf(fieldItem);
		if (fieldIndex < 0 || fieldIndex >= initValues.length)
			return null;
		
		// return the value
		
		return initValues[fieldIndex];
	}

	@Override
	protected FieldDefinition internal_GetFieldDef() {
		return this.fieldDef;
	}
}
