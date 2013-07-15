package uk.ac.cam.db538.dexter.dex.field;

import org.jf.dexlib.AnnotationDirectoryItem;
import org.jf.dexlib.ClassDataItem.EncodedField;

import uk.ac.cam.db538.dexter.dex.DexClass;
import uk.ac.cam.db538.dexter.hierarchy.FieldDefinition;

public class DexInstanceField extends DexField {

	public DexInstanceField(DexClass parentClass, FieldDefinition fieldDef) {
		super(parentClass, fieldDef);
	}
	
	public DexInstanceField(DexClass parentClass, FieldDefinition fieldDef, EncodedField fieldItem, AnnotationDirectoryItem annoDir) {
		super(parentClass, fieldDef, fieldItem, annoDir);
	}
}
