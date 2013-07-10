package uk.ac.cam.db538.dexter.hierarchy.builder;

import org.jf.dexlib.ClassDataItem.EncodedMethod;

import uk.ac.cam.db538.dexter.dex.type.DexMethodId;
import uk.ac.cam.db538.dexter.dex.type.DexPrototype;
import uk.ac.cam.db538.dexter.dex.type.DexTypeCache;

public class DexMethodScanner implements IMethodScanner {

	private final EncodedMethod methodDefItem; 
	private final DexTypeCache typeCache;

	public DexMethodScanner(EncodedMethod method, DexTypeCache typeCache) {
		this.methodDefItem = method;
		this.typeCache = typeCache;
	}

	@Override
	public DexMethodId getMethodId() {
		return DexMethodId.parseMethodId(
			methodDefItem.method.getMethodName().getStringValue(), 
			DexPrototype.parse(methodDefItem.method.getPrototype(), typeCache),
			typeCache);
	}

	@Override
	public int getAccessFlags() {
		return methodDefItem.accessFlags;
	}
}
