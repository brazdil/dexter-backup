package uk.ac.cam.db538.dexter;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import lombok.val;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import org.jf.dexlib.DexFile;
import org.jf.dexlib.DexFileFromMemory;
import org.jf.dexlib.Util.ByteArrayAnnotatedOutput;

import uk.ac.cam.db538.dexter.dex.AuxiliaryDex;
import uk.ac.cam.db538.dexter.dex.Dex;
import uk.ac.cam.db538.dexter.hierarchy.builder.HierarchyBuilder;

import com.rx201.dx.translator.DexCodeGeneration;

public class MainTest {

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

  private static void writeToJar(Dex dex, File targetFile) {
     final byte[] newDex = dex.writeToFile();

     System.out.println("Creating JAR");
     try {
	     targetFile.delete();
	     ZipFile jarFile = new ZipFile(targetFile);
	     
	     ZipParameters parameters = new ZipParameters();
	     parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
	     parameters.setFileNameInZip("classes.dex");
	     parameters.setSourceExternalStream(true);
	
	     jarFile.addStream(new ByteArrayInputStream(newDex), parameters);
     } catch (ZipException e) {
     }
  }
	
  public static void main(String[] args) throws IOException {
	long epoch = System.currentTimeMillis();
    if (args.length != 2 && args.length != 3) {
      System.err.println("usage: dexter <framework-dir> <apk-file> [<destination-apk]>");
      System.exit(1);
    }

    val apkFile = new File(args[1]);
    if (!apkFile.isFile()) {
      System.err.println("<apk-file> is not a file");
      System.exit(1);
    }

    File apkFile_new;
    if (args.length == 3)
       apkFile_new = new File(args[2]);
    else
       apkFile_new = new File(apkFile.getAbsolutePath() + "_new.apk");
    
    // dumpAnnotation(apkFile);
    
    // val apkFile_new = new File(apkFile.getAbsolutePath() + "_new.apk");

    val frameworkDir = new File(args[0]);
    if (!frameworkDir.isDirectory()) {
      System.err.println("<framework-dir> is not a directory");
      System.exit(1);
    }
    
    // build runtime class hierarchy
    val hierarchyBuilder = new HierarchyBuilder();
    
    System.out.println("Scanning framework");
    hierarchyBuilder.importFrameworkFolder(frameworkDir);
    long hierarchyTime = System.currentTimeMillis() - epoch;
    
    System.out.println("Scanning application");
    val fileApp = new DexFile(apkFile);
    val fileAux = new DexFileFromMemory(ClassLoader.getSystemResourceAsStream("merge-classes.dex"));
    
    System.out.println("Building hierarchy");
    val buildData = hierarchyBuilder.buildAgainstApp(fileApp, fileAux);
    val hierarchy = buildData.getValA();
    val renamerAux = buildData.getValB();
    
    System.out.println("Parsing application");
    AuxiliaryDex dexAux = new AuxiliaryDex(fileAux, hierarchy, renamerAux); 
    Dex dexApp = new Dex(fileApp, hierarchy, dexAux);
    
    if (args.length == 3) {
       DexCodeGeneration.DEBUG = false;
//      System.out.println("Instrumenting application");
//      dex.instrument(false);
    } else {
//    	dex.instrument(false);
    }
    
    writeToJar(dexApp, apkFile_new);
    
    long analysisTime = DexCodeGeneration.totalAnalysisTime;
    long translationTime = DexCodeGeneration.totalCGTime;
    long compileTime = DexCodeGeneration.totalDxTime;
    long totalTime = System.currentTimeMillis() - epoch;
    
    System.out.println("===1=== Hierarchy:" + hierarchyTime + 
    		", Analyze:" + analysisTime +
    		", Translate:" + translationTime +
    		", Compile:" + compileTime +
    		", Total:" + totalTime);
  }

}
