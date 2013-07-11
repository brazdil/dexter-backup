package uk.ac.cam.db538.dexter.dex.code.insn;

import lombok.Getter;

import org.jf.dexlib.Code.Opcode;

import uk.ac.cam.db538.dexter.dex.code.reg.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.reg.DexTaintRegister;
import uk.ac.cam.db538.dexter.dex.code.reg.DexWideRegister;
import uk.ac.cam.db538.dexter.dex.code.reg.RegisterWidth;

public enum Opcode_Move {
  Single("move", "move-result", "return"),
  Wide("move-wide", "move-result-wide", "return-wide"),
  Object("move-obj", "move-result-obj", "return-obj");
  
  @Getter private final String AssemblyName_Standard;
  @Getter private final String AssemblyName_Result;
  @Getter private final String AssemblyName_Return;

  private Opcode_Move(String assemblyName_Standard, String assemblyName_Result, String assemblyName_Return) {
    AssemblyName_Standard = assemblyName_Standard;
    AssemblyName_Result = assemblyName_Result;
    AssemblyName_Return = assemblyName_Return;
  }

  public static Opcode_Move convert(Opcode opcode) {
    switch (opcode) {
    case MOVE:
    case MOVE_FROM16:
    case MOVE_16:
    case MOVE_RESULT:
    case RETURN:
      return Single;
    case MOVE_OBJECT:
    case MOVE_OBJECT_FROM16:
    case MOVE_OBJECT_16:
    case MOVE_RESULT_OBJECT:
    case RETURN_OBJECT:
      return Object;
    case MOVE_WIDE:
    case MOVE_WIDE_FROM16:
    case MOVE_WIDE_16:
    case MOVE_RESULT_WIDE:
    case RETURN_WIDE:
      return Wide;
    default:
      return null;
    }
  }
  
  public void checkRegisterType(DexRegister reg) {
    switch(this) {
    case Single:
    	if (reg.getWidth() != RegisterWidth.SINGLE)
    		throw new Error("Register width does not fit the opcode of Move");
    	break;
    case Wide:
    	if (reg.getWidth() != RegisterWidth.WIDE)
    		throw new Error("Register width does not fit the opcode of Move");
    	break;
    case Object:
    	if (reg instanceof DexWideRegister || reg instanceof DexTaintRegister) 
    		throw new Error("Object references can only be stored in single, non-taint registers");
    	break;
    }
  }
}