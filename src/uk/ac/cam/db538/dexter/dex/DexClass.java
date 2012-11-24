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

import uk.ac.cam.db538.dexter.dex.code.insn.InstructionParsingException;
import uk.ac.cam.db538.dexter.dex.method.DexDirectMethod;
import uk.ac.cam.db538.dexter.dex.method.DexMethod;
import uk.ac.cam.db538.dexter.dex.method.DexPurelyVirtualMethod;
import uk.ac.cam.db538.dexter.dex.method.DexVirtualMethod;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;
import uk.ac.cam.db538.dexter.dex.type.UnknownTypeException;

public class DexClass {

  @Getter private final Dex parentFile;
  @Getter private final DexClassType type;
  @Getter private final DexClassType superType;
  private final Set<AccessFlags> accessFlagSet;
  protected final Set<DexField> fields;
  protected final Set<DexMethod> methods;
  private final Set<DexClassType> interfaces;
  @Getter private final String SourceFile;

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
    this.SourceFile = sourceFile;
  }

  public DexClass(Dex parent, ClassDefItem clsInfo) throws UnknownTypeException, InstructionParsingException {
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
    if (f.getParentClass() != null)
      f.getParentClass().removeField(f);

    fields.add(f);
  }

  public void removeField(DexField f) {
    if (f.getParentClass() == this) {
      fields.remove(f);
      f.setParentClass(null);
    }
  }

  public void addMethod(DexMethod m) {
    if (m.getParentClass() != null)
      m.getParentClass().removeMethod(m);

    methods.add(m);
  }

  public void removeMethod(DexMethod m) {
    if (m.getParentClass() == this) {
      methods.remove(m);
      m.setParentClass(null);
    }
  }

  public void instrument() {
    for (val method : methods)
      method.instrument();
  }

  public void writeToFile(DexFile outFile, DexAssemblingCache cache) {
    val classType = cache.getType(type);
    val superType = cache.getType(superType);
    val accessFlags = DexUtils.assembleAccessFlags(accessFlagSet);
    val interfaces = (interfaces.isEmpty())
                     ? null
                     : cache.getTypeList(new ArrayList<DexRegisterType>(interfaces));
    val sourceFile = (SourceFile == null)
                     ? null
                     : cache.getStringConstant(SourceFile);
    val annotations = (AnnotationDirectoryItem) null; // AnnotationDirectoryItem.internAnnotationDirectoryItem(
//                        outFile,
//                        AnnotationSetItem.internAnnotationSetItem(
//                          outFile,
//                          new LinkedList<AnnotationItem>()),
//                        new LinkedList<FieldAnnotation>(),
//                        new LinkedList<MethodAnnotation>(),
//                        new LinkedList<ParameterAnnotation>());

    val staticFields = new LinkedList<EncodedField>();
    val instanceFields = new LinkedList<EncodedField>();
    val directMethods = new LinkedList<EncodedMethod>();
    val virtualMethods = new LinkedList<EncodedMethod>();

    for (val field : fields)
      if (field.isStatic())
        staticFields.add(field.writeToFile(outFile, cache));
      else
        instanceFields.add(field.writeToFile(outFile, cache));

    for (val method : methods)
      if (method instanceof DexDirectMethod)
        directMethods.add(method.writeToFile(outFile, cache));
      else if ((method instanceof DexVirtualMethod) || (method instanceof DexPurelyVirtualMethod))
        virtualMethods.add(method.writeToFile(outFile, cache));

    val classData = ClassDataItem.internClassDataItem(
                      outFile,
                      staticFields,
                      instanceFields,
                      directMethods,
                      virtualMethods);

    val staticFieldInitializers = new LinkedList<StaticFieldInitializer>();

    ClassDefItem.internClassDefItem(
      outFile, classType, accessFlags, superType,
      interfaces, sourceFile, annotations,
      classData, staticFieldInitializers);
  }
}
