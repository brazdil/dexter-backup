package uk.ac.cam.db538.dexter.dex;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.DexFile;

import uk.ac.cam.db538.dexter.dex.type.ClassRenamer;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.hierarchy.ClassDefinition;
import uk.ac.cam.db538.dexter.hierarchy.FieldDefinition;
import uk.ac.cam.db538.dexter.hierarchy.InterfaceDefinition;
import uk.ac.cam.db538.dexter.hierarchy.MethodDefinition;
import uk.ac.cam.db538.dexter.hierarchy.RuntimeHierarchy;
import uk.ac.cam.db538.dexter.merge.InternalClassAnnotation;
import uk.ac.cam.db538.dexter.merge.InternalMethodAnnotation;
import uk.ac.cam.db538.dexter.merge.MethodCallHelper;
import uk.ac.cam.db538.dexter.merge.ObjectTaintStorage;
import uk.ac.cam.db538.dexter.merge.TaintConstants;

public class AuxiliaryDex extends Dex {

	@Getter private final MethodDefinition method_TaintGet; 
	@Getter private final MethodDefinition method_TaintSet; 

	@Getter private final MethodDefinition method_QueryTaint; 
	@Getter private final MethodDefinition method_ServiceTaint; 
	
	@Getter private final FieldDefinition field_CallParamTaint;
	@Getter private final FieldDefinition field_CallResultTaint;
	@Getter private final FieldDefinition field_CallParamSemaphore;
	@Getter private final FieldDefinition field_CallResultSemaphore;
	
	@Getter private final InterfaceDefinition anno_InternalClass;
	@Getter private final InterfaceDefinition anno_InternalMethod;
	
	public AuxiliaryDex(DexFile dexAux, RuntimeHierarchy hierarchy, ClassRenamer renamer) {
		super(dexAux, hierarchy, null, renamer);
		
		// ObjectTaintStorage class
		val clsObjTaint = getClassDef(hierarchy, renamer, CLASS_OBJTAINT);
		
		this.method_TaintGet = findMethodByName(clsObjTaint, "get");
		this.method_TaintSet = findMethodByName(clsObjTaint, "set");
		
		// TaintConstants class
		val clsTaintConsts = getClassDef(hierarchy, renamer, CLASS_TAINTCONSTANTS);
		
		this.method_QueryTaint = findMethodByName(clsTaintConsts, "queryTaint");
		this.method_ServiceTaint = findMethodByName(clsTaintConsts, "serviceTaint");
		
		// MethodCallHelper class
		val clsMethodCallHelper = getClassDef(hierarchy, renamer, CLASS_METHODCALLHELPER);
		
		this.field_CallParamTaint = findStaticFieldByName(clsMethodCallHelper, "ARG");
		this.field_CallResultTaint = findStaticFieldByName(clsMethodCallHelper, "RES");
		this.field_CallParamSemaphore = findStaticFieldByName(clsMethodCallHelper, "S_ARG");
		this.field_CallResultSemaphore = findStaticFieldByName(clsMethodCallHelper, "S_RES");
		
		// Annotations
		this.anno_InternalClass = getAnnoDef(hierarchy, renamer, CLASS_INTERNALCLASS);
		this.anno_InternalMethod = getAnnoDef(hierarchy, renamer, CLASS_INTERNALMETHOD);
	}
	
	private static MethodDefinition findMethodByName(ClassDefinition clsDef, String name) {
		for (val method : clsDef.getMethods())
			if (method.getMethodId().getName().equals(name))
				return method;
		throw new Error("Failed to locate an auxiliary method");
	}
	
	private static FieldDefinition findStaticFieldByName(ClassDefinition clsDef, String name) {
		for (val field : clsDef.getStaticFields())
			if (field.getFieldId().getName().equals(name))
				return field;
		throw new Error("Failed to locate an auxiliary static field");
	}

	private static ClassDefinition getClassDef(RuntimeHierarchy hierarchy, ClassRenamer classRenamer, String className) {
		return hierarchy.getClassDefinition(new DexClassType(classRenamer.applyRules(className)));
	}
	
	private static InterfaceDefinition getAnnoDef(RuntimeHierarchy hierarchy, ClassRenamer classRenamer, String className) {
		return hierarchy.getInterfaceDefinition(new DexClassType(classRenamer.applyRules(className)));
	}

	private static final String CLASS_OBJTAINT = 
			DexClassType.jvm2dalvik(ObjectTaintStorage.class.getName());
	private static final String CLASS_METHODCALLHELPER = 
			DexClassType.jvm2dalvik(MethodCallHelper.class.getName());
	private static final String CLASS_INTERNALCLASS = 
			DexClassType.jvm2dalvik(InternalClassAnnotation.class.getName());
	private static final String CLASS_INTERNALMETHOD =
			DexClassType.jvm2dalvik(InternalMethodAnnotation.class.getName());
	private static final String CLASS_TAINTCONSTANTS =
			DexClassType.jvm2dalvik(TaintConstants.class.getName());
}
