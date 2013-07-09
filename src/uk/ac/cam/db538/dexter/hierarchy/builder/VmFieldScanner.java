package uk.ac.cam.db538.dexter.hierarchy.builder;

import java.lang.reflect.Field;

import lombok.val;

import org.jf.dexlib.Util.AccessFlags;

import uk.ac.cam.db538.dexter.dex.DexUtils;
import uk.ac.cam.db538.dexter.dex.type.DexFieldId;
import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;
import uk.ac.cam.db538.dexter.dex.type.DexTypeCache;

public class VmFieldScanner implements IFieldScanner {

	private final DexTypeCache typeCache;
	private final Field fieldDef;
	
	public VmFieldScanner(Field fieldDef, DexTypeCache typeCache) {
		this.typeCache = typeCache;
		this.fieldDef = fieldDef;
	}

	@Override
	public DexFieldId getFieldId() {
		return DexFieldId.parseFieldId(
			fieldDef.getName(),
			DexRegisterType.parse(DexRegisterType.jvm2dalvik(fieldDef.getType().getName()), typeCache), 
			typeCache);				
	}

	@Override
	public int getAccessFlags() {
		val flags = VmClassScanner.convertModifier(fieldDef.getModifiers());
		
		if (fieldDef.isSynthetic())
			flags.add(AccessFlags.SYNTHETIC);

		return DexUtils.assembleAccessFlags(flags);
	}
}
