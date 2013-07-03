package uk.ac.cam.db538.dexter.dex.type;

import java.util.ArrayList;
import java.util.List;

import lombok.val;

import org.jf.dexlib.DexFile;
import org.jf.dexlib.TypeIdItem;
import org.jf.dexlib.TypeListItem;

import uk.ac.cam.db538.dexter.dex.DexAssemblingCache;
import uk.ac.cam.db538.dexter.utils.Cache;

public abstract class DexType {

protected DexType() { }

	public abstract String getDescriptor();
	public abstract String getPrettyName();
  
	public static DexType parse(String typeDescriptor, DexTypeCache cache) throws UnknownTypeException {
		val res = DexType_Void.parse(typeDescriptor);
	    if (res != null)
	    	return res;
	    else
	    	return DexType_Register.parse(typeDescriptor, cache);
	}

	public static Cache<DexType, TypeIdItem> createAssemblingCache(final DexAssemblingCache cache, final DexFile outFile) {
		return new Cache<DexType, TypeIdItem>() {
			@Override
			protected TypeIdItem createNewEntry(DexType type) {
				return TypeIdItem.internTypeIdItem(outFile,
						cache.getStringConstant(type.getDescriptor()));
			}
		};
	}
	  
	public static Cache<List<DexType_Register>, TypeListItem> createAssemblingCacheForLists(final DexAssemblingCache cache, final DexFile outFile) {
		return new Cache<List<DexType_Register>, TypeListItem>() {
			@Override
			protected TypeListItem createNewEntry(List<DexType_Register> typeList) {
				val dexTypeList = new ArrayList<TypeIdItem>(typeList.size());
				for (val type : typeList)
					dexTypeList.add(cache.getType(type));
	  
				return TypeListItem.internTypeListItem(outFile, dexTypeList);
			}
		};
	}
  
	// HASHCODE and EQUALS defined through descriptor only

	@Override
	public int hashCode() {
		return getDescriptor().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DexType other = (DexType) obj;
		return this.getDescriptor().equals(other.getDescriptor());
	}
}
