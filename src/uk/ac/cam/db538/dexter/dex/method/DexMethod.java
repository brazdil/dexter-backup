package uk.ac.cam.db538.dexter.dex.method;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.AnnotationDirectoryItem.MethodAnnotation;
import org.jf.dexlib.AnnotationDirectoryItem.ParameterAnnotation;
import org.jf.dexlib.AnnotationItem;
import org.jf.dexlib.AnnotationSetItem;
import org.jf.dexlib.AnnotationSetRefList;
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
import uk.ac.cam.db538.dexter.dex.type.DexPrototype;
import uk.ac.cam.db538.dexter.dex.type.DexReferenceType;
import uk.ac.cam.db538.dexter.utils.Cache;
import uk.ac.cam.db538.dexter.utils.Triple;

public abstract class DexMethod {

  @Getter private DexClass parentClass;
  @Getter private final String name;
  private final Set<AccessFlags> accessFlagSet;
  @Getter private DexPrototype prototype;
  private final Set<DexAnnotation> annotations;
  private final List<Set<DexAnnotation>> paramAnnotations;
  private EncodedMethod parentMethod;
  
  public DexMethod(DexClass parent, String name, Set<AccessFlags> accessFlags, DexPrototype prototype, Set<DexAnnotation> annotations, List<Set<DexAnnotation>> paramAnnotations) {
    this.parentClass = parent;
    this.name = name;
    this.accessFlagSet = DexUtils.getNonNullAccessFlagSet(accessFlags);
    this.prototype = prototype;
    this.annotations = (annotations == null) ? new HashSet<DexAnnotation>() : annotations;
    this.paramAnnotations = (paramAnnotations == null) ? new ArrayList<Set<DexAnnotation>>() : paramAnnotations;

    if (!isAbstract())
      this.parentClass.getParentFile().getClassHierarchy().addImplementedMethod(
        parentClass.getType(), this.name, this.prototype, this.isPrivate(), this.isNative(), this.isPublic());
    parentMethod = null;
  }

  public DexMethod(DexClass parent, EncodedMethod methodInfo, AnnotationSetItem encodedAnnotations, AnnotationSetRefList paramAnnotations) {
    this(parent,
         methodInfo.method.getMethodName().getStringValue(),
         DexUtils.getAccessFlagSet(AccessFlags.getAccessFlagsForMethod(methodInfo.accessFlags)),
         DexPrototype.parse(methodInfo.method.getPrototype(), parent.getParentFile().getParsingCache()),
         DexAnnotation.parseAll(encodedAnnotations, parent.getParentFile().getParsingCache()),
         DexAnnotation.parseAll(paramAnnotations, parent.getParentFile().getParsingCache()));
    parentMethod = methodInfo;
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

  public boolean isNative() {
    return accessFlagSet.contains(AccessFlags.NATIVE);
  }

  public boolean isPublic() {
    return accessFlagSet.contains(AccessFlags.PUBLIC);
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
    CodeItem code = generateCodeItem(outFile, cache);

    return new EncodedMethod(methodItem, DexUtils.assembleAccessFlags(accessFlagSet), code);
  }

  public static Cache<Triple<DexReferenceType, DexPrototype, String>, MethodIdItem> createAssemblingCache(final DexAssemblingCache cache, final DexFile outFile) {
    return new Cache<Triple<DexReferenceType, DexPrototype, String>, MethodIdItem>() {
      @Override
      protected MethodIdItem createNewEntry(Triple<DexReferenceType, DexPrototype, String> key) {
        return MethodIdItem.internMethodIdItem(
                 outFile,
                 cache.getType(key.getValA()),
                 cache.getPrototype(key.getValB()),
                 cache.getStringConstant(key.getValC()));
      }
    };
  }

  private AnnotationSetItem assembleAnnotationSetItem(DexFile outFile, DexAssemblingCache cache, Collection<DexAnnotation> annoCollections) {
	    val annoList = new ArrayList<AnnotationItem>(annoCollections.size());
	    for (val anno : annoCollections)
	      annoList.add(anno.writeToFile(outFile, cache));

	    return AnnotationSetItem.internAnnotationSetItem(outFile, annoList);
	  
  }
  public MethodAnnotation assembleAnnotations(DexFile outFile, DexAssemblingCache cache) {
	    if (annotations.size() == 0)
		    return null;
	    val annoSet = assembleAnnotationSetItem(outFile, cache, annotations);
	    val methodAnno = new MethodAnnotation(cache.getMethod(parentClass.getType(), prototype, name), annoSet);

	    return methodAnno;
	  }

  public ParameterAnnotation assembleParameterAnnotations(DexFile outFile, DexAssemblingCache cache) {
	    if (paramAnnotations.size() == 0)
	    	return null;
	    List<AnnotationSetItem> annoList = new ArrayList<AnnotationSetItem>();
		for (val anno : paramAnnotations) {
	      annoList.add(assembleAnnotationSetItem(outFile, cache, anno));
	    }
	    val annoSetRefList = AnnotationSetRefList.internAnnotationSetRefList(outFile, annoList);
	    val paramAnno = new ParameterAnnotation(cache.getMethod(parentClass.getType(), prototype, name), annoSetRefList);

	    return paramAnno;
	  }

  public abstract void markMethodOriginal();

  public abstract void countInstructions(HashMap<Class, Integer> count);
}
