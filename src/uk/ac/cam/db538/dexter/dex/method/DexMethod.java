package uk.ac.cam.db538.dexter.dex.method;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.AnnotationDirectoryItem;
import org.jf.dexlib.AnnotationDirectoryItem.MethodAnnotation;
import org.jf.dexlib.AnnotationDirectoryItem.ParameterAnnotation;
import org.jf.dexlib.AnnotationItem;
import org.jf.dexlib.AnnotationSetItem;
import org.jf.dexlib.ClassDataItem.EncodedMethod;
import org.jf.dexlib.CodeItem;
import org.jf.dexlib.DexFile;
import org.jf.dexlib.MethodIdItem;

import uk.ac.cam.db538.dexter.dex.Dex;
import uk.ac.cam.db538.dexter.dex.DexAnnotation;
import uk.ac.cam.db538.dexter.dex.DexAssemblingCache;
import uk.ac.cam.db538.dexter.dex.DexClass;
import uk.ac.cam.db538.dexter.dex.DexInstrumentationCache;
import uk.ac.cam.db538.dexter.dex.DexUtils;
import uk.ac.cam.db538.dexter.hierarchy.MethodDefinition;
import uk.ac.cam.db538.dexter.utils.Cache;

public abstract class DexMethod {

	@Getter private final DexClass parentClass;
	@Getter private final MethodDefinition methodDef;
  
	private final List<DexAnnotation> _annotations;
	@Getter private final List<DexAnnotation> annotations;
	
	// private final List<Set<DexAnnotation>> paramAnnotations;
  
	public DexMethod(DexClass parent, MethodDefinition methodDef) {
		this.parentClass = parent;
		this.methodDef = methodDef;
    
		this._annotations = new ArrayList<DexAnnotation>();
		this.annotations = Collections.unmodifiableList(this._annotations);
    
		// this.paramAnnotations = (paramAnnotations == null) ? new ArrayList<Set<DexAnnotation>>() : paramAnnotations;
	}

	public DexMethod(DexClass parent, MethodDefinition methodDef, EncodedMethod methodInfo, AnnotationDirectoryItem annoDir) {
		this(parent, methodDef);
		
		this._annotations.addAll(init_ParseAnnotations(getParentFile(), methodInfo, annoDir));
	}

	private static List<DexAnnotation> init_ParseAnnotations(Dex dex, EncodedMethod methodInfo, AnnotationDirectoryItem annoDir) {
		if (annoDir == null)
			return Collections.emptyList();
		else
			return DexAnnotation.parseAll(annoDir.getMethodAnnotations(methodInfo.method), dex.getTypeCache());
	}
	
	public Dex getParentFile() {
		return parentClass.getParentFile();
	}

	public void addAnnotation(DexAnnotation anno) {
		_annotations.add(anno);
	}

	public abstract boolean isVirtual();

	public abstract void instrument(DexInstrumentationCache cache);

	protected abstract CodeItem generateCodeItem(DexFile outFile, DexAssemblingCache cache);

	public EncodedMethod writeToFile(DexFile outFile, DexAssemblingCache cache) {
		val classType = cache.getType(parentClass.getClassDef().getType());
		val methodName = cache.getStringConstant(methodDef.getMethodId().getName());
		val methodPrototype = cache.getPrototype(methodDef.getMethodId().getPrototype());

		val methodItem = MethodIdItem.internMethodIdItem(outFile, classType, methodPrototype, methodName);
		CodeItem code = generateCodeItem(outFile, cache);

		return new EncodedMethod(methodItem, DexUtils.assembleAccessFlags(methodDef.getAccessFlags()), code);
	}

	public static Cache<MethodDefinition, MethodIdItem> createAssemblingCache(final DexAssemblingCache cache, final DexFile outFile) {
		return new Cache<MethodDefinition, MethodIdItem>() {
			@Override
			protected MethodIdItem createNewEntry(MethodDefinition key) {
				return MethodIdItem.internMethodIdItem(
						outFile,
						cache.getType(key.getParentClass().getType()),
						cache.getPrototype(key.getMethodId().getPrototype()),
						cache.getStringConstant(key.getMethodId().getName()));
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
		val methodAnno = new MethodAnnotation(cache.getMethod(methodDef), annoSet);

		return methodAnno;
	}

	public ParameterAnnotation assembleParameterAnnotations(DexFile outFile, DexAssemblingCache cache) {
//		if (paramAnnotations.size() == 0)
//			return null;
//		
//		List<AnnotationSetItem> annoList = new ArrayList<AnnotationSetItem>();
//		for (val anno : paramAnnotations)
//			annoList.add(assembleAnnotationSetItem(outFile, cache, anno));
//		
//	    val annoSetRefList = AnnotationSetRefList.internAnnotationSetRefList(outFile, annoList);
//	    val paramAnno = new ParameterAnnotation(cache.getMethod(methodDef), annoSetRefList);
//
//	    return paramAnno;
		// TODO: finish this
		return null;
	}

	public abstract void markMethodOriginal();

	public abstract void countInstructions(HashMap<Class<?>, Integer> count);
}
