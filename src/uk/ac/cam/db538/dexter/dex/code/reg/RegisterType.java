package uk.ac.cam.db538.dexter.dex.code.reg;

import lombok.Getter;

import org.jf.dexlib.Code.Opcode;

public enum RegisterType {
	SINGLE_PRIMITIVE(""),
	WIDE_PRIMITIVE("-wide"),
	REFERENCE("-obj");
	
	@Getter private String asmSuffix;
	
	RegisterType(String suffix) {
		this.asmSuffix = suffix;
	}

	public void checkRegisterType(DexRegister reg) {
		if (!reg.canStoreType(this))
			throw new Error("Register cannot store type " + this.name());
	}

	public static RegisterType fromOpcode(Opcode opcode) {
		switch (opcode) {
		case MOVE:
	    case MOVE_FROM16:
	    case MOVE_16:
	    case MOVE_RESULT:
	    case RETURN:
	      return SINGLE_PRIMITIVE;
	    
	    case MOVE_OBJECT:
	    case MOVE_OBJECT_FROM16:
	    case MOVE_OBJECT_16:
	    case MOVE_RESULT_OBJECT:
	    case RETURN_OBJECT:
	      return REFERENCE;
	    
	    case MOVE_WIDE:
	    case MOVE_WIDE_FROM16:
	    case MOVE_WIDE_16:
	    case MOVE_RESULT_WIDE:
	    case RETURN_WIDE:
	      return WIDE_PRIMITIVE;

	    default:
	      return null;
	    }
	}
}