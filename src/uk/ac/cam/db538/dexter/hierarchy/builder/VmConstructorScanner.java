package uk.ac.cam.db538.dexter.hierarchy.builder;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import lombok.val;

import org.jf.dexlib.Util.AccessFlags;

import uk.ac.cam.db538.dexter.dex.DexUtils;
import uk.ac.cam.db538.dexter.dex.type.DexMethodId;
import uk.ac.cam.db538.dexter.dex.type.DexPrototype;
import uk.ac.cam.db538.dexter.dex.type.DexTypeCache;

@SuppressWarnings("rawtypes")
public class VmConstructorScanner implements IMethodScanner {

	private final DexTypeCache typeCache;
	private final Constructor methodDef;
	
	public VmConstructorScanner(Constructor methodDef, DexTypeCache typeCache) {
		this.typeCache = typeCache;
		this.methodDef = methodDef;
	}

	@Override
	public DexMethodId getMethodId() {
		return DexMethodId.parseMethodId(
			isStatic() ? "<clinit>" : "<init>",
			DexPrototype.parse(
				typeCache.getCachedType_Void(),
				VmMethodScanner.parseArgumentTypes(methodDef.getParameterTypes(), typeCache),
				typeCache),
			typeCache);
	}
	
	private boolean isStatic() {
		return Modifier.isStatic(methodDef.getModifiers());
	}

	@Override
	public int getAccessFlags() {
		val flags = VmClassScanner.convertModifier(methodDef.getModifiers());
		
		if (methodDef.isSynthetic())
			flags.add(AccessFlags.SYNTHETIC);
		if (methodDef.isVarArgs())
			flags.add(AccessFlags.VARARGS);
		
		flags.add(AccessFlags.CONSTRUCTOR);

		return DexUtils.assembleAccessFlags(flags);
	}
}
