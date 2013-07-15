package uk.ac.cam.db538.dexter.hierarchy.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import lombok.val;

import org.jf.dexlib.ClassDataItem;
import org.jf.dexlib.ClassDefItem;
import org.jf.dexlib.Util.AccessFlags;

import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexTypeCache;

public class DexClassScanner {

	private final DexTypeCache typeCache;
	
	private final ClassDefItem classDefItem;
	private final ClassDataItem classDataItem;
	
	public DexClassScanner(ClassDefItem cls, DexTypeCache typeCache) {
		this.classDefItem = cls;
		this.classDataItem = this.classDefItem.getClassData();
		this.typeCache = typeCache;
	}

	public String getClassDescriptor() {
		return classDefItem.getClassType().getTypeDescriptor(); 
	}

	public boolean isInterface() {
		val flagsList = Arrays.asList(
			AccessFlags.getAccessFlagsForClass(getAccessFlags()));
		return flagsList.contains(AccessFlags.INTERFACE);
	}

	public int getAccessFlags() {
		return classDefItem.getAccessFlags();
	}

	public Collection<DexMethodScanner> getMethodScanners() {
		if (classDataItem == null)
			return Collections.emptyList();
		
		val allMethods = new ArrayList<DexMethodScanner>(classDataItem.getDirectMethodCount() + classDataItem.getVirtualMethodCount());
		for (val method : classDataItem.getDirectMethods())
			allMethods.add(new DexMethodScanner(method, typeCache));
		for (val method : classDataItem.getVirtualMethods())
			allMethods.add(new DexMethodScanner(method, typeCache));
		
		return allMethods;
	}

	public String getSuperclassDescriptor() {
		val typeItem = classDefItem.getSuperclass();
		if (typeItem == null)
			return null;
		else
			return typeItem.getTypeDescriptor();
	}

	public Collection<DexClassType> getInterfaces() {
		val interfaceDefs = classDefItem.getInterfaces();
		if (interfaceDefs == null)
			return Collections.emptyList();
		
		val allInterfaces = new ArrayList<DexClassType>(interfaceDefs.getTypeCount());
		
		for (val interfaceType : interfaceDefs.getTypes())
			allInterfaces.add(DexClassType.parse(interfaceType.getTypeDescriptor(), typeCache));
		
		return allInterfaces;
	}

	public Collection<DexFieldScanner> getStaticFieldScanners() {
		if (classDataItem == null)
			return Collections.emptyList();
		
		val allStaticFields = new ArrayList<DexFieldScanner>(classDataItem.getStaticFieldCount());
		for (val fieldItem : classDataItem.getStaticFields())
			allStaticFields.add(new DexFieldScanner(fieldItem, typeCache));
		
		return allStaticFields;
	}

	public Collection<DexFieldScanner> getInstanceFieldScanners() {
		if (classDataItem == null)
			return Collections.emptyList();
		
		val allInstanceFields = new ArrayList<DexFieldScanner>(classDataItem.getInstanceFieldCount());
		for (val fieldItem : classDataItem.getInstanceFields())
			allInstanceFields.add(new DexFieldScanner(fieldItem, typeCache));
		
		return allInstanceFields;
	}
}
