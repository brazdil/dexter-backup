package com.rx201.dx.translator.util;

import uk.ac.cam.db538.dexter.dex.code.DexParameterRegister;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;

public class DexRegisterHelper {
	
	public static DexRegister next(DexRegister reg) {
		assert !(reg instanceof DexParameterRegister);
//		if (reg instanceof DexParameterRegister)
//			return new DexParameterRegister(((DexParameterRegister)reg).getParameterIndex() + 1);
//		else
//			return new DexRegister(reg.getOriginalIndex() + 1);
		return new DexRegister(reg.getOriginalIndex() + 1);
	}
	
	private static long normalize0(DexRegister reg) {
		if (reg instanceof DexParameterRegister)
			return (1L << 32) | ((DexParameterRegister)reg).getParameterIndex();
		else
			return reg.getOriginalIndex();
	}
	
	public static int normalize(DexRegister reg) {
		// We were worried that DexParameterRegister may appear in the middle of the
		// code, hence all the normalisation, but it turns out not to be the case.
		// But just to be certain here.
		assert  !(reg instanceof DexParameterRegister);
		return reg.getOriginalIndex();
	}
	
	public static boolean deepEqual(DexRegister r0, DexRegister r1) {
		return normalize0(r0) == normalize0(r1);
	}

	public static boolean isPair(DexRegister r0, DexRegister r1) {
		return normalize0(r0) + 1 == normalize0(r1);
	}
		
}
