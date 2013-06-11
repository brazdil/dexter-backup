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
	public static DexRegisterType fromRegisterType(RegisterType t, DexParsingCache cache) {
		switch(t.category) {
		case Boolean:
		case One: // happened in translation stage of switch test case 
			return DexRegisterType.parse("Z", cache);
		case Byte:
		case PosByte:
			return DexRegisterType.parse("B", cache);
		case Short:
		case PosShort:
			return DexRegisterType.parse("S", cache);
		case Char:
			return DexRegisterType.parse("C", cache);
		case Integer:
		case Null: // <cinit> in assert test case 
			return DexRegisterType.parse("I", cache);
		case Float:
			return DexRegisterType.parse("F", cache);
		case LongLo:
		case LongHi:
			return DexRegisterType.parse("J", cache);
		case DoubleLo:
		case DoubleHi:
			return DexRegisterType.parse("D", cache);

		case UninitRef:
		case UninitThis:
		case Reference:
			return DexRegisterType.parse(t.type.getClassType(), cache);
		
		default:
			throw new UnsupportedOperationException("Unknown register type");
		}
	}
	
	public static RegisterType toRegisterType(DexRegisterType t) {
			return RegisterType.getRegisterTypeForType(t.getDescriptor());
	}
}
