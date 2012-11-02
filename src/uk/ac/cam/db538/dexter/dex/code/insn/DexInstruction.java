package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.HashMap;
import java.util.Map;

import org.jf.dexlib.Code.Instruction;

import lombok.val;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.reg.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.reg.RegisterAllocation;

public abstract class DexInstruction extends DexCodeElement {

  // INSTRUCTION INSTRUMENTATION

  public static class TaintRegisterMap {
    private final Map<DexRegister, DexRegister> RegisterMap;
    private final int IdOffset;

    public TaintRegisterMap(DexCode code) {
      RegisterMap = new HashMap<DexRegister, DexRegister>();

      // find the maximal register id in the code
      // this is strictly for GUI purposes
      // actual register allocation happens later;
      // that said, it still organises the registers
      // according to this
      int maxId = -1;
      for (val reg : code.getAllReferencedRegisters())
        if (maxId < reg.getId())
          maxId = reg.getId();
      IdOffset = maxId + 1;
    }

    public DexRegister getTaintRegister(DexRegister reg) {
      val taintReg = RegisterMap.get(reg);
      if (taintReg == null) {
        val newReg = new DexRegister(reg.getId() + IdOffset);
        RegisterMap.put(reg, newReg);
        return newReg;
      } else
        return taintReg;
    }
  }

  public DexCodeElement[] instrument(TaintRegisterMap mapping) {
    return new DexCodeElement[] { this };
  }

  // ASSEMBLING

  public DexRegister[] getReferencedRegisters() {
    return new DexRegister[] { };
  }

  public Instruction[] assembleBytecode(RegisterAllocation regAlloc) throws InstructionAssemblyException {
    return new Instruction[] { };
  }

  static boolean fitsIntoBits_Signed(long value, int bits) {
    assert bits > 0;
    assert bits <= 64;

    long upperBound = 1L << bits - 1;
    return (value < upperBound) && (value >= -upperBound);
  }

  static boolean fitsIntoBits_Unsigned(long value, int bits) {
    assert bits > 0;
    assert bits <= 64;

    long mask = 0L;
    for (int i = bits; i < 64; ++i)
      mask |= 1L << i;
    return (value & mask) == 0L;
  }

  static boolean fitsIntoHighBits_Signed(long value, int bitsWidth, int bitsBottomEmpty) {
    assert bitsWidth > 0;
    assert bitsWidth <= 64;
    assert bitsBottomEmpty > 0;
    assert bitsBottomEmpty <= 64;
    assert bitsWidth + bitsBottomEmpty <= 64;

    // check that the bottom bits are zero
    // and then that it fits in the sum of bit arguments
    long mask = 0L;
    for (int i = 0; i < bitsBottomEmpty; ++i)
      mask |= 1L << i;
    return ((value & mask) == 0L) &&
           fitsIntoBits_Signed(value, bitsBottomEmpty + bitsWidth);
  }
}
