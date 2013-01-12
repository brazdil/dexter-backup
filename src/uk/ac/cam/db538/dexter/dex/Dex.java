package uk.ac.cam.db538.dexter.dex;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.DexFile;
import org.jf.dexlib.Util.AccessFlags;
import org.jf.dexlib.Util.ByteArrayAnnotatedOutput;

import uk.ac.cam.db538.dexter.dex.DexInstrumentationCache.InstrumentationWarning;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ReturnVoid;
import uk.ac.cam.db538.dexter.dex.method.DexDirectMethod;
import uk.ac.cam.db538.dexter.dex.method.DexMethodWithCode;
import uk.ac.cam.db538.dexter.dex.method.DexPrototype;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexVoid;
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

  @Getter private DexClassType internalClassAnnotation_Type;
  @Getter private DexClassType internalMethodAnnotation_Type;

  @Getter private DexClassType taintConstants_Type;

  @Getter private DexClass externalStaticFieldTaint_Class;
  @Getter private DexMethodWithCode externalStaticFieldTaint_Clinit;

  @Getter private final DexParsingCache parsingCache;

  public Dex() {
    classes = new NoDuplicatesList<DexClass>();
    parsingCache = new DexParsingCache();
    classHierarchy = new DexClassHierarchy(DexClassType.parse("Ljava/lang/Object;", parsingCache));
  }

  public Dex(File filename, File androidJar) throws IOException {
    this();

    classHierarchy.addAllClassesFromJAR(androidJar, parsingCache);

    val originalFile = new DexFile(filename);
    classes.addAll(parseAllClasses(originalFile));

    for (val clazz : classes)
      clazz.markMethodsOriginal();

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
    long suffix = 0L;
    do {
      desc = "L$" + suffix + ";";
      suffix++;
    } while (parsingCache.classTypeExists(desc));

    return DexClassType.parse(desc, parsingCache);
  }

  private List<DexClass> parseExtraClasses() {
    // generate names
    val clsTaintConstants = generateClassType();
    val clsInternalClassAnnotation = generateClassType();
    val clsInternalMethodAnnotation = generateClassType();
    val clsObjTaint = generateClassType();
    val clsObjTaintEntry = generateClassType();
    val clsMethodCallHelper = generateClassType();

    // set descriptor replacements
    parsingCache.setDescriptorReplacement(CLASS_TAINTCONSTANTS, clsTaintConstants.getDescriptor());
    parsingCache.setDescriptorReplacement(CLASS_INTERNALCLASS, clsInternalClassAnnotation.getDescriptor());
    parsingCache.setDescriptorReplacement(CLASS_INTERNALMETHOD, clsInternalMethodAnnotation.getDescriptor());
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
    parsingCache.removeDescriptorReplacement(CLASS_TAINTCONSTANTS);
    parsingCache.removeDescriptorReplacement(CLASS_INTERNALCLASS);
    parsingCache.removeDescriptorReplacement(CLASS_INTERNALMETHOD);
    parsingCache.removeDescriptorReplacement(CLASS_OBJTAINT);
    parsingCache.removeDescriptorReplacement(CLASS_OBJTAINTENTRY);
    parsingCache.removeDescriptorReplacement(CLASS_METHODCALLHELPER);

    // store Object Taint Storage class type and method references
    // store MethodCallHelper class type & method and field references
    taintConstants_Type = clsTaintConstants;
    objectTaintStorage_Type = clsObjTaint;
    methodCallHelper_Type = clsMethodCallHelper;
    internalClassAnnotation_Type = clsInternalClassAnnotation;
    internalMethodAnnotation_Type = clsInternalMethodAnnotation;

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

  private List<DexClass> generateExtraClasses() {
    externalStaticFieldTaint_Class = new DexClass(
      this,
      generateClassType(),
      DexClassType.parse("Ljava/lang/Object;", parsingCache),
      EnumSet.of(AccessFlags.PUBLIC),
      null,
      null,
      null,
      null);

    val clinitCode = new DexCode();
    clinitCode.add(new DexInstruction_ReturnVoid(clinitCode));

    externalStaticFieldTaint_Clinit = new DexDirectMethod(
      externalStaticFieldTaint_Class,
      "<clinit>",
      EnumSet.of(AccessFlags.STATIC, AccessFlags.CONSTRUCTOR),
      new DexPrototype(DexVoid.parse("V", parsingCache), null),
      clinitCode,
      null);
    externalStaticFieldTaint_Class.addMethod(externalStaticFieldTaint_Clinit);

    return Arrays.asList(new DexClass[] { externalStaticFieldTaint_Class });
  }

  public List<DexClass> getClasses() {
    return Collections.unmodifiableList(classes);
  }

  public List<InstrumentationWarning> instrument() {
    val cache = new DexInstrumentationCache(this);

    val extraClassesLinked = parseExtraClasses();
    val extraClassesGenerated = generateExtraClasses();

    for (val cls : classes)
      cls.instrument(cache);

    classes.addAll(extraClassesLinked);
    classes.addAll(extraClassesGenerated);

    classHierarchy.checkConsistentency();

    return cache.getWarnings();
  }

  public void writeToFile(File filename) throws IOException {
    classHierarchy.checkConsistentency();

    val outFile = new DexFile();
    val out = new ByteArrayAnnotatedOutput();

    val asmCache = new DexAssemblingCache(outFile, parsingCache);
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
  private static final String CLASS_INTERNALCLASS = "Luk/ac/cam/db538/dexter/merge/InternalClassAnnotation;";
  private static final String CLASS_INTERNALMETHOD = "Luk/ac/cam/db538/dexter/merge/InternalMethodAnnotation;";
  private static final String CLASS_TAINTCONSTANTS = "Luk/ac/cam/db538/dexter/merge/TaintConstants;";
}
