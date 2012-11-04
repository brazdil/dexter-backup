package uk.ac.cam.db538.dexter.dex.code.insn;

import lombok.val;

import org.jf.dexlib.Code.Instruction;

import uk.ac.cam.db538.dexter.dex.DexParsingCache;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.reg.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.reg.RegisterAllocation;
import uk.ac.cam.db538.dexter.dex.type.UnknownTypeException;

import static org.junit.Assert.*;

public class Utils {

  static DexCodeElement parseAndCompare(Instruction insn, String output) {
    DexCode code;
    try {
      code = new DexCode(new Instruction[] { insn }, new DexParsingCache());
    } catch (Throwable e) {
      fail(e.getClass().getName() + ": " + e.getMessage());
      return null;
    }

    val insnList = code.getInstructionList();
    
    assertEquals(1, insnList.size());

    val insnInsn = insnList.get(0);
    assertEquals(output, insnInsn.getOriginalAssembly());

    return insnInsn;
  }

  static void parseAndCompare(Instruction[] insns, String[] output) {
    DexCode code;
    try {
      code = new DexCode(insns, new DexParsingCache());
    } catch (UnknownTypeException e) {
      fail(e.getClass().getName() + ": " + e.getMessage());
      return;
    }

    val insnList = code.getInstructionList();

    assertEquals(output.length, insnList.size());
    for (int i = 0; i < output.length; ++i)
      assertEquals(output[i], insnList.get(i).getOriginalAssembly());
  }

  static RegisterAllocation genRegAlloc(DexRegister ... regs) {
    val regAlloc = new RegisterAllocation();
    for (val reg : regs)
      regAlloc.put(reg, reg.getId());
    return regAlloc;
  }

  static long numFitsInto_Signed(int bits) {
    return (1L << (bits - 1)) - 1;
  }

  static int numFitsInto_Unsigned(int bits) {
    return (1 << bits) - 1;
  }
  
  static void instrumentAndCompare(DexCode code, String[] output) {
	  val insnList = code.instrument().getInstructionList();
	    assertEquals(output.length, insnList.size());
	    for (int i = 0; i < output.length; ++i)
	      assertEquals(output[i], insnList.get(i).getOriginalAssembly());
  }
}
