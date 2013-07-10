package uk.ac.cam.db538.dexter.dex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.AnnotationDirectoryItem;
import org.jf.dexlib.AnnotationDirectoryItem.FieldAnnotation;
import org.jf.dexlib.AnnotationDirectoryItem.MethodAnnotation;
import org.jf.dexlib.AnnotationDirectoryItem.ParameterAnnotation;
import org.jf.dexlib.AnnotationItem;
import org.jf.dexlib.AnnotationSetItem;
import org.jf.dexlib.AnnotationVisibility;
import org.jf.dexlib.ClassDataItem;
import org.jf.dexlib.ClassDataItem.EncodedField;
import org.jf.dexlib.ClassDataItem.EncodedMethod;
import org.jf.dexlib.ClassDefItem;
import org.jf.dexlib.ClassDefItem.StaticFieldInitializer;
import org.jf.dexlib.DexFile;
import org.jf.dexlib.EncodedValue.EncodedValue;

import uk.ac.cam.db538.dexter.dex.method.DexAbstractMethod;
import uk.ac.cam.db538.dexter.dex.method.DexDirectMethod;
import uk.ac.cam.db538.dexter.dex.method.DexMethod;
import uk.ac.cam.db538.dexter.dex.method.DexVirtualMethod;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;
import uk.ac.cam.db538.dexter.hierarchy.BaseClassDefinition;
import uk.ac.cam.db538.dexter.hierarchy.ClassDefinition;

public class DexClass {

	@Getter private final Dex parentFile;
	@Getter private final BaseClassDefinition classDef;
  
	private final Set<DexMethod> _methods;
	@Getter private final Set<DexMethod> methods;
  
	private final Set<DexField> _instanceFields;
	@Getter private final Set<DexField> instanceFields;

	private final Set<DexStaticField> _staticFields;
	@Getter private final Set<DexStaticField> staticFields;
	
	private final Set<DexAnnotation> _annotations;
	@Getter private final Set<DexAnnotation> annotations;
  
	@Getter private final String sourceFile;
  
	public DexClass(Dex parent, BaseClassDefinition classDef, String sourceFile) {
		this.parentFile = parent;
		this.classDef = classDef;
    
		this._methods = new HashSet<DexMethod>();
    	this.methods = Collections.unmodifiableSet(this._methods);
    
    	this._instanceFields = new HashSet<DexField>();
    	this.instanceFields = Collections.unmodifiableSet(this._instanceFields);

    	this._staticFields = new HashSet<DexStaticField>();
    	this.staticFields = Collections.unmodifiableSet(this._staticFields);
    	
    	this._annotations = new HashSet<DexAnnotation>();
    	this.annotations = Collections.unmodifiableSet(this._annotations);
    
    	this.sourceFile = sourceFile;
	}
  
	public DexClass(Dex parent, ClassDefItem clsItem) {
		this(parent,
		     init_FindClassDefinition(parent, clsItem),
		     DexUtils.parseString(clsItem.getSourceFile()));

		val annotationDirectory = clsItem.getAnnotations();
		this._annotations.addAll(init_ParseAnnotations(parent, annotationDirectory));
		
//		
//		
//    val clsData = clsInfo.getClassData();
//    if (clsData != null) {
//      
//      for (val instanceFieldInfo : clsData.getInstanceFields())
//        fields.add(new DexField(this, instanceFieldInfo, findFieldAnnotation(instanceFieldInfo, fieldAnnotations)));
//
//      for (val directMethodInfo : clsData.getDirectMethods())
//        methods.add(new DexDirectMethod(this, directMethodInfo, findMethodAnnotation(directMethodInfo, methodAnnotations), 
//        		findParameterAnnotation(directMethodInfo, paramAnnotations)));
//      for (val virtualMethodInfo : clsData.getVirtualMethods()) {
//        if (isMethodAbstract(virtualMethodInfo.accessFlags))
//          methods.add(new DexAbstractMethod(this, virtualMethodInfo, findMethodAnnotation(virtualMethodInfo, methodAnnotations),
//        		  findParameterAnnotation(virtualMethodInfo, paramAnnotations)));
//        else
//          methods.add(new DexVirtualMethod(this, virtualMethodInfo, findMethodAnnotation(virtualMethodInfo, methodAnnotations),
//        		  findParameterAnnotation(virtualMethodInfo, paramAnnotations)));
//      }
//      
//    }
	}
  
