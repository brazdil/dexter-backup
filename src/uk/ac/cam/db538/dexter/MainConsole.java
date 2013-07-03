package uk.ac.cam.db538.dexter;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;

import lombok.val;

import org.jf.dexlib.DexFile;
import org.jf.dexlib.Code.Analysis.ClassPath;
import org.jf.dexlib.Util.ByteArrayAnnotatedOutput;

import uk.ac.cam.db538.dexter.apk.Apk;

public class MainConsole {
	private static void dumpAnnotation(File apkFile) {
	    val out = new ByteArrayAnnotatedOutput();
	    out.enableAnnotations(80, true);
	    
	    DexFile outFile;
		try {
			outFile = new DexFile(apkFile);
		    outFile.place();
		    outFile.writeTo(out);
			out.writeAnnotationsTo(new FileWriter("annot_original.txt"));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	    
//  private static void writeToJar(Apk apk, File targetFile) {
//		final byte[] newDex = apk.getDexFile().writeToFile();
//
//		System.out.println("Creating JAR");
//		try {
//			targetFile.delete();
//			ZipFile jarFile = new ZipFile(targetFile);
//			
//			ZipParameters parameters = new ZipParameters();
//			parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
//			parameters.setFileNameInZip("classes.dex");
//			parameters.setSourceExternalStream(true);
//
//			jarFile.addStream(new ByteArrayInputStream(newDex), parameters);
//		} catch (ZipException e) {
//		}
//  }
  
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
    dumpAnnotation(apkFile);
    
    val apkFile_new = new File(apkFile.getAbsolutePath() + "_new.apk");

    val frameworkDir = new File(args[0]);
    if (!frameworkDir.isDirectory()) {
      System.err.println("<framework-dir> is not a directory");
      System.exit(1);
    }
    
    val APK = new Apk(apkFile, frameworkDir);

    ClassPath.InitializeClassPath(new String[]{frameworkDir.getAbsolutePath()}, 
    		frameworkDir.list(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.endsWith(".jar");
				}
    		}), 
    		new String[]{}, 
    		"dexfile", 
    		new DexFile(apkFile), 
    		false);
    
//    writeToJar(APK, apkFile_new);

//    APK.getDexFile().countInstructions();
    val warnings = APK.getDexFile().instrument(false);
    for (val warning : warnings)
      System.err.println("warning: " + warning.getMessage());
//    APK.getDexFile().countInstructions();
    APK.writeToFile(apkFile_new);
//    APK.getDexFile().countInstructions();

  }
}
