package uk.ac.cam.db538.dexter.dex.type;

import java.io.Serializable;

import lombok.Getter;
import lombok.val;

public class DexMethodId implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Getter private final String name;
	@Getter private final DexPrototype prototype;
	
	private final int hashcode;
	
	public DexMethodId(String name, DexPrototype prototype) {
		this.name = name;
		this.prototype = prototype;
		
		// precompute hashcode
		int result = 31 + name.hashCode();
		result = 31 * result + prototype.hashCode();
		this.hashcode = result;
	}

	public static DexMethodId parseMethodId(String methodName, DexPrototype methodPrototype, DexTypeCache cache) {
		val mid = new DexMethodId(methodName, methodPrototype);
		return cache.getCachedMethodId(mid); // will return 'mid' if not cached yet
	}

	@Override
	public int hashCode() {
		return hashcode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		
		if (!(obj instanceof DexMethodId))
			return false;
		DexMethodId other = (DexMethodId) obj;

		return
			this.name.hashCode() == other.name.hashCode() &&
			this.prototype.hashCode() == other.prototype.hashCode() &&
			this.name.equals(other.name) &&
			this.prototype.equals(other.prototype);
	}
}