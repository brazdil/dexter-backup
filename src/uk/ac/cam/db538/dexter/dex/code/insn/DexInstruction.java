package uk.ac.cam.db538.dexter.dex.code.insn;


import org.jf.dexlib.Code.Instruction;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.DexCode_AssemblingState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;

public abstract class DexInstruction extends DexCodeElement {

  public DexInstruction(DexCode methodCode) {
    super(methodCode);
  }

  // INSTRUCTION INSTRUMENTATION

  public DexCodeElement[] instrument(DexCode_InstrumentationState mapping) {
    throw new UnsupportedOperationException("Instruction " + this.getClass().getSimpleName() + " doesn't have instrumentation implemented");
  }

  // ASSEMBLING

  public Instruction[] assembleBytecode(DexCode_AssemblingState state) {
    throw new UnsupportedOperationException("Instruction " + this.getClass().getSimpleName() + " doesn't have assembling implemented");
  }

  protected final Instruction[] throwCannotAssembleException(String reason) {
    throw new InstructionAssemblyException("Instruction " + this.getClass().getSimpleName() + " couldn't be assembled (" + reason + ")");
  }

  protected final Instruction[] throwNoSuitableFormatFound() {
    return throwCannotAssembleException("No suitable format of instruction found");
  }

  protected final Instruction[] throwWideRegistersExpected() {
    throw new InstructionAssemblyException("Wide registers badly aligned with instruction: " + getOriginalAssembly());
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

  static boolean formWideRegister(int reg1, int reg2) {
    return (reg1 + 1 == reg2);
  }
}
