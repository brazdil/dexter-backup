package uk.ac.cam.db538.dexter;

import java.io.File;
import java.io.IOException;

import lombok.val;

import org.jf.dexlib.DexFile;

import uk.ac.cam.db538.dexter.dex.Dex;
import uk.ac.cam.db538.dexter.hierarchy.RuntimeHierarchy;
import uk.ac.cam.db538.dexter.hierarchy.builder.HierarchyBuilder;

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
    val frameworkDir = new File(args[0]);
    if (!frameworkDir.isDirectory()) {
      System.err.println("<framework-dir> is not a directory");
      System.exit(1);
    }
    
    // build runtime class hierarchy
    val hierarchyBuilder = new HierarchyBuilder();
    
    System.out.println("Scanning framework");
    hierarchyBuilder.importFrameworkFolder(frameworkDir);
    
//    System.out.println("Storing hierarchy");
//    hierarchyBuilder.serialize(new File("hierarchy.dump"));
    
//    System.out.println("Loading framework from dump");
//    HierarchyBuilder hierarchyBuilder = HierarchyBuilder.deserialize(new File("hierarchy.dump"));
    
    System.out.println("Scanning application");
    val dexFile = new DexFile(apkFile);
    
    System.out.println("Building hierarchy");
    RuntimeHierarchy hierarchy = hierarchyBuilder.buildAgainstApp(dexFile);
    
    System.out.println("Parsing application");
    val dexAux = ClassLoader.getSystemResourceAsStream("merge-classes.dex");
    Dex dex = new Dex(dexFile, hierarchy, dexAux);
    
    System.out.println("Instrumenting application");
    dex.instrument(false);
    
    System.out.println("DONE");
  }
}
