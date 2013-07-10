package uk.ac.cam.db538.dexter.dex.method;

import java.util.HashMap;

import org.jf.dexlib.AnnotationDirectoryItem;
import org.jf.dexlib.ClassDataItem.EncodedMethod;
import org.jf.dexlib.CodeItem;
import org.jf.dexlib.DexFile;

import uk.ac.cam.db538.dexter.dex.DexAssemblingCache;
import uk.ac.cam.db538.dexter.dex.DexClass;
import uk.ac.cam.db538.dexter.dex.DexInstrumentationCache;
import uk.ac.cam.db538.dexter.hierarchy.MethodDefinition;

public class DexAbstractMethod extends DexMethod {

	public DexAbstractMethod(DexClass parent, MethodDefinition methodDef, EncodedMethod methodInfo, AnnotationDirectoryItem annoDir) {
		super(parent, methodDef, methodInfo, annoDir);
	}

	public DexAbstractMethod(DexClass parent, MethodDefinition methodDef) {
		super(parent, methodDef);
	}

	@Override
	public boolean isVirtual() {
		return true;
	}

	@Override
	public void instrument(DexInstrumentationCache cache) { }

	@Override
	protected CodeItem generateCodeItem(DexFile outFile, DexAssemblingCache cache) {
		return null;
	}

	@Override
	public void markMethodOriginal() { }

	@Override
	public void countInstructions(HashMap<Class<?>, Integer> count) { }
}
