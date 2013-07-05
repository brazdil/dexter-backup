package uk.ac.cam.db538.dexter.hierarchy;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.val;

import org.jf.dexlib.ClassDataItem;
import org.jf.dexlib.ClassDefItem;
import org.jf.dexlib.DexFile;
import org.jf.dexlib.DexFile.NoClassesDexException;
import org.jf.dexlib.Util.AccessFlags;

import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexFieldId;
import uk.ac.cam.db538.dexter.dex.type.DexMethodId;
import uk.ac.cam.db538.dexter.dex.type.DexTypeCache;
import uk.ac.cam.db538.dexter.utils.Pair;

public class HierarchyBuilder implements Serializable {

	private boolean foundRoot = false;
	private final DexTypeCache typeCache;
	
	private final Map<DexClassType, Pair<BaseClassDefinition, ClassData>> definedClasses;
	
	public HierarchyBuilder(DexTypeCache cache) {
		typeCache = cache;
		definedClasses = new HashMap<DexClassType, Pair<BaseClassDefinition, ClassData>>();
	}

	public void importDexFolder(File dir) throws IOException {
		String[] files = dir.list(FILTER_DEX_ODEX_JAR);
		
		for (String filename : files)
			importDex(new File(dir, filename), false);
	}
	
	public void importDex(File file, boolean isInternal) throws IOException {
		// parse the file
		DexFile dex;
		try {
			dex = new DexFile(file, false, true);
		} catch (NoClassesDexException e) {
			// file does not contain classes.dex
			return;
		}

		importDex(dex, isInternal);
		
		// explicitly dispose of the object
		dex = null;
		System.gc();
	}
	
	public void importDex(DexFile dex, boolean isInternal) {
		// recursively scan classes
		for (val cls : dex.ClassDefsSection.getItems())
			scanClass(cls, isInternal);
	}
	
	private void scanClass(ClassDefItem cls, boolean isInternal) {
		val clsData = cls.getClassData();
		val clsType = DexClassType.parse(cls.getClassType().getTypeDescriptor(), typeCache);
		
		// check that class has not been defined before
		if (definedClasses.containsKey(clsType))
			throw new HierarchyException("Multiple definition of class " + clsType.getPrettyName());
		
		// examine access flags
		int iFlags = cls.getAccessFlags();
		List<AccessFlags> listFlags = Arrays.asList(AccessFlags.getAccessFlagsForClass(cls.getAccessFlags()));
		
		// create class definition object instance
		BaseClassDefinition clsInfo;
		ClassData clsInfo_Data = new ClassData();
		if (listFlags.contains(AccessFlags.INTERFACE))
			clsInfo = new InterfaceDefinition(clsType, iFlags, isInternal);
		else {
			val classDef = new ClassDefinition(clsType, cls.getAccessFlags(), isInternal);
			scanInstanceFields(clsData, classDef);
			
			if (cls.getInterfaces() != null) {
				clsInfo_Data.interfaces = new HashSet<DexClassType>();
				for (val ifaceTypeItem : cls.getInterfaces().getTypes())
					clsInfo_Data.interfaces.add(DexClassType.parse(ifaceTypeItem.getTypeDescriptor(), typeCache));
			}

			clsInfo = classDef;
		}
		
		scanMethods(clsData, clsInfo);
		scanStaticFields(clsData, clsInfo);
		
		// acquire superclass info
		val superclsTypeItem = cls.getSuperclass();
		if (superclsTypeItem != null)
			clsInfo_Data.superclass = DexClassType.parse(superclsTypeItem.getTypeDescriptor(), typeCache);
		else {
			// check only one root exists
			if (foundRoot)
				throw new HierarchyException("More than one hierarchy root found");
			else if (isInternal)
				throw new HierarchyException("Hierarchy root cannot be internal");
			else
				foundRoot = true;
		}
		
		// store data
		definedClasses.put(clsType, new Pair<BaseClassDefinition, ClassData>(clsInfo, clsInfo_Data));
	}
	
	private void scanMethods(ClassDataItem clsData, BaseClassDefinition clsDef) {
		if (clsData == null)
			return;

		for (val methodItem : clsData.getDirectMethods())
			clsDef.addDeclaredMethod(
				new MethodDefinition(
					clsDef,
					DexMethodId.parseMethodId(methodItem.method, typeCache),
					methodItem.accessFlags));

		for (val methodItem : clsData.getVirtualMethods())
			clsDef.addDeclaredMethod(
				new MethodDefinition(
					clsDef,
					DexMethodId.parseMethodId(methodItem.method, typeCache),
					methodItem.accessFlags));
	}
	
