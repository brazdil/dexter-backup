package uk.ac.cam.db538.dexter.dex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.AnnotationDirectoryItem;
import org.jf.dexlib.AnnotationDirectoryItem.FieldAnnotation;
import org.jf.dexlib.AnnotationDirectoryItem.MethodAnnotation;
import org.jf.dexlib.AnnotationDirectoryItem.ParameterAnnotation;
import org.jf.dexlib.AnnotationItem;
import org.jf.dexlib.AnnotationSetItem;
import org.jf.dexlib.AnnotationSetRefList;
import org.jf.dexlib.AnnotationVisibility;
import org.jf.dexlib.ClassDataItem;
import org.jf.dexlib.ClassDataItem.EncodedField;
import org.jf.dexlib.ClassDataItem.EncodedMethod;
import org.jf.dexlib.ClassDefItem;
import org.jf.dexlib.ClassDefItem.StaticFieldInitializer;
import org.jf.dexlib.DexFile;
import org.jf.dexlib.EncodedArrayItem;
import org.jf.dexlib.TypeIdItem;
import org.jf.dexlib.TypeListItem;
import org.jf.dexlib.EncodedValue.EncodedValue;
import org.jf.dexlib.Util.AccessFlags;

import uk.ac.cam.db538.dexter.dex.method.DexAbstractMethod;
import uk.ac.cam.db538.dexter.dex.method.DexDirectMethod;
import uk.ac.cam.db538.dexter.dex.method.DexMethod;
import uk.ac.cam.db538.dexter.dex.method.DexVirtualMethod;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;
import uk.ac.cam.db538.dexter.dex.type.DexTypeCache;
import uk.ac.cam.db538.dexter.hierarchy.ClassDefinition;

public class DexClass {

  @Getter private final Dex parentFile;
  @Getter private final DexClassType type;
  private final Set<AccessFlags> accessFlagSet;
  protected final Set<DexField> fields;
  protected final Set<DexMethod> methods;
  protected final Set<DexAnnotation> annotations;
  @Getter private final String sourceFile;
  private final Map<DexField, EncodedValue> staticInitializer;
  
  public DexClass(Dex parent, DexClassType type, DexClassType superType,
                  Set<AccessFlags> accessFlags, Set<DexField> fields,
                  Set<DexClassType> interfaces,
                  Set<DexAnnotation> annotations, String sourceFile,
                  boolean isInternal) {
    this.parentFile = parent;
    this.type = type;
    this.accessFlagSet = DexUtils.getNonNullAccessFlagSet(accessFlags);
    this.fields = (fields == null) ? new HashSet<DexField>() : fields;
    this.methods = new HashSet<DexMethod>();
    this.annotations = (annotations == null) ? new HashSet<DexAnnotation>() : annotations;
    this.sourceFile = sourceFile;
    this.staticInitializer = new HashMap<DexField, EncodedValue>();
  }
  
  private boolean isMethodAbstract(int accessFlags) {
    for (val flag : AccessFlags.getAccessFlagsForMethod(accessFlags))
      if (flag == AccessFlags.ABSTRACT)
        return true;
    return false;
  }

  private static String getSuperclassTypeDesc(TypeIdItem classType, TypeIdItem superclassType) {
    if (superclassType == null)
      return classType.getTypeDescriptor();
    else
      return superclassType.getTypeDescriptor();
  }

