package uk.ac.cam.db538.dexter.dex.code;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import lombok.val;

import org.jf.dexlib.Code.Instruction;

import uk.ac.cam.db538.dexter.dex.DexParsingCache;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.type.UnknownTypeException;

public class Utils {

  public static DexCodeElement parseAndCompare(Instruction insn, String output) {
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

  public static void parseAndCompare(Instruction[] insns, String[] output) {
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

  public static Map<DexRegister, Integer> genRegAlloc(DexRegister ... regs) {
    val regAlloc = new HashMap<DexRegister, Integer>();
    for (val reg : regs)
      regAlloc.put(reg, reg.getId());
    return regAlloc;
  }

  public static long numFitsInto_Signed(int bits) {
    return (1L << (bits - 1)) - 1;
  }

  public static int numFitsInto_Unsigned(int bits) {
    return (1 << bits) - 1;
  }

  public static void instrumentAndCompare(DexCode code, String[] output) {
	code.instrument();
    val insnList = code.getInstructionList();
    assertEquals(output.length, insnList.size());
    for (int i = 0; i < output.length; ++i)
      assertEquals(output[i], insnList.get(i).getOriginalAssembly());
  }
}
