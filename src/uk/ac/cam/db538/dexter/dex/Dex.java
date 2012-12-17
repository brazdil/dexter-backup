package uk.ac.cam.db538.dexter.dex;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.DexFile;
import org.jf.dexlib.Util.ByteArrayAnnotatedOutput;

import uk.ac.cam.db538.dexter.dex.method.DexDirectMethod;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.hierarchy.DexClassHierarchy;
import uk.ac.cam.db538.dexter.utils.NoDuplicatesList;

public class Dex {

  private final List<DexClass> classes;
  @Getter private final DexClassHierarchy classHierarchy;

  @Getter private DexClassType objectTaintStorage_Type;
  @Getter private DexDirectMethod objectTaintStorage_Get;
  @Getter private DexDirectMethod objectTaintStorage_Set;

  @Getter private DexClassType methodCallHelper_Type;
  @Getter private DexField methodCallHelper_Arg;
  @Getter private DexField methodCallHelper_Res;
  @Getter private DexField methodCallHelper_SArg;
  @Getter private DexField methodCallHelper_SRes;
  @Getter private DexDirectMethod methodCallHelper_SArgAcquire;
  @Getter private DexDirectMethod methodCallHelper_SResAcquire;

  @Getter private final DexParsingCache parsingCache;

  public Dex() {
    classes = new NoDuplicatesList<DexClass>();
    parsingCache = new DexParsingCache();
    classHierarchy = new DexClassHierarchy(parsingCache.getClassType("Ljava/lang/Object;"));
  }

  public Dex(File filename, File androidJar) throws IOException {
    this();

    classHierarchy.addAllClassesFromJAR(androidJar, parsingCache);

    val originalFile = new DexFile(filename);
    classes.addAll(parseAllClasses(originalFile));

    classHierarchy.checkConsistentency();
  }

  private static File getMergeFile() throws IOException {
    val tempFile = File.createTempFile("dexter", "merge");

    val tempFile_Out = new BufferedOutputStream(new FileOutputStream(tempFile));
    val mergeResource_In = ClassLoader.getSystemResourceAsStream("merge-classes.dex");

    val buffer = new byte[1024];
    int written;
    while ((written = mergeResource_In.read(buffer)) >= 0)
      tempFile_Out.write(buffer, 0, written);

    tempFile_Out.close();
    mergeResource_In.close();

    return tempFile;
  }

  private List<DexClass> parseAllClasses(DexFile file) {
    val dexClsInfos = file.ClassDefsSection.getItems();
    val classList = new ArrayList<DexClass>(dexClsInfos.size());

    for (val dexClsInfo : dexClsInfos)
      classList.add(new DexClass(this, dexClsInfo));

    return classList;
  }

  /*
   * Needs to generate a short, but unique class name
   */
  private DexClassType generateClassType() {
    String desc;
    long suffix = 0;
    do {
      desc = "L$" + suffix + ";";
      suffix++;
    } while (parsingCache.classTypeExists(desc));

    return DexClassType.parse(desc, parsingCache);
  }

  private List<DexClass> parseExtraClasses() {
    // generate names
    val clsObjTaint = generateClassType();
    val clsObjTaintEntry = generateClassType();
    val clsMethodCallHelper = generateClassType();

    // set descriptor replacements
    parsingCache.setDescriptorReplacement(CLASS_OBJTAINT, clsObjTaint.getDescriptor());
    parsingCache.setDescriptorReplacement(CLASS_OBJTAINTENTRY, clsObjTaintEntry.getDescriptor());
    parsingCache.setDescriptorReplacement(CLASS_METHODCALLHELPER, clsMethodCallHelper.getDescriptor());

    // open the merge DEX file
    DexFile mergeDex;
    try {
      mergeDex = new DexFile(getMergeFile());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    // parse the classes
    val extraClasses = parseAllClasses(mergeDex);

    // remove descriptor replacements
    parsingCache.removeDescriptorReplacement(CLASS_OBJTAINT);
    parsingCache.removeDescriptorReplacement(CLASS_OBJTAINTENTRY);
    parsingCache.removeDescriptorReplacement(CLASS_METHODCALLHELPER);

    // store Object Taint Storage class type and method references
    // store MethodCallHelper class type & method and field references
    objectTaintStorage_Type = clsObjTaint;
    methodCallHelper_Type = clsMethodCallHelper;
    for (val clazz : extraClasses)

      if (clazz.getType() == objectTaintStorage_Type) {
        for (val method : clazz.getMethods()) {
          if (! (method instanceof DexDirectMethod))
            continue;
          if (method.getName().equals("get"))
            objectTaintStorage_Get = (DexDirectMethod) method;
          else if (method.getName().equals("set"))
            objectTaintStorage_Set = (DexDirectMethod) method;
        }

      } else if (clazz.getType() == methodCallHelper_Type) {
        for (val method : clazz.getMethods()) {
          if (! (method instanceof DexDirectMethod))
            continue;
          if (method.getName().equals("S_ARG_acquire"))
            methodCallHelper_SArgAcquire = (DexDirectMethod) method;
          else if (method.getName().equals("S_RES_acquire"))
            methodCallHelper_SResAcquire = (DexDirectMethod) method;
        }

        for (val field : clazz.getFields())
          if (field.getName().equals("ARG"))
            methodCallHelper_Arg = field;
          else if (field.getName().equals("RES"))
            methodCallHelper_Res = field;
          else if (field.getName().equals("S_ARG"))
            methodCallHelper_SArg = field;
          else if (field.getName().equals("S_RES"))
            methodCallHelper_SRes = field;
      }

    return extraClasses;
  }

  public List<DexClass> getClasses() {
    return Collections.unmodifiableList(classes);
  }

  public void instrument() {
    val cache = new DexInstrumentationCache(parsingCache);

    val extraClasses = parseExtraClasses();

    for (val cls : classes)
      cls.instrument(cache);

    classes.addAll(extraClasses);
  }

  public void writeToFile(File filename) throws IOException {
    classHierarchy.checkConsistentency();

    val outFile = new DexFile();
    val out = new ByteArrayAnnotatedOutput();

    val asmCache = new DexAssemblingCache(outFile);
    for (val cls : classes)
      cls.writeToFile(outFile, asmCache);

    outFile.place();
    outFile.writeTo(out);

    val bytes = out.toByteArray();

    DexFile.calcSignature(bytes);
    DexFile.calcChecksum(bytes);

    val fos = new FileOutputStream(filename);
    fos.write(bytes);
    fos.close();
  }

  private static final String CLASS_OBJTAINT = "Luk/ac/cam/db538/dexter/merge/ObjectTaintStorage;";
  private static final String CLASS_OBJTAINTENTRY = "Luk/ac/cam/db538/dexter/merge/ObjectTaintStorage$Entry;";
  private static final String CLASS_METHODCALLHELPER = "Luk/ac/cam/db538/dexter/merge/MethodCallHelper;";
}