	private void scanStaticFields(ClassDataItem clsData, BaseClassDefinition clsDef) {
		if (clsData == null)
			return;
		
		for (val fieldItem : clsData.getStaticFields())
			clsDef.addDeclaredStaticField(
				new StaticFieldDefinition(
					clsDef, 
					DexFieldId.parseFieldId(fieldItem.field, typeCache),
					fieldItem.accessFlags));
	}
	
	private void scanInstanceFields(ClassDataItem clsData, ClassDefinition clsDef) {
		if (clsData == null)
			return;

		for (val fieldItem : clsData.getInstanceFields())
			clsDef.addDeclaredInstanceField(
				new InstanceFieldDefinition(
					clsDef,
					DexFieldId.parseFieldId(fieldItem.field, typeCache),
					fieldItem.accessFlags));
	}
		
	public RuntimeHierarchy build() {
		val classList = new HashMap<DexClassType, BaseClassDefinition>();
		for (val classDefPair : definedClasses.values()) {
			val baseCls = classDefPair.getValA();
			val clsData = classDefPair.getValB();
			
			// connect to parent and vice versa
			val sclsType = clsData.superclass;
			if (sclsType != null) {
				val sclsInfo = definedClasses.get(sclsType);
				if (sclsInfo == null)
					throw new HierarchyException("Class " + baseCls.getClassType().getPrettyName() + " is missing its parent " + sclsType.getPrettyName());
				else
					baseCls.setSuperclassLink(sclsInfo.getValA());
			}

			// proper classes only (not interfaces)
			if (baseCls instanceof ClassDefinition) {
				val properCls = (ClassDefinition) baseCls;
				
				// connect to interfaces
				val ifaces = clsData.interfaces;
				if (ifaces != null) {
					for (val ifaceType : ifaces) {
						val ifaceInfo = definedClasses.get(ifaceType).getValA();
						if (ifaceInfo == null || !(ifaceInfo instanceof InterfaceDefinition))
							throw new HierarchyException("Class " + baseCls.getClassType().getPrettyName() + " is missing its interface " + ifaceType.getPrettyName());
						else
							properCls.addImplementedInterface((InterfaceDefinition) ifaceInfo);
					}
				}
			}
			
			classList.put(baseCls.getClassType(), baseCls);
		}
			
		return new RuntimeHierarchy(classList);
	}
	
	public void removeInternalClasses() {
		val classDefPairs = new ArrayList<Pair<BaseClassDefinition, ClassData>>(definedClasses.values());
		
		for (val defPair : classDefPairs) {
			val cls = defPair.getValA();
			if (cls.isInternal())
				definedClasses.remove(cls.getClassType());
		}
	}
	
	private static final FilenameFilter FILTER_DEX_ODEX_JAR = new FilenameFilter() {
		@Override
		public boolean accept(File dir, String name) {
			return name.endsWith(".dex") || name.endsWith(".odex") || name.endsWith(".jar"); 
		}
	};
	
	private static class ClassData implements Serializable {
		private static final long serialVersionUID = 1L;

		DexClassType superclass = null;
		Set<DexClassType> interfaces = null;
	}

	// SERIALIZATION
	
	private static final long serialVersionUID = 1L;

	public void serialize(File outputFile) throws IOException {
		typeCache.clear(); // can be removed
		
		val fos = new FileOutputStream(outputFile);
		try {
			val oos = new ObjectOutputStream(new BufferedOutputStream(fos));
			try {
				oos.writeObject(this);
			} finally {
				oos.close();
			}
		} finally {
			fos.close();
		}
	}
	
	public static HierarchyBuilder deserialize(File inputFile) throws IOException {
		val fis = new FileInputStream(inputFile);
		try {
			val ois = new ObjectInputStream(new BufferedInputStream(fis));
			try {
				Object hierarchy;
				try {
					hierarchy = ois.readObject();
				} catch (ClassNotFoundException ex) {
					throw new HierarchyException(ex);
				}
				
				if (hierarchy instanceof HierarchyBuilder)
					return (HierarchyBuilder) hierarchy;
				else
					throw new HierarchyException("Input file does not contain an instance of HierarchyBuilder");
			} finally {
				ois.close();
			}
		} finally {
			fis.close();
		}
	}
}
