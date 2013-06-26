package com.rx201.dx.translator;

import org.jf.dexlib.Code.Analysis.ClassPath;
import org.jf.dexlib.Code.Analysis.RegisterType;
import org.jf.dexlib.Code.Analysis.RegisterType.Category;

import com.android.dx.rop.type.Type;

import uk.ac.cam.db538.dexter.dex.DexParsingCache;
import uk.ac.cam.db538.dexter.dex.type.DexPrimitiveType;
import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;
import uk.ac.cam.db538.dexter.dex.type.hierarchy.DexClassHierarchy;

public class DexRegisterTypeHelper {
	
	public static RegisterType toRegisterType(DexRegisterType t) {
			return RegisterType.getRegisterTypeForType(t.getDescriptor());
	}
}
