package uk.ac.cam.db538.dexter.hierarchy;

import uk.ac.cam.db538.dexter.dex.type.DexFieldId;

public class StaticFieldDefinition extends FieldDefinition {

	private static final long serialVersionUID = 1L;

	public StaticFieldDefinition(BaseClassDefinition cls, DexFieldId fieldId, int accessFlags) {
		super(cls, fieldId, accessFlags);
	}
}
