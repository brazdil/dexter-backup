package uk.ac.cam.db538.dexter.dex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.AnnotationDirectoryItem;
import org.jf.dexlib.AnnotationDirectoryItem.FieldAnnotation;
import org.jf.dexlib.AnnotationDirectoryItem.MethodAnnotation;
import org.jf.dexlib.AnnotationDirectoryItem.ParameterAnnotation;
import org.jf.dexlib.AnnotationItem;
import org.jf.dexlib.AnnotationSetItem;
import org.jf.dexlib.ClassDataItem;
import org.jf.dexlib.ClassDataItem.EncodedField;
import org.jf.dexlib.ClassDataItem.EncodedMethod;
import org.jf.dexlib.ClassDefItem;
import org.jf.dexlib.ClassDefItem.StaticFieldInitializer;
import org.jf.dexlib.DexFile;
import org.jf.dexlib.TypeListItem;
import org.jf.dexlib.Util.AccessFlags;

import uk.ac.cam.db538.dexter.dex.method.DexDirectMethod;
import uk.ac.cam.db538.dexter.dex.method.DexMethod;
import uk.ac.cam.db538.dexter.dex.method.DexAbstractMethod;
import uk.ac.cam.db538.dexter.dex.method.DexVirtualMethod;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;

public class DexClass {

  @Getter private final Dex parentFile;
  @Getter private final DexClassType type;
  private final Set<AccessFlags> accessFlagSet;
  protected final Set<DexField> fields;
  protected final Set<DexMethod> methods;
  @Getter private final String sourceFile;

  public DexClass(Dex parent, DexClassType type, DexClassType superType,
                  Set<AccessFlags> accessFlags, Set<DexField> fields,
                  Set<DexClassType> interfaces,
                  Set<DexAnnotation> annotations, String sourceFile) {
    this.parentFile = parent;
    this.type = type;
    this.accessFlagSet = DexUtils.getNonNullAccessFlagSet(accessFlags);
    this.fields = (fields == null) ? new HashSet<DexField>() : fields;
    this.methods = new HashSet<DexMethod>();
    this.sourceFile = sourceFile;

    this.type.setDefinedInternally(true);
    this.parentFile.getClassHierarchy().addMember(
      this.type,
      superType,
      interfaces,
      annotations,
      isInterface()
    );
  }

  private boolean isMethodAbstract(int accessFlags) {
    for (val flag : AccessFlags.getAccessFlagsForMethod(accessFlags))
      if (flag == AccessFlags.ABSTRACT)
        return true;
    return false;
  }

  public DexClass(Dex parent, ClassDefItem clsInfo) {
    this(parent,
         DexClassType.parse(clsInfo.getClassType().getTypeDescriptor(), parent.getParsingCache()),
         DexClassType.parse(clsInfo.getSuperclass().getTypeDescriptor(), parent.getParsingCache()),
         DexUtils.getAccessFlagSet(AccessFlags.getAccessFlagsForClass(clsInfo.getAccessFlags())),
         null,
         parseTypeList(clsInfo.getInterfaces(), parent.getParsingCache()),
         parseAnnotations(clsInfo.getAnnotations(), parent.getParsingCache()),
         (clsInfo.getSourceFile() == null) ? null : clsInfo.getSourceFile().getStringValue());

    List<MethodAnnotation> methodAnnotations = null;
    if (clsInfo.getAnnotations() != null)
      methodAnnotations = clsInfo.getAnnotations().getMethodAnnotations();

    List<FieldAnnotation> fieldAnnotations = null;
    if (clsInfo.getAnnotations() != null)
      fieldAnnotations = clsInfo.getAnnotations().getFieldAnnotations();

    val clsData = clsInfo.getClassData();
    if (clsData != null) {
      for (val staticFieldInfo : clsData.getStaticFields())
        fields.add(new DexField(this, staticFieldInfo, findFieldAnnotation(staticFieldInfo, fieldAnnotations)));
      for (val instanceFieldInfo : clsData.getInstanceFields())
        fields.add(new DexField(this, instanceFieldInfo, findFieldAnnotation(instanceFieldInfo, fieldAnnotations)));

      for (val directMethodInfo : clsData.getDirectMethods())
        methods.add(new DexDirectMethod(this, directMethodInfo, findMethodAnnotation(directMethodInfo, methodAnnotations)));
      for (val virtualMethodInfo : clsData.getVirtualMethods()) {
        if (isMethodAbstract(virtualMethodInfo.accessFlags))
          methods.add(new DexAbstractMethod(this, virtualMethodInfo, findMethodAnnotation(virtualMethodInfo, methodAnnotations)));
        else
          methods.add(new DexVirtualMethod(this, virtualMethodInfo, findMethodAnnotation(virtualMethodInfo, methodAnnotations)));
      }
    }
  }

  private static AnnotationSetItem findMethodAnnotation(EncodedMethod encMethod, List<MethodAnnotation> methodAnnotations) {
    if (methodAnnotations != null)
      for (val annoItem : methodAnnotations)
        if (annoItem.method.equals(encMethod.method))
          return annoItem.annotationSet;
    return null;
  }

