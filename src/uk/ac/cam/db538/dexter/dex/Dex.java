package uk.ac.cam.db538.dexter.dex;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.LinkedList;

import org.jf.dexlib.DexFile;

import uk.ac.cam.db538.dexter.dex.type.TypeCache;

import lombok.Getter;
import lombok.val;

public class Dex {

	@Getter private DexFile OriginalFile;
	
	@Getter	private List<DexClass> Classes;
	@Getter private TypeCache KnownTypes;
	
	public Dex(File filename) throws IOException {
		OriginalFile = new DexFile(filename);
		
		KnownTypes = new TypeCache();
		
		Classes = new LinkedList<DexClass>();
		val dexClsInfos = OriginalFile.ClassDefsSection.getItems();
		for (val dexClsInfo : dexClsInfos)
			Classes.add(new DexClass(this, dexClsInfo));
	}
	
}