	private static ClassDefinition init_FindClassDefinition(Dex parent, ClassDefItem clsItem) {
		val hierarchy = parent.getHierarchy();
		val clsType = DexClassType.parse(clsItem.getClassType().getTypeDescriptor(), 
		                                 hierarchy.getTypeCache());
		return hierarchy.getClassDefinition(clsType); 
	}

	private static Set<DexAnnotation> init_ParseAnnotations(Dex parent, AnnotationDirectoryItem annoDir) {
		if (annoDir == null)
			return Collections.emptySet();
		else
			return DexAnnotation.parseAll(annoDir.getClassAnnotations(), parent.getTypeCache());
	}
	
	  
//	private static AnnotationSetItem findMethodAnnotation(EncodedMethod encMethod, AnnotationDirectoryItem annoDir) {
//		if (annoDir != null)
//			return annoDir.getMethodAnnotations(encMethod.method);
//		else
//			return null;
//	}
//
//	private static AnnotationSetRefList findParameterAnnotation(EncodedMethod encMethod, AnnotationDirectoryItem annoDir) {
//		if (annoDir != null)
//			return annoDir.getParameterAnnotations(encMethod.method);
//		else
//			return null;
//	}
//

  public Set<DexClassType> getInterfaces() {
	if (classDef instanceof ClassDefinition) {
		val ifaceDefs = ((ClassDefinition) classDef).getInterfaces();
		if (ifaceDefs.isEmpty())
			return Collections.emptySet();

		val set = new HashSet<DexClassType>();
		for (val ifaceDef : ifaceDefs)
			set.add(ifaceDef.getType());
		return set;
	} else
		return Collections.emptySet();
  }

  public void addAnnotation(DexAnnotation anno) {
	  this._annotations.add(anno);
  }

  public void addField(DexField f) {
	  if (f instanceof DexStaticField)
		  this._staticFields.add((DexStaticField) f);
	  else
		  this._instanceFields.add(f);
  }

  public void addMethod(DexMethod m) {
    this._methods.add(m);
  }

  public void instrument(DexInstrumentationCache cache) {
    System.out.println("Instrumenting class " + this.classDef.getType().getPrettyName());
	  
    for (val method : this._methods)
      method.instrument(cache);

    this.addAnnotation(
      new DexAnnotation(parentFile.getInternalClassAnnotation_Type(),
                        AnnotationVisibility.RUNTIME));
  }

  public void writeToFile(DexFile outFile, DexAssemblingCache cache) {
    System.out.println("Assembling class " + this.classDef.getType().getPrettyName());
    
    val interfaces = this.getInterfaces();
    val classAnnotations = this.getAnnotations();

    val asmClassType = cache.getType(classDef.getType());
    val asmSuperType = cache.getType(classDef.getSuperclass().getType());
    val asmAccessFlags = DexUtils.assembleAccessFlags(classDef.getAccessFlags());
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

    val asmFieldAnnotations = new ArrayList<FieldAnnotation>(instanceFields.size() + staticFields.size());
    for (val field : instanceFields) {
        val fieldAnno = field.assembleAnnotations(outFile, cache);
        if (fieldAnno != null)
            asmFieldAnnotations.add(fieldAnno);
    }
    for (val field : staticFields) {
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

    for (val field : staticFields) {
    	EncodedField outField = field.writeToFile(outFile, cache);  
        asmStaticFields.add(outField);
        
    	EncodedValue initialValue = field.getInitialValue();
    	if (initialValue != null) {
    		initialValue = DexUtils.cloneEncodedValue(initialValue, cache);
    		staticFieldInitializers.add(new StaticFieldInitializer(initialValue, outField));
    	}
    }
    
    for (val field : instanceFields)
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

  public void countInstructions(HashMap<Class<?>, Integer> count) {
    for (val method : methods)
      method.countInstructions(count);
  }
}
