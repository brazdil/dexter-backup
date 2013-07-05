package uk.ac.cam.db538.dexter.dex.type;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.FieldIdItem;

public class DexFieldId {
	
	@Getter private final String name;
	@Getter private final DexRegisterType type;
	
	private final int hashcode;
	
	public DexFieldId(String name, DexRegisterType type) {
		this.name = name;
		this.type = type;
		
		// precompute hashcode
		int result = 31 + name.hashCode();
		result = 31 * result + type.hashCode();
		this.hashcode = result;
	}

	public static DexFieldId parseFieldId(FieldIdItem item, DexTypeCache cache) {
		val fid = new DexFieldId(item.getFieldName().getStringValue(), 
                DexRegisterType.parse(item.getFieldType().getTypeDescriptor(), cache));

		return cache.getCachedFieldId(fid); // will return 'fid' if not cached yet
	}

	@Override
	public int hashCode() {
		return hashcode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		
		if (!(obj instanceof DexFieldId))
			return false;
		DexFieldId other = (DexFieldId) obj;

		return
			this.name.hashCode() == other.name.hashCode() &&
			this.type.hashCode() == other.type.hashCode() &&
			this.name.equals(other.name) &&
			this.type.equals(other.type);
	}
}