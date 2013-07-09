package uk.ac.cam.db538.dexter.hierarchy.builder;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jf.dexlib.Util.AccessFlags;

import lombok.val;
import uk.ac.cam.db538.dexter.dex.DexUtils;
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
	
	static List<DexRegisterType> parseArgumentTypes(Class<?>[] params, DexTypeCache typeCache) {
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
				parseArgumentTypes(methodDef.getParameterTypes(), typeCache),
				typeCache),
			typeCache);
	}

	@Override
	public int getAccessFlags() {
		val flags = VmClassScanner.convertModifier(methodDef.getModifiers());
		
		if (methodDef.isBridge())
			flags.add(AccessFlags.BRIDGE);
		if (methodDef.isSynthetic())
			flags.add(AccessFlags.SYNTHETIC);
		if (methodDef.isVarArgs())
			flags.add(AccessFlags.VARARGS);

		return DexUtils.assembleAccessFlags(flags);
	}
}
