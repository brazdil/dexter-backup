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
import java.util.Map.Entry;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.DexFile;
import org.jf.dexlib.DexFile.NoClassesDexException;

import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexTypeCache;
import uk.ac.cam.db538.dexter.hierarchy.BaseClassDefinition;
import uk.ac.cam.db538.dexter.hierarchy.ClassDefinition;
import uk.ac.cam.db538.dexter.hierarchy.FieldDefinition;
import uk.ac.cam.db538.dexter.hierarchy.HierarchyException;
import uk.ac.cam.db538.dexter.hierarchy.InterfaceDefinition;
import uk.ac.cam.db538.dexter.hierarchy.MethodDefinition;
import uk.ac.cam.db538.dexter.hierarchy.RuntimeHierarchy;

public class HierarchyBuilder implements Serializable {

	private ClassDefinition root;
	@Getter private final DexTypeCache typeCache;
	
	private final Map<DexClassType, ClassVariants> definedClasses;
	
	public HierarchyBuilder() {
		root = null;
		typeCache = new DexTypeCache();
		definedClasses = new HashMap<DexClassType, ClassVariants>();
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
	
	private void scanClass(DexClassScanner clsScanner, boolean isInternal) {
		val clsType = DexClassType.parse(clsScanner.getClassDescriptor(), typeCache);
		
		val baseclsData = new ClassData();
		
		if (clsScanner.isInterface())
			baseclsData.classDef = new InterfaceDefinition(clsType, clsScanner.getAccessFlags(), isInternal);
		else {
			val clsDef = new ClassDefinition(clsType, clsScanner.getAccessFlags(), isInternal);
			baseclsData.classDef = clsDef;
			
			scanInstanceFields(clsScanner, clsDef);
			scanSuperclass(clsScanner, clsDef, baseclsData, isInternal);

			baseclsData.interfaces = clsScanner.getInterfaces();
		}
		
		scanMethods(clsScanner, baseclsData.classDef);
		scanStaticFields(clsScanner, baseclsData.classDef);
		
		// store data
		ClassVariants clsVariants = definedClasses.get(clsType);
		if (clsVariants == null) {
			clsVariants = new ClassVariants();
			definedClasses.put(clsType, clsVariants);
		}
		clsVariants.setVariant(baseclsData, isInternal);	
	}
	
	private void foundRoot(ClassDefinition clsInfo, boolean isInternal) {
		// check only one root exists
		if (root != null)
			throw new HierarchyException("More than one hierarchy root found (" + root.getType().getPrettyName() + " vs. " + clsInfo.getType().getPrettyName() + ")");
		else if (isInternal)
			throw new HierarchyException("Hierarchy root cannot be internal");
		else
			root = clsInfo;
	}
	
	private void scanMethods(DexClassScanner clsScanner, BaseClassDefinition baseclsDef) {
		for (val methodScanner : clsScanner.getMethodScanners()) {
			baseclsDef.addDeclaredMethod(
				new MethodDefinition(
					baseclsDef,
					methodScanner.getMethodId(),
					methodScanner.getAccessFlags()));
		}
	}
	
	private void scanStaticFields(DexClassScanner clsScanner, BaseClassDefinition baseclsDef) {
		for (val fieldScanner : clsScanner.getStaticFieldScanners())
			baseclsDef.addDeclaredStaticField(
				new FieldDefinition(
					baseclsDef, 
					fieldScanner.getFieldId(),
					fieldScanner.getAccessFlags()));
	}
	
	private void scanInstanceFields(DexClassScanner clsScanner, ClassDefinition clsDef) {
		for (val fieldScanner : clsScanner.getInstanceFieldScanners())
			clsDef.addDeclaredInstanceField(
				new FieldDefinition(
					clsDef, 
					fieldScanner.getFieldId(),
					fieldScanner.getAccessFlags()));
	}
	
	private void scanSuperclass(DexClassScanner clsScanner, ClassDefinition clsDef, ClassData baseclsData, boolean isInternal) {
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
			val clsData = classDefPair.getClassData();
			val baseCls = clsData.classDef;
			
			// connect to parent and vice versa
			val sclsType = (baseCls instanceof ClassDefinition) ? clsData.superclass : root.getType();
			if (sclsType != null) {
				val sclsVariants = definedClasses.get(sclsType);
				if (sclsVariants == null)
					throw new HierarchyException("Class " + baseCls.getType().getPrettyName() + " is missing its parent " + sclsType.getPrettyName());
				else
					baseCls.setSuperclassLink(sclsVariants.getClassData().classDef);
			}

			// proper classes only (not interfaces)
			if (baseCls instanceof ClassDefinition) {
				val properCls = (ClassDefinition) baseCls;
				
				// connect to interfaces
				val ifaces = clsData.interfaces;
				if (ifaces != null) {
					for (val ifaceType : ifaces) {
						val ifaceInfo_Pair = definedClasses.get(ifaceType);
						if (ifaceInfo_Pair == null || !(ifaceInfo_Pair.getClassData().classDef instanceof InterfaceDefinition))
							throw new HierarchyException("Class " + baseCls.getType().getPrettyName() + " is missing its interface " + ifaceType.getPrettyName());
						else
							properCls.addImplementedInterface((InterfaceDefinition) ifaceInfo_Pair.getClassData().classDef);
					}
				}
			}
			
			classList.put(baseCls.getType(), baseCls);
		}
		
		if (root == null)
			throw new HierarchyException("Hierarchy is missing a root");
			
		return new RuntimeHierarchy(classList, root, typeCache);
	}
	
	public void removeInternalClasses() {
		val classEntries = new ArrayList<Entry<DexClassType, ClassVariants>>(definedClasses.entrySet());
		
		for (val classEntry : classEntries) {
			val classPair = classEntry.getValue();
			classPair.deleteInternal();
			if (classPair.isEmpty())
				definedClasses.remove(classEntry.getKey());
		}
	}
	
	private static final FilenameFilter FILTER_DEX_ODEX_JAR = new FilenameFilter() {
		@Override
		public boolean accept(File dir, String name) {
			return name.endsWith(".dex") || name.endsWith(".odex") || name.endsWith(".jar"); 
		}
	};
	
	private static class ClassVariants implements Serializable {
		private static final long serialVersionUID = 1L;
		
		private ClassData internal;
		private ClassData external;
		
		public ClassVariants() {
			this.internal = this.external = null;
		}
		
		public ClassData getClassData() {
			// prefer internal
			if (internal != null)
				return internal;
			else if (external != null)
				return external;
			else
				throw new HierarchyException("No class data available");
		}
		
		public void setVariant(ClassData cls, boolean isInternal) {
			if (isInternal) {
				if (this.internal != null)
					throw new HierarchyException("Multiple definitions of internal class " + this.internal.classDef.getType().getPrettyName());
				
				this.internal = cls;
			} else {
				if (this.external != null)
					throw new HierarchyException("Multiple definitions of external class " + this.external.classDef.getType().getPrettyName());
				
				this.external = cls;
			}
		}

		public void deleteInternal() {
			internal = null;
		}
		
		public boolean isEmpty() {
			return (external == null) && (internal == null); 
		}
	}
	
	private static class ClassData implements Serializable {
		private static final long serialVersionUID = 1L;
		
		BaseClassDefinition classDef = null;

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

	public RuntimeHierarchy buildAgainstApp(DexFile dex) {
		try {
			importDex(dex, true);
			return build();
		} finally {
			removeInternalClasses();
		}
	}
}
