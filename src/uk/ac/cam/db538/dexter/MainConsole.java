package uk.ac.cam.db538.dexter;

import uk.ac.cam.db538.dexter.apk.Apk;
import uk.ac.cam.db538.dexter.dex.Dex;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.jar.JarFile;

import org.jf.dexlib.Code.Analysis.ClassPath;

import lombok.val;

public class MainConsole {
  public static void main(String[] args) throws IOException {
    if (args.length != 2) {
      System.err.println("usage: dexter <framework-dir> <apk-file>");
      System.exit(1);
    }

    val apkFile = new File(args[1]);
    if (!apkFile.isFile()) {
      System.err.println("<apk-file> is not a file");
      System.exit(1);
    }
    val apkFile_new = new File(apkFile.getAbsolutePath() + "_new.apk");

    val frameworkDir = new File(args[0]);
    if (!frameworkDir.isDirectory()) {
      System.err.println("<framework-dir> is not a directory");
      System.exit(1);
    }
    
    val APK = new Apk(apkFile, frameworkDir);

    val fullFrameworkDir = new File("framework-2.3");
    ClassPath.InitializeClassPath(new String[]{fullFrameworkDir.getAbsolutePath()}, 
    		fullFrameworkDir.list(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.endsWith(".jar");
				}
    		}), 
    		new String[]{}, 
    		"dexfile", 
    		null, 
    		false);
    
    //val warnings = APK.getDexFile().instrument(false);
    //for (val warning : warnings)
    //  System.err.println("warning: " + warning.getMessage());
    // APK.getDexFile().transformSSA();
    APK.writeToFile(apkFile_new);
    APK.getDexFile().countInstructions();
  }
}
