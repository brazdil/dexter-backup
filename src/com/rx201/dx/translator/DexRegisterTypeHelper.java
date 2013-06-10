package com.rx201.dx.translator;

import org.jf.dexlib.Code.Analysis.ClassPath;
import org.jf.dexlib.Code.Analysis.RegisterType;
import org.jf.dexlib.Code.Analysis.RegisterType.Category;

import com.android.dx.rop.type.Type;

import uk.ac.cam.db538.dexter.dex.DexParsingCache;
import uk.ac.cam.db538.dexter.dex.type.DexPrimitiveType;
import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;

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
	
	
	public static RegisterType permissiveMerge(RegisterType t1, RegisterType t2) {
		/* This is valid, so deal with this special case here
		 *     const/4 v8, 0x0
		 *     const/16 v7, 0x20
		 *     shl-long/2addr v5, v7
		 *     
		 * a.k.a merging Integer with LongLo/Hi should be allowed    
		*/
		if ( (t1.category == Category.LongLo || t1.category == Category.LongHi || 
				t1.category == Category.DoubleLo || t1.category == Category.DoubleHi) &&
				t2.category == Category.Integer) 
			return t1;
		
		if ( (t2.category == Category.LongLo || t2.category == Category.LongHi || 
				t2.category == Category.DoubleLo || t2.category == Category.DoubleHi) &&
				t1.category == Category.Integer) 
			return t2;
		
		if (t1.category == Category.UninitRef && t2.category == Category.UninitRef)
			return RegisterType.getRegisterType(Category.UninitRef, 
					ClassPath.getCommonSuperclass(t1.type, t2.type));
		else if (  (t1.category == Category.UninitRef || t1.category == Category.Reference)
				&& (t2.category == Category.UninitRef || t2.category == Category.Reference))
			return RegisterType.getRegisterType(Category.Reference, 
					ClassPath.getCommonSuperclass(t1.type, t2.type));
		
		return t1.merge(t2);
	}
}
