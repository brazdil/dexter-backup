package uk.ac.cam.db538.dexter.dex;

import java.util.ArrayList;
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

  @Getter private final Dex ParentFile;
  @Getter private final DexClassType Type;
  @Getter private final DexClassType SuperType;
  @Getter private final Set<AccessFlags> AccessFlagSet;
  @Getter protected final Set<DexField> Fields;
  @Getter protected final Set<DexMethod> Methods;
  @Getter private final Set<DexClassType> Interfaces;
  @Getter private final String SourceFile;

  public DexClass(Dex parent, DexClassType type, DexClassType superType,
                  Set<AccessFlags> accessFlags, Set<DexField> fields,
                  Set<DexMethod> methods, Set<DexClassType> interfaces,
                  String sourceFile) {
    ParentFile = parent;
    Type = type;
    SuperType = superType;
    AccessFlagSet = DexUtils.getNonNullAccessFlagSet(accessFlags);
    Fields = (fields == null) ? new HashSet<DexField>() : fields;
    Methods = (methods == null) ? new HashSet<DexMethod>() : methods;
    Interfaces = (interfaces == null) ? new HashSet<DexClassType>() : interfaces;
    SourceFile = sourceFile;
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
        Interfaces.add(parent.getParsingCache().getClassType(interfaceType.getTypeDescriptor()));

    val clsData = clsInfo.getClassData();
    if (clsData != null) {
      for (val staticFieldInfo : clsData.getStaticFields())
        Fields.add(new DexField(this, staticFieldInfo));
      for (val instanceFieldInfo : clsData.getInstanceFields())
        Fields.add(new DexField(this, instanceFieldInfo));

      for (val directMethodInfo : clsData.getDirectMethods())
        Methods.add(new DexDirectMethod(this, directMethodInfo));
      for (val virtualMethodInfo : clsData.getVirtualMethods()) {
        if (virtualMethodInfo.codeItem == null)
          Methods.add(new DexPurelyVirtualMethod(this, virtualMethodInfo));
        else
          Methods.add(new DexVirtualMethod(this, virtualMethodInfo));
      }

    }
  }

  public boolean isAbstract() {
    return AccessFlagSet.contains(AccessFlags.ABSTRACT);
  }

  public boolean isAnnotation() {
    return AccessFlagSet.contains(AccessFlags.ANNOTATION);
  }

  public boolean isEnum() {
    return AccessFlagSet.contains(AccessFlags.ENUM);
  }

  public boolean isInterface() {
    return AccessFlagSet.contains(AccessFlags.INTERFACE);
  }

  public void addField(DexField f) {
    if (f.getParentClass() != null)
      f.getParentClass().removeField(f);

    Fields.add(f);
  }

  public void removeField(DexField f) {
    if (f.getParentClass() == this) {
      Fields.remove(f);
      f.setParentClass(null);
    }
  }

  public void addMethod(DexMethod m) {
    if (m.getParentClass() != null)
      m.getParentClass().removeMethod(m);

    Methods.add(m);
  }

  public void removeMethod(DexMethod m) {
    if (m.getParentClass() == this) {
      Methods.remove(m);
      m.setParentClass(null);
    }
  }

  public void instrument() {
    for (val method : Methods)
      method.instrument();
  }

  public void writeToFile(DexFile outFile, DexAssemblingCache cache) {
    val classType = cache.getType(Type);
    val superType = cache.getType(SuperType);
    val accessFlags = DexUtils.assembleAccessFlags(AccessFlagSet);
    val interfaces = (Interfaces.isEmpty())
                     ? null
                     : cache.getTypeList(new ArrayList<DexRegisterType>(Interfaces));
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

    for (val field : Fields)
      if (field.isStatic())
        staticFields.add(field.writeToFile(outFile, cache));
      else
        instanceFields.add(field.writeToFile(outFile, cache));

    for (val method : Methods)
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