  private static AnnotationSetItem findFieldAnnotation(EncodedField encField, List<FieldAnnotation> fieldAnnotations) {
    if (fieldAnnotations != null)
      for (val annoItem : fieldAnnotations)
        if (annoItem.field.equals(encField.field))
          return annoItem.annotationSet;
    return null;
  }

  private static Set<DexClassType> parseTypeList(TypeListItem list, DexParsingCache cache) {
    val set = new HashSet<DexClassType>();

    if (list != null)
      for (val elem : list.getTypes())
        set.add(DexClassType.parse(elem.getTypeDescriptor(), cache));

    return set;
  }

  private static Set<DexAnnotation> parseAnnotations(AnnotationDirectoryItem annotations, DexParsingCache cache) {
    if (annotations == null)
      return Collections.emptySet();
    else
      return DexAnnotation.parseAll(annotations.getClassAnnotations(), cache);
  }

  public Set<AccessFlags> getAccessFlagSet() {
    return Collections.unmodifiableSet(accessFlagSet);
  }

  public Set<DexField> getFields() {
    return Collections.unmodifiableSet(fields);
  }

  public Set<DexMethod> getMethods() {
    return Collections.unmodifiableSet(methods);
  }

  public DexClassType getSuperclassType() {
    return this.parentFile.getClassHierarchy().getSuperclassType(type);
  }

  public Set<DexClassType> getInterfaces() {
    return this.parentFile.getClassHierarchy().getInterfaces(type);
  }

  public Set<DexAnnotation> getAnnotations() {
    return this.parentFile.getClassHierarchy().getAnnotations(type);
  }

  public boolean isAbstract() {
    return accessFlagSet.contains(AccessFlags.ABSTRACT);
  }

  public boolean isAnnotation() {
    return accessFlagSet.contains(AccessFlags.ANNOTATION);
  }

  public boolean isEnum() {
    return accessFlagSet.contains(AccessFlags.ENUM);
  }

  public boolean isInterface() {
    return accessFlagSet.contains(AccessFlags.INTERFACE);
  }

  public void addField(DexField f) {
    fields.add(f);
  }

  public void addMethod(DexMethod m) {
    methods.add(m);
  }

  public void instrument(DexInstrumentationCache cache) {
    for (val method : methods)
      method.instrument(cache);
  }

  public void writeToFile(DexFile outFile, DexAssemblingCache cache) {
    val interfaces = this.getInterfaces();
    val classAnnotations = this.getAnnotations();

    val asmClassType = cache.getType(type);
    val asmSuperType = cache.getType(getSuperclassType());
    val asmAccessFlags = DexUtils.assembleAccessFlags(accessFlagSet);
    val asmInterfaces = (interfaces.isEmpty())
                        ? null
                        : cache.getTypeList(new ArrayList<DexRegisterType>(interfaces));
    val asmSourceFile = (sourceFile == null)
                        ? null
                        : cache.getStringConstant(sourceFile);

    val asmClassAnnotations = new ArrayList<AnnotationItem>(classAnnotations.size());
    for (val anno : classAnnotations)
      asmClassAnnotations.add(anno.writeToFile(outFile, cache));

    val asmMethodAnnotations = new ArrayList<MethodAnnotation>(methods.size());
    for (val method : methods)
      asmMethodAnnotations.add(method.assembleAnnotations(outFile, cache));

    val asmFieldAnnotations = new ArrayList<FieldAnnotation>(fields.size());
    for (val field : fields)
      asmFieldAnnotations.add(field.assembleAnnotations(outFile, cache));

    val asmAnnotations = AnnotationDirectoryItem.internAnnotationDirectoryItem(
                           outFile,
                           AnnotationSetItem.internAnnotationSetItem(
                             outFile,
                             asmClassAnnotations),
                           asmFieldAnnotations,
                           asmMethodAnnotations,
                           new LinkedList<ParameterAnnotation>());

    val asmStaticFields = new LinkedList<EncodedField>();
    val asmInstanceFields = new LinkedList<EncodedField>();
    val asmDirectMethods = new LinkedList<EncodedMethod>();
    val asmVirtualMethods = new LinkedList<EncodedMethod>();

    for (val field : fields)
      if (field.isStatic())
        asmStaticFields.add(field.writeToFile(outFile, cache));
      else
        asmInstanceFields.add(field.writeToFile(outFile, cache));

    for (val method : methods)
      if (method instanceof DexDirectMethod)
        asmDirectMethods.add(method.writeToFile(outFile, cache));
      else if ((method instanceof DexVirtualMethod) || (method instanceof DexAbstractMethod))
        asmVirtualMethods.add(method.writeToFile(outFile, cache));

    val classData = ClassDataItem.internClassDataItem(
                      outFile,
                      asmStaticFields,
                      asmInstanceFields,
                      asmDirectMethods,
                      asmVirtualMethods);

    val staticFieldInitializers = new LinkedList<StaticFieldInitializer>();

    ClassDefItem.internClassDefItem(
      outFile, asmClassType, asmAccessFlags, asmSuperType,
      asmInterfaces, asmSourceFile, asmAnnotations,
      classData, staticFieldInitializers);
  }

  public void markMethodsOriginal() {
    for (val method : methods)
      method.markMethodOriginal();
  }

  public boolean containsField(String fieldName) {
    for (val f : fields)
      if (f.getName().equals(fieldName))
        return true;
    return false;
  }
}
