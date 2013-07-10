package uk.ac.cam.db538.dexter.dex;

import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.AnnotationDirectoryItem;
import org.jf.dexlib.ClassDataItem.EncodedField;
import org.jf.dexlib.ClassDefItem;
import org.jf.dexlib.EncodedValue.EncodedValue;

import uk.ac.cam.db538.dexter.hierarchy.FieldDefinition;

import com.android.dx.dex.file.EncodedArrayItem;

public class DexStaticField extends DexField {

	@Getter private final EncodedValue initialValue; 

	public DexStaticField(DexClass parentClass, FieldDefinition fieldDef, Set<DexAnnotation> annotations, EncodedArrayItem staticInitializers, EncodedValue initialValue) {
		super(parentClass, fieldDef, annotations);
		this.initialValue = initialValue;
	}
	
	public DexStaticField(DexClass parentClass, FieldDefinition fieldDef, ClassDefItem classItem, EncodedField fieldItem, AnnotationDirectoryItem annoDir) {
		super(parentClass, fieldDef, fieldItem, annoDir);
		this.initialValue = init_ParseInitialValue(classItem, fieldItem);
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
}