  public DexClass(Dex parent, ClassDefItem clsInfo, boolean isInternal) {
    this(parent,
         DexClassType.parse(clsInfo.getClassType().getTypeDescriptor(), parent.getParsingCache()),
         DexClassType.parse(getSuperclassTypeDesc(clsInfo.getClassType(), clsInfo.getSuperclass()), parent.getParsingCache()),
         DexUtils.getAccessFlagSet(AccessFlags.getAccessFlagsForClass(clsInfo.getAccessFlags())),
         null,
         parseTypeList(clsInfo.getInterfaces(), parent.getParsingCache()),
         parseAnnotations(clsInfo.getAnnotations(), parent.getParsingCache()),
         (clsInfo.getSourceFile() == null) ? null : clsInfo.getSourceFile().getStringValue(),
         isInternal);

    List<MethodAnnotation> methodAnnotations = null;
    if (clsInfo.getAnnotations() != null)
      methodAnnotations = clsInfo.getAnnotations().getMethodAnnotations();

    List<FieldAnnotation> fieldAnnotations = null;
    if (clsInfo.getAnnotations() != null)
      fieldAnnotations = clsInfo.getAnnotations().getFieldAnnotations();

    List<ParameterAnnotation> paramAnnotations = null;
    if (clsInfo.getAnnotations() != null)
    	paramAnnotations = clsInfo.getAnnotations().getParameterAnnotations();

    val clsData = clsInfo.getClassData();
    if (clsData != null) {
      EncodedArrayItem initializers = clsInfo.getStaticFieldInitializers();
      EncodedValue[] initValues;
      if (initializers != null) {
      	  initValues = initializers.getEncodedArray().values;
      } else {
  		  initValues = new EncodedValue[0];
      }
      
      for (val staticFieldInfo : clsData.getStaticFields()) {
    	DexField staticField = new DexField(this, staticFieldInfo, findFieldAnnotation(staticFieldInfo, fieldAnnotations));
        fields.add(staticField);
        if (staticInitializer.size() < initValues.length)
          staticInitializer.put(staticField, initValues[staticInitializer.size()]);
      }
      for (val instanceFieldInfo : clsData.getInstanceFields())
        fields.add(new DexField(this, instanceFieldInfo, findFieldAnnotation(instanceFieldInfo, fieldAnnotations)));

      for (val directMethodInfo : clsData.getDirectMethods())
        methods.add(new DexDirectMethod(this, directMethodInfo, findMethodAnnotation(directMethodInfo, methodAnnotations), 
        		findParameterAnnotation(directMethodInfo, paramAnnotations), isInternal));
      for (val virtualMethodInfo : clsData.getVirtualMethods()) {
        if (isMethodAbstract(virtualMethodInfo.accessFlags))
          methods.add(new DexAbstractMethod(this, virtualMethodInfo, findMethodAnnotation(virtualMethodInfo, methodAnnotations),
        		  findParameterAnnotation(virtualMethodInfo, paramAnnotations)));
        else
          methods.add(new DexVirtualMethod(this, virtualMethodInfo, findMethodAnnotation(virtualMethodInfo, methodAnnotations),
        		  findParameterAnnotation(virtualMethodInfo, paramAnnotations), isInternal));
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

  private static AnnotationSetRefList findParameterAnnotation(EncodedMethod encMethod, List<ParameterAnnotation> paramAnnotations) {
	    if (paramAnnotations != null)
	      for (val annoItem : paramAnnotations)
	        if (annoItem.method.equals(encMethod.method))
	          return annoItem.annotationSet;
	    return null;
	  }

  private static Set<DexClassType> parseTypeList(TypeListItem list, DexTypeCache cache) {
    val set = new HashSet<DexClassType>();

    if (list != null)
      for (val elem : list.getTypes())
        set.add(DexClassType.parse(elem.getTypeDescriptor(), cache));

    return set;
  }

  private static Set<DexAnnotation> parseAnnotations(AnnotationDirectoryItem annotations, DexTypeCache cache) {
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
    return parentFile.getHierarchy().getBaseClassDefinition(type).getSuperclass().getClassType();
  }

  public Set<DexClassType> getInterfaces() {
	val clsDef = parentFile.getHierarchy().getBaseClassDefinition(type);
	if (clsDef instanceof ClassDefinition) {
		val ifaceDefs = ((ClassDefinition) clsDef).getInterfaces();
		if (ifaceDefs.isEmpty())
			return Collections.emptySet();

		val set = new HashSet<DexClassType>();
		for (val ifaceDef : ifaceDefs)
			set.add(ifaceDef.getClassType());
		return set;
	} else
		return Collections.emptySet();
  }

  public Set<DexAnnotation> getAnnotations() {
    return Collections.unmodifiableSet(annotations);
  }

  public void addAnnotation(DexAnnotation anno) {
	  annotations.add(anno);
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
    System.out.println("Instrumenting class " + this.getType().getPrettyName());

    for (val method : methods)
      method.instrument(cache);

    this.addAnnotation(
      new DexAnnotation(parentFile.getInternalClassAnnotation_Type(),
                        AnnotationVisibility.RUNTIME));
  }

  public void writeToFile(DexFile outFile, DexAssemblingCache cache) {
    System.out.println("Assembling class " + this.getType().getPrettyName());
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
    for (val method : methods) {
      val methodAnno = method.assembleAnnotations(outFile, cache);
      if (methodAnno != null)
          asmMethodAnnotations.add(methodAnno);
    }

    val asmFieldAnnotations = new ArrayList<FieldAnnotation>(fields.size());
    for (val field : fields) {
        val fieldAnno = field.assembleAnnotations(outFile, cache);
        if (fieldAnno != null)
            asmFieldAnnotations.add(fieldAnno);
    }

    val asmParamAnnotations = new ArrayList<ParameterAnnotation>(methods.size());
    for (val method : methods) {
        val paramAnno = method.assembleParameterAnnotations(outFile, cache);
        if (paramAnno != null)
            asmParamAnnotations.add(paramAnno);
    }

    AnnotationSetItem asmClassAnnotationSet = null;
    if (asmClassAnnotations.size() > 0)
        asmClassAnnotationSet = AnnotationSetItem.internAnnotationSetItem(
                                    outFile,
                                    asmClassAnnotations);
    
    AnnotationDirectoryItem asmAnnotations = null;
    if (asmClassAnnotationSet!= null || asmFieldAnnotations.size() != 0 || 
            asmMethodAnnotations.size() != 0 || asmParamAnnotations.size() != 0) {
        asmAnnotations = AnnotationDirectoryItem.internAnnotationDirectoryItem(
                                                   outFile,
                                                   asmClassAnnotationSet,
                                                   asmFieldAnnotations,
                                                   asmMethodAnnotations,
                                                   asmParamAnnotations);
    }

    val asmStaticFields = new LinkedList<EncodedField>();
    val asmInstanceFields = new LinkedList<EncodedField>();
    val asmDirectMethods = new LinkedList<EncodedMethod>();
    val asmVirtualMethods = new LinkedList<EncodedMethod>();
    val staticFieldInitializers = new LinkedList<StaticFieldInitializer>();

    for (val field : fields)
      if (field.isStatic()) {
    	EncodedField outField = field.writeToFile(outFile, cache);  
        asmStaticFields.add(outField);
        
    	EncodedValue value = null;
    	if (staticInitializer.containsKey(field))
    		value = DexEncodedValue.cloneEncodedValue(staticInitializer.get(field), cache);
		staticFieldInitializers.add(new StaticFieldInitializer(value, outField));
      } else
        asmInstanceFields.add(field.writeToFile(outFile, cache));

    for (val method : methods) {
      if (method instanceof DexDirectMethod)
        asmDirectMethods.add(method.writeToFile(outFile, cache));
      else if ((method instanceof DexVirtualMethod) || (method instanceof DexAbstractMethod))
        asmVirtualMethods.add(method.writeToFile(outFile, cache));
    }

    val classData = ClassDataItem.internClassDataItem(
                      outFile,
                      asmStaticFields,
                      asmInstanceFields,
                      asmDirectMethods,
                      asmVirtualMethods);

    
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

  public void countInstructions(HashMap<Class, Integer> count) {
    for (val method : methods)
      method.countInstructions(count);
  }
}
