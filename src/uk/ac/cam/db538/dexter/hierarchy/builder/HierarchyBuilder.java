package uk.ac.cam.db538.dexter.hierarchy.builder;

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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.DexFile;
import org.jf.dexlib.DexFile.NoClassesDexException;

import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexTypeCache;
import uk.ac.cam.db538.dexter.hierarchy.BaseClassDefinition;
import uk.ac.cam.db538.dexter.hierarchy.ClassDefinition;
import uk.ac.cam.db538.dexter.hierarchy.HierarchyException;
import uk.ac.cam.db538.dexter.hierarchy.InstanceFieldDefinition;
import uk.ac.cam.db538.dexter.hierarchy.InterfaceDefinition;
import uk.ac.cam.db538.dexter.hierarchy.MethodDefinition;
import uk.ac.cam.db538.dexter.hierarchy.RuntimeHierarchy;
import uk.ac.cam.db538.dexter.hierarchy.StaticFieldDefinition;
import uk.ac.cam.db538.dexter.utils.Pair;

public class HierarchyBuilder implements Serializable {

	private ClassDefinition root;
	@Getter private final DexTypeCache typeCache;
	
	private final Map<DexClassType, Pair<BaseClassDefinition, ClassData>> definedClasses;
	
	public HierarchyBuilder() {
		root = null;
		typeCache = new DexTypeCache();
		definedClasses = new HashMap<DexClassType, Pair<BaseClassDefinition, ClassData>>();
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
			scanClass(new DexClassScanner(cls, typeCache), isInternal);
	}
	
	private void importDependencies() {
		boolean somethingChanged;
		do {
			somethingChanged = false;
			
			// need to make a copy of the class list
			// because we will modify it inside the loop
			val currentClassSet = new ArrayList<Pair<BaseClassDefinition, ClassData>>(definedClasses.values());
			
			for (val clsPair : currentClassSet) {
				val clsData = clsPair.getValB();
				if (clsData == null)
					continue;
				
				// check superclass presence
				val supercls = clsData.superclass;
				if (supercls != null && !definedClasses.containsKey(supercls)) {
					needVMClass(supercls);
					somethingChanged = true;
				}
				
				// check presence of all interfaces
				val interfaces = clsData.interfaces;
				if (interfaces != null)
					for (val ifacecls : interfaces)
						if (!definedClasses.containsKey(ifacecls)) {
							needVMClass(ifacecls);
							somethingChanged = true;
						}
			}
		} while (somethingChanged);
	}
	
	@SuppressWarnings("rawtypes")
	private void needVMClass(DexClassType clsType) {
		// try to find it in the running VM
		Class vmClass;
		try {
			vmClass = Class.forName(clsType.getJavaDescriptor());
		} catch (ClassNotFoundException e) {
			throw new HierarchyException("Class " + clsType.getPrettyName() + " not present in VM");
		}
		
		// import it
		scanClass(new VmClassScanner(vmClass, typeCache), false);
	}
	
	private void scanClass(IClassScanner clsScanner, boolean isInternal) {
		val clsType = checkClassType(clsScanner);
		
		BaseClassDefinition baseclsDef;
		ClassData baseclsData = null;
		
		if (clsScanner.isInterface())
			baseclsDef = new InterfaceDefinition(clsType, clsScanner.getAccessFlags(), isInternal);
		else {
			val clsDef = new ClassDefinition(clsType, clsScanner.getAccessFlags(), isInternal);
			
			baseclsDef = clsDef;
			baseclsData = new ClassData();
			
			scanInstanceFields(clsScanner, clsDef);
			scanSuperclass(clsScanner, clsDef, baseclsData, isInternal);

			baseclsData.interfaces = clsScanner.getInterfaces();
		}
		
		scanMethods(clsScanner, baseclsDef);
		scanStaticFields(clsScanner, baseclsDef);
		
		// store data
		definedClasses.put(clsType, new Pair<BaseClassDefinition, ClassData>(baseclsDef, baseclsData));
	}
	
