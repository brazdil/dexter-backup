package uk.ac.cam.db538.dexter.dex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.AnnotationDirectoryItem;
import org.jf.dexlib.ClassDataItem;
import org.jf.dexlib.ClassDataItem.EncodedField;
import org.jf.dexlib.ClassDataItem.EncodedMethod;
import org.jf.dexlib.ClassDefItem;
import org.jf.dexlib.ClassDefItem.StaticFieldInitializer;
import org.jf.dexlib.DexFile;
import org.jf.dexlib.Util.AccessFlags;

import uk.ac.cam.db538.dexter.dex.method.DexDirectMethod;
import uk.ac.cam.db538.dexter.dex.method.DexMethod;
import uk.ac.cam.db538.dexter.dex.method.DexPurelyVirtualMethod;
import uk.ac.cam.db538.dexter.dex.method.DexVirtualMethod;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;

public class DexClass {

  @Getter private final Dex parentFile;
  @Getter private final DexClassType type;
  @Getter private final DexClassType superType;
  private final Set<AccessFlags> accessFlagSet;
  protected final Set<DexField> fields;
  protected final Set<DexMethod> methods;
  private final Set<DexClassType> interfaces;
  @Getter private final String sourceFile;

  public DexClass(Dex parent, DexClassType type, DexClassType superType,
                  Set<AccessFlags> accessFlags, Set<DexField> fields,
                  Set<DexMethod> methods, Set<DexClassType> interfaces,
                  String sourceFile) {
    this.parentFile = parent;
    this.type = type;
    this.superType = superType;
    this.accessFlagSet = DexUtils.getNonNullAccessFlagSet(accessFlags);
    this.fields = (fields == null) ? new HashSet<DexField>() : fields;
    this.methods = (methods == null) ? new HashSet<DexMethod>() : methods;
    this.interfaces = (interfaces == null) ? new HashSet<DexClassType>() : interfaces;
    this.sourceFile = sourceFile;

    this.type.setDefinedInternally(true);
  }

  public DexClass(Dex parent, ClassDefItem clsInfo) {
    this(parent,
         DexClassType.parse(clsInfo.getClassType().getTypeDescriptor(), parent.getParsingCache()),
         DexClassType.parse(clsInfo.getSuperclass().getTypeDescriptor(), parent.getParsingCache()),
         DexUtils.getAccessFlagSet(AccessFlags.getAccessFlagsForClass(clsInfo.getAccessFlags())),
         null,
         null,
         null,
         (clsInfo.getSourceFile() == null) ? null : clsInfo.getSourceFile().getStringValue());

    if (clsInfo.getInterfaces() != null)
      for (val interfaceType : clsInfo.getInterfaces().getTypes())
        interfaces.add(parent.getParsingCache().getClassType(interfaceType.getTypeDescriptor()));

    val clsData = clsInfo.getClassData();
    if (clsData != null) {
      for (val staticFieldInfo : clsData.getStaticFields())
        fields.add(new DexField(this, staticFieldInfo));
      for (val instanceFieldInfo : clsData.getInstanceFields())
        fields.add(new DexField(this, instanceFieldInfo));

      for (val directMethodInfo : clsData.getDirectMethods())
        methods.add(new DexDirectMethod(this, directMethodInfo));
      for (val virtualMethodInfo : clsData.getVirtualMethods()) {
        if (virtualMethodInfo.codeItem == null)
          methods.add(new DexPurelyVirtualMethod(this, virtualMethodInfo));
        else
          methods.add(new DexVirtualMethod(this, virtualMethodInfo));
      }
    }
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

  public void instrument() {
    for (val method : methods)
      method.instrument();
  }

  public void writeToFile(DexFile outFile, DexAssemblingCache cache) {
    val asmClassType = cache.getType(type);
    val asmSuperType = cache.getType(this.superType);
    val asmAccessFlags = DexUtils.assembleAccessFlags(accessFlagSet);
    val asmInterfaces = (interfaces.isEmpty())
                        ? null
                        : cache.getTypeList(new ArrayList<DexRegisterType>(interfaces));
    val asmSourceFile = (sourceFile == null)
                        ? null
                        : cache.getStringConstant(sourceFile);
    val asmAnnotations = (AnnotationDirectoryItem) null; // AnnotationDirectoryItem.internAnnotationDirectoryItem(
//                        outFile,
//                        AnnotationSetItem.internAnnotationSetItem(
//                          outFile,
//                          new LinkedList<AnnotationItem>()),
//                        new LinkedList<FieldAnnotation>(),
//                        new LinkedList<MethodAnnotation>(),
//                        new LinkedList<ParameterAnnotation>());

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
      else if ((method instanceof DexVirtualMethod) || (method instanceof DexPurelyVirtualMethod))
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
}
