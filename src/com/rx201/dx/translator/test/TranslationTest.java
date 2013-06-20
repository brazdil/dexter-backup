package com.rx201.dx.translator.test;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import lombok.val;

import org.jf.dexlib.Code.Analysis.ClassPath;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import uk.ac.cam.db538.dexter.apk.Apk;

@RunWith(Parameterized.class)
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
	
	@Parameters(name = "{1}")
	public static Collection<Object[]> data() {
		ArrayList<Object[]> tests = new ArrayList<Object[]>();
		
		for (String file : testsDir.list(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".apk");
			}
		})) {
			tests.add(new Object[] {new File(testsDir.getAbsolutePath(), file ), file});
		}
		Collections.sort(tests, new Comparator<Object[]>() {

			@Override
			public int compare(Object[] arg0, Object[] arg1) {
				File f0 = (File)arg0[0];
				File f1 = (File)arg1[0];
				return f0.getName().compareTo(f1.getName());
			}
		});
		return tests;
	}
	
	private File file;
	
	public TranslationTest(File file, String filename) {
		this.file = file;
	}
	
	@Test
	public void test() throws IOException{
	    val APK = new Apk(file, frameworkDir);
	    APK.getDexFile().instrument(false);
	    APK.getDexFile().writeToFile();
	}
	
}