	private DexClassType checkClassType(IClassScanner clsScanner) {
		val dalvikDescriptor = clsScanner.getClassDescriptor();
		val clsType = DexClassType.parse(dalvikDescriptor, typeCache);
		
		// check that class has not been defined before
		if (definedClasses.containsKey(clsType))
			throw new HierarchyException("Multiple definition of class " + clsType.getPrettyName());
		else
			return clsType;
	}
	
	private void foundRoot(ClassDefinition clsInfo, boolean isInternal) {
		// check only one root exists
		if (root != null)
			throw new HierarchyException("More than one hierarchy root found (" + root.getClassType().getPrettyName() + " vs. " + clsInfo.getClassType().getPrettyName() + ")");
		else if (isInternal)
			throw new HierarchyException("Hierarchy root cannot be internal");
		else
			root = clsInfo;
	}
	
	private void scanMethods(IClassScanner clsScanner, BaseClassDefinition baseclsDef) {
		for (val methodScanner : clsScanner.getMethodScanners()) {
			baseclsDef.addDeclaredMethod(
				new MethodDefinition(
					baseclsDef,
					methodScanner.getMethodId(),
					methodScanner.getAccessFlags()));
		}
	}
	
	private void scanStaticFields(IClassScanner clsScanner, BaseClassDefinition baseclsDef) {
		for (val fieldScanner : clsScanner.getStaticFieldScanners())
			baseclsDef.addDeclaredStaticField(
				new StaticFieldDefinition(
					baseclsDef, 
					fieldScanner.getFieldId(),
					fieldScanner.getAccessFlags()));
	}
	
	private void scanInstanceFields(IClassScanner clsScanner, ClassDefinition clsDef) {
		for (val fieldScanner : clsScanner.getInstanceFieldScanners())
			clsDef.addDeclaredInstanceField(
				new InstanceFieldDefinition(
					clsDef, 
					fieldScanner.getFieldId(),
					fieldScanner.getAccessFlags()));
	}
	
	private void scanSuperclass(IClassScanner clsScanner, ClassDefinition clsDef, ClassData baseclsData, boolean isInternal) {
		// acquire superclass info
		val typeDescriptor = clsScanner.getSuperclassDescriptor();
		if (typeDescriptor != null)
			baseclsData.superclass = DexClassType.parse(typeDescriptor, typeCache);
		else
			foundRoot(clsDef, isInternal);
	}
	
	public RuntimeHierarchy build() {
		val classList = new HashMap<DexClassType, BaseClassDefinition>();
		for (val classDefPair : definedClasses.values()) {
			val baseCls = classDefPair.getValA();
			val clsData = classDefPair.getValB();
			
			// connect to parent and vice versa
			val sclsType = (baseCls instanceof ClassDefinition) ? clsData.superclass : root.getClassType();
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
						val ifaceInfo_Pair = definedClasses.get(ifaceType);
						if (ifaceInfo_Pair == null || !(ifaceInfo_Pair.getValA() instanceof InterfaceDefinition))
							throw new HierarchyException("Class " + baseCls.getClassType().getPrettyName() + " is missing its interface " + ifaceType.getPrettyName());
						else
							properCls.addImplementedInterface((InterfaceDefinition) ifaceInfo_Pair.getValA());
					}
				}
			}
			
			classList.put(baseCls.getClassType(), baseCls);
		}
		
		if (root == null)
			throw new HierarchyException("Hierarchy is missing a root");
			
		return new RuntimeHierarchy(classList, root, typeCache);
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
		Collection<DexClassType> interfaces = null;
	}

	// SERIALIZATION
	
	private static final long serialVersionUID = 1L;

	public void serialize(File outputFile) throws IOException {
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
	
	// USEFUL SHORTCUTS
	
	public void importFrameworkFolder(File dir) throws IOException {
		String[] files = dir.list(FILTER_DEX_ODEX_JAR);
		
		for (String filename : files)
			importDex(new File(dir, filename), false);
	}

	public RuntimeHierarchy buildAgainstApp(DexFile dex, boolean fillDependencies) {
		try {
			importDex(dex, true);
			if (fillDependencies)
				importDependencies();
			return build();
		} finally {
			removeInternalClasses();
		}
	}
}
