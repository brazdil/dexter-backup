package uk.ac.cam.db538.dexter.dex;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.LinkedList;

import org.jf.dexlib.DexFile;

import lombok.Getter;
import lombok.val;

public class Dex {

	@Getter
	private DexFile OriginalFile;
	
	@Getter
	private List<DexClass> Classes;
	
	public Dex(File filename) throws IOException {
		OriginalFile = new DexFile(filename);
		Classes = getClasses(OriginalFile);
	}
	
	private static List<DexClass> getClasses(DexFile file) {
		val classes = new LinkedList<DexClass>();
		
		val dexClsInfos = file.ClassDefsSection.getItems();
		for (val dexClsInfo : dexClsInfos)
			classes.add(new DexClass(dexClsInfo));
		
		return classes;
	}
	
}
