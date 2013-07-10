package uk.ac.cam.db538.dexter.hierarchy.builder;

import org.jf.dexlib.ClassDataItem.EncodedField;

import uk.ac.cam.db538.dexter.dex.type.DexFieldId;
import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;
import uk.ac.cam.db538.dexter.dex.type.DexTypeCache;

public class DexFieldScanner {

	private final DexTypeCache typeCache;
	private final EncodedField fieldItem;
	
	public DexFieldScanner(EncodedField fieldItem, DexTypeCache typeCache) {
		this.fieldItem = fieldItem;
		this.typeCache = typeCache;
	}

	public DexFieldId getFieldId() {
		return DexFieldId.parseFieldId(
			fieldItem.field.getFieldName().getStringValue(), 
            DexRegisterType.parse(fieldItem.field.getFieldType().getTypeDescriptor(), typeCache),
            typeCache);
	}

	public int getAccessFlags() {
		return fieldItem.accessFlags;
	}
}
