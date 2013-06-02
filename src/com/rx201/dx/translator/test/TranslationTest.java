package com.rx201.dx.translator.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import lombok.val;

import org.jf.dexlib.Code.Analysis.ClassPath;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.cam.db538.dexter.apk.Apk;

public class TranslationTest {

	private static File frameworkDir = new File("framework-2.3-lite");
	private static File testsDir = new File("cg_test/tests/");
	
	@BeforeClass 
	public static void onlyOnce() {
	    ClassPath.InitializeClassPath(new String[]{frameworkDir.getAbsolutePath()}, 
	    		frameworkDir.list(new FilenameFilter() {
					public boolean accept(File dir, String name) {
						return name.endsWith(".jar");
					}
	    		}), 
	    		new String[]{}, 
	    		"dexfile", 
	    		null, 
	    		false);
    }
	
	private void testEach(File file) throws IOException {
	    val APK = new Apk(file, frameworkDir);
	    APK.getDexFile().writeToFile();
	}
	
	@Test
	public void test() throws IOException {
	    
		for (String file : testsDir.list(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".apk");
			}
		})) {
			testEach(new File(testsDir.getAbsolutePath(), file ));
		}
	}
}
