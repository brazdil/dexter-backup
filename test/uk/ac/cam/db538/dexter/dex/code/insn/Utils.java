package uk.ac.cam.db538.dexter.dex.code.insn;

import lombok.val;

import org.jf.dexlib.Code.Instruction;

import uk.ac.cam.db538.dexter.dex.DexParsingCache;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.reg.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.reg.RegisterAllocation;

import static org.junit.Assert.*;

public class Utils {

  static DexCodeElement parseAndCompare(Instruction insn, String output) {
    DexCode insnList;
    try {
      insnList = new DexCode(new Instruction[] { insn }, new DexParsingCache());
    } catch (Throwable e) {
      fail(e.getClass().getName() + ": " + e.getMessage());
      return null;
    }

    assertEquals(1, insnList.size());

    val insnInsn = insnList.get(0);
    assertEquals(output, insnInsn.getOriginalAssembly());

    return insnInsn;
  }

  static RegisterAllocation genRegAlloc(DexRegister ... regs) {
    val regAlloc = new RegisterAllocation();
    for (val reg : regs)
      regAlloc.put(reg, reg.getId());
    return regAlloc;
  }

}
