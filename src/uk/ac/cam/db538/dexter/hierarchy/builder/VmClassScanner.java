package uk.ac.cam.db538.dexter.hierarchy.builder;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import lombok.val;

import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexTypeCache;

@SuppressWarnings("rawtypes")
public class VmClassScanner implements IClassScanner {

	private final DexTypeCache typeCache;
	private final Class classDef;
	
	public VmClassScanner(Class classDef, DexTypeCache typeCache) {
		this.typeCache = typeCache;
		this.classDef = classDef;
	}

	@Override
	public String getClassDescriptor() {
		return DexClassType.jvm2dalvik(classDef.getName());
	}

	@Override
	public String getSuperclassDescriptor() {
		val scls = classDef.getSuperclass();
		if (scls != null)
			return DexClassType.jvm2dalvik(scls.getName());
		else
			return null;
	}

	@Override
	public boolean isInterface() {
		return classDef.isInterface();
	}

	@Override
	public int getAccessFlags() {
		// TODO: does this map correctly?
		return classDef.getModifiers();
	}

	@Override
	public Collection<DexClassType> getInterfaces() {
		val interfaceClasses = classDef.getInterfaces();
		
		if (interfaceClasses.length == 0)
			return Collections.emptyList();
		
		val allInterfaces = new ArrayList<DexClassType>(interfaceClasses.length);
		for (val interfaceClass : interfaceClasses)
			allInterfaces.add(
				DexClassType.parse(DexClassType.jvm2dalvik(interfaceClass.getName()), typeCache));
		
		return allInterfaces;
	}

	@Override
	public Collection<IMethodScanner> getMethodScanners() {
		val methodClasses = classDef.getDeclaredMethods();
		
		val allMethods = new ArrayList<IMethodScanner>(methodClasses.length);
		for (val methodClass : methodClasses)
			allMethods.add(new VmMethodScanner(methodClass, typeCache));
		
		return allMethods;
	}

	@Override
	public Collection<IFieldScanner> getStaticFieldScanners() {
		val fields = classDef.getDeclaredFields();
		
		val allStaticFields = new ArrayList<IFieldScanner>(fields.length);
		for (val field : fields)
			if (Modifier.isStatic(field.getModifiers()))
				allStaticFields.add(new VmFieldScanner(field, typeCache));
		
		return allStaticFields;
	}

	@Override
	public Collection<IFieldScanner> getInstanceFieldScanners() {
		val fields = classDef.getDeclaredFields();
		
		val allInstanceFields = new ArrayList<IFieldScanner>(fields.length);
		for (val field : fields)
			if (!Modifier.isStatic(field.getModifiers()))
				allInstanceFields.add(new VmFieldScanner(field, typeCache));
		
		return allInstanceFields;
	}
}
