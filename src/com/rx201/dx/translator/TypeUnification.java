package com.rx201.dx.translator;

import org.jf.dexlib.Code.Analysis.ClassPath;
import org.jf.dexlib.Code.Analysis.ClassPath.ArrayClassDef;
import org.jf.dexlib.Code.Analysis.ClassPath.ClassDef;
import org.jf.dexlib.Code.Analysis.RegisterType;
import org.jf.dexlib.Code.Analysis.RegisterType.Category;
import org.jf.dexlib.Code.Analysis.ValidationException;

import com.android.dx.rop.type.Type;

import uk.ac.cam.db538.dexter.dex.Dex;
import uk.ac.cam.db538.dexter.dex.DexParsingCache;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexPrimitiveType;
import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;
import uk.ac.cam.db538.dexter.dex.type.hierarchy.DexClassHierarchy;

public class TypeUnification {


	private static ClassDef getCommonSuperclass(Dex parentFile, ClassDef c1, ClassDef c2) {
		try {
			return ClassPath.getCommonSuperclass(c1, c2);
		} catch (ValidationException e) {
			return ClassPath.getClassDef("Ljava/lang/Object;");
		}
	}
	
	public static RegisterType permissiveMerge(Dex parentFile, RegisterType t1, RegisterType t2) {
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
		
		if ( (t1.category == Category.Integer && t2.category == Category.Float) ||
		        (t2.category == Category.Integer && t1.category == Category.Float))
		    return RegisterType.getRegisterType(Category.Float, null);
		
		if (t1.category == Category.UninitRef && t2.category == Category.UninitRef)
			return RegisterType.getRegisterType(Category.UninitRef, 
					getCommonSuperclass(parentFile, t1.type, t2.type));
		else if (  (t1.category == Category.UninitRef || t1.category == Category.Reference)
				&& (t2.category == Category.UninitRef || t2.category == Category.Reference))
			return RegisterType.getRegisterType(Category.Reference, 
					getCommonSuperclass(parentFile, t1.type, t2.type));
		
		return t1.merge(t2);
	}

	
}
