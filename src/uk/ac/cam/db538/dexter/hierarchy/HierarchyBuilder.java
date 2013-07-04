package uk.ac.cam.db538.dexter.hierarchy;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import lombok.val;

import org.jf.dexlib.ClassDefItem;
import org.jf.dexlib.DexFile;
import org.jf.dexlib.DexFile.NoClassesDexException;

import uk.ac.cam.db538.dexter.dex.type.DexTypeCache;
import uk.ac.cam.db538.dexter.dex.type.DexType_Class;

public class HierarchyBuilder {

	private final DexTypeCache typeCache;
	private final Set<DexType_Class> definedClasses;
	
	public HierarchyBuilder(DexTypeCache cache) {
		typeCache = cache;
		definedClasses = new HashSet<DexType_Class>();
	}

	public void scanDexFolder(File dir, HierarchyScanCallback callback) throws IOException {
		String[] files = dir.list(FILTER_DEX_ODEX);
		
		if (callback != null) callback.onFolderScanStarted(dir, files.length);
		
		for (String filename : files)
			scanDex(new File(dir, filename), callback);
		
		if (callback != null) callback.onFolderScanFinished(dir, files.length);
	}
	
	public void scanDex(File file, HierarchyScanCallback callback) throws IOException {
		if (callback != null) callback.onFileScanStarted(file);
		
		// parse the file
		DexFile dex;
		try {
			dex = new DexFile(file, false, true);
		} catch (NoClassesDexException e) {
			// file does not contain classes.dex
			if (callback != null) callback.onFileScanFinished(file);
			return;
		}

		// recursively scan classes
		for (val cls : dex.ClassDefsSection.getItems())
			scanClass(cls);
		
		// explicitly dispose of the object
		dex = null;
		System.gc();

		if (callback != null) callback.onFileScanFinished(file);
	}
	
	private void scanClass(ClassDefItem cls) {
		val clsType = DexType_Class.parse(cls.getClassType().getTypeDescriptor(), typeCache);
		
		// add class to the list of defined classes
		if (definedClasses.contains(clsType))
			throw new HierarchyException("Multiple definition of class " + clsType.getPrettyName());
		definedClasses.add(clsType);
	}

	private static final FilenameFilter FILTER_DEX_ODEX = new FilenameFilter() {
		@Override
		public boolean accept(File dir, String name) {
			return name.endsWith(".dex") || name.endsWith(".odex"); 
		}
	};
}
