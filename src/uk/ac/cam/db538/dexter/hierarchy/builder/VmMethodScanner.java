package uk.ac.cam.db538.dexter.hierarchy.builder;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.val;
import uk.ac.cam.db538.dexter.dex.type.DexMethodId;
import uk.ac.cam.db538.dexter.dex.type.DexPrototype;
import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;
import uk.ac.cam.db538.dexter.dex.type.DexType;
import uk.ac.cam.db538.dexter.dex.type.DexTypeCache;

public class VmMethodScanner implements IMethodScanner {

	private final DexTypeCache typeCache;
	private final Method methodDef;
	
	public VmMethodScanner(Method methodDef, DexTypeCache typeCache) {
		this.typeCache = typeCache;
		this.methodDef = methodDef;
	}

	private DexType parseReturnType() {
		return DexType.parse(
			DexType.jvm2dalvik(methodDef.getReturnType().getName()), 
			typeCache);
	}
	
	private List<DexRegisterType> parseArgumentTypes() {
		val params = methodDef.getParameterTypes();
		
		if (params.length == 0)
			return Collections.emptyList();

		val allParams = new ArrayList<DexRegisterType>(params.length);
		for (val param : params)
			allParams.add(DexRegisterType.parse(DexRegisterType.jvm2dalvik(param.getName()), typeCache));
	
		return allParams;
	}
	
	@Override
	public DexMethodId getMethodId() {
		return DexMethodId.parseMethodId(
			methodDef.getName(),
			DexPrototype.parse(
				parseReturnType(),
				parseArgumentTypes(),
				typeCache),
			typeCache);
	}

	@Override
	public int getAccessFlags() {
		return methodDef.getModifiers();
	}

}
