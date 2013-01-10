package uk.ac.cam.db538.dexter.dex.method;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.AnnotationItem;
import org.jf.dexlib.AnnotationSetItem;
import org.jf.dexlib.AnnotationDirectoryItem.MethodAnnotation;
import org.jf.dexlib.ClassDataItem.EncodedMethod;
import org.jf.dexlib.CodeItem;
import org.jf.dexlib.DexFile;
import org.jf.dexlib.MethodIdItem;
import org.jf.dexlib.Util.AccessFlags;

import uk.ac.cam.db538.dexter.dex.Dex;
import uk.ac.cam.db538.dexter.dex.DexAnnotation;
import uk.ac.cam.db538.dexter.dex.DexAssemblingCache;
import uk.ac.cam.db538.dexter.dex.DexClass;
import uk.ac.cam.db538.dexter.dex.DexInstrumentationCache;
import uk.ac.cam.db538.dexter.dex.DexUtils;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.utils.Cache;
import uk.ac.cam.db538.dexter.utils.Triple;

public abstract class DexMethod {

  @Getter private DexClass parentClass;
  @Getter private final String name;
  private final Set<AccessFlags> accessFlagSet;
  @Getter private DexPrototype prototype;
  private final Set<DexAnnotation> annotations;

  public DexMethod(DexClass parent, String name, Set<AccessFlags> accessFlags, DexPrototype prototype, Set<DexAnnotation> annotations) {
    this.parentClass = parent;
    this.name = name;
    this.accessFlagSet = DexUtils.getNonNullAccessFlagSet(accessFlags);
    this.prototype = prototype;
    this.annotations = (annotations == null) ? new HashSet<DexAnnotation>() : annotations;

    if (!isAbstract())
      this.parentClass.getParentFile().getClassHierarchy().addImplementedMethod(
        parentClass.getType(), this.name, this.prototype, this.isPrivate());
  }

  public DexMethod(DexClass parent, EncodedMethod methodInfo, AnnotationSetItem encodedAnnotations) {
    this(parent,
         methodInfo.method.getMethodName().getStringValue(),
         DexUtils.getAccessFlagSet(AccessFlags.getAccessFlagsForMethod(methodInfo.accessFlags)),
         new DexPrototype(methodInfo.method.getPrototype(), parent.getParentFile().getParsingCache()),
         DexAnnotation.parseAll(encodedAnnotations, parent.getParentFile().getParsingCache()));
  }

  public Set<AccessFlags> getAccessFlagSet() {
    return Collections.unmodifiableSet(accessFlagSet);
  }

  public Dex getParentFile() {
    return parentClass.getParentFile();
  }

  public boolean isStatic() {
    return accessFlagSet.contains(AccessFlags.STATIC);
  }

  public boolean isAbstract() {
    return accessFlagSet.contains(AccessFlags.ABSTRACT);
  }

  public boolean isPrivate() {
    return accessFlagSet.contains(AccessFlags.PRIVATE);
  }

  public boolean isConstructor() {
    return getAccessFlagSet().contains(AccessFlags.CONSTRUCTOR);
  }

  public Set<DexAnnotation> getAnnotations() {
    return Collections.unmodifiableSet(annotations);
  }

  public void addAnnotation(DexAnnotation anno) {
    annotations.add(anno);
  }

  public abstract boolean isVirtual();

  public abstract void instrument(DexInstrumentationCache cache);

  protected abstract CodeItem generateCodeItem(DexFile outFile, DexAssemblingCache cache);

  public EncodedMethod writeToFile(DexFile outFile, DexAssemblingCache cache) {
    val classType = cache.getType(parentClass.getType());
    val methodName = cache.getStringConstant(name);
    val methodPrototype = cache.getPrototype(prototype);

    val methodItem = MethodIdItem.internMethodIdItem(outFile, classType, methodPrototype, methodName);
    return new EncodedMethod(methodItem, DexUtils.assembleAccessFlags(accessFlagSet), generateCodeItem(outFile, cache));
  }

  public static Cache<Triple<DexClassType, DexPrototype, String>, MethodIdItem> createAssemblingCache(final DexAssemblingCache cache, final DexFile outFile) {
    return new Cache<Triple<DexClassType, DexPrototype, String>, MethodIdItem>() {
      @Override
      protected MethodIdItem createNewEntry(Triple<DexClassType, DexPrototype, String> key) {
        return MethodIdItem.internMethodIdItem(
                 outFile,
                 cache.getType(key.getValA()),
                 cache.getPrototype(key.getValB()),
                 cache.getStringConstant(key.getValC()));
      }
    };
  }

  public MethodAnnotation assembleAnnotations(DexFile outFile, DexAssemblingCache cache) {
    val annoList = new ArrayList<AnnotationItem>(annotations.size());
    for (val anno : annotations)
      annoList.add(anno.writeToFile(outFile, cache));

    val annoSet = AnnotationSetItem.internAnnotationSetItem(outFile, annoList);
    val methodAnno = new MethodAnnotation(cache.getMethod(parentClass.getType(), prototype, name), annoSet);

    return methodAnno;
  }

  public abstract void markMethodOriginal();
}
