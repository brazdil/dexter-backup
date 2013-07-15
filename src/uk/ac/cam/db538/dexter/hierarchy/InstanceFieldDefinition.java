package uk.ac.cam.db538.dexter.hierarchy;

import uk.ac.cam.db538.dexter.dex.type.DexFieldId;

public class InstanceFieldDefinition extends FieldDefinition {

	private static final long serialVersionUID = 1L;

	public InstanceFieldDefinition(BaseClassDefinition cls, DexFieldId fieldId, int accessFlags) {
		super(cls, fieldId, accessFlags);
	}
}
