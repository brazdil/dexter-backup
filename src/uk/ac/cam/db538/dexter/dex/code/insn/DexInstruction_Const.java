package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction11n;
import org.jf.dexlib.Code.Format.Instruction21h;
import org.jf.dexlib.Code.Format.Instruction21s;
import org.jf.dexlib.Code.Format.Instruction31i;

import uk.ac.cam.db538.dexter.analysis.coloring.ColorRange;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_AssemblingState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;

public class DexInstruction_Const extends DexInstruction {

  @Getter private final DexRegister regTo;
  @Getter private final long value;

  // CAREFUL: if Value is 32-bit and bottom 16-bits are zero,
  //          turn it into const/high16 instruction

  public DexInstruction_Const(DexCode methodCode, DexRegister to, long value) {
    super(methodCode);

    this.regTo = to;
    this.value = value;
  }

  public DexInstruction_Const(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) {
    super(methodCode);

    if (insn instanceof Instruction11n && insn.opcode == Opcode.CONST_4) {

      val insnConst4 = (Instruction11n) insn;
      regTo = parsingState.getRegister(insnConst4.getRegisterA());
      value = insnConst4.getLiteral();

    } else if (insn instanceof Instruction21s && insn.opcode == Opcode.CONST_16) {

      val insnConst16 = (Instruction21s) insn;
      regTo = parsingState.getRegister(insnConst16.getRegisterA());
      value = insnConst16.getLiteral();

    } else if (insn instanceof Instruction31i && insn.opcode == Opcode.CONST) {

      val insnConst = (Instruction31i) insn;
      regTo = parsingState.getRegister(insnConst.getRegisterA());
      value = insnConst.getLiteral();

    } else if (insn instanceof Instruction21h && insn.opcode == Opcode.CONST_HIGH16) {

      val insnConstHigh16 = (Instruction21h) insn;
      regTo = parsingState.getRegister(insnConstHigh16.getRegisterA());
      value = insnConstHigh16.getLiteral() << 16;

    } else
      throw FORMAT_EXCEPTION;
  }

  @Override
  public String getOriginalAssembly() {
    return "const v" + regTo.getOriginalIndexString() + ", #" + value;
  }

  @Override
  public DexCodeElement[] instrument(DexCode_InstrumentationState state) {
    return new DexCodeElement[] {
             this,
             new DexInstruction_Const(
               this.getMethodCode(),
               state.getTaintRegister(regTo),
               (value == 0xdec0ded) ? 1 : 0)
           };
  }

  @Override
  public Instruction[] assembleBytecode(DexCode_AssemblingState state) {
    int rTo = state.getRegisterAllocation().get(regTo);

    if (fitsIntoBits_Unsigned(rTo, 4) && fitsIntoBits_Signed(value, 4))
      return new Instruction[] { new Instruction11n(Opcode.CONST_4, (byte) rTo, (byte) value) };
    else if (fitsIntoBits_Unsigned(rTo, 8) && fitsIntoBits_Signed(value, 16))
      return new Instruction[] { new Instruction21s(Opcode.CONST_16, (short) rTo, (short) value) };
    else if (fitsIntoBits_Unsigned(rTo, 8) && fitsIntoHighBits_Signed(value, 16, 16))
      return new Instruction[] { new Instruction21h(Opcode.CONST_HIGH16, (short) rTo, (short) (value >> 16)) };
    else if (fitsIntoBits_Unsigned(rTo, 8) && fitsIntoBits_Signed(value, 32))
      return new Instruction[] { new Instruction31i(Opcode.CONST, (short) rTo, (int) value) };
    else
      return throwNoSuitableFormatFound();
  }

  @Override
  public Set<DexRegister> lvaDefinedRegisters() {
    return createSet(regTo);
  }

  @Override
  public Set<GcRangeConstraint> gcRangeConstraints() {
    return createSet(new GcRangeConstraint(regTo, ColorRange.RANGE_8BIT));
  }

  @Override
  protected DexCodeElement gcReplaceWithTemporaries(Map<DexRegister, DexRegister> mapping) {
    return new DexInstruction_Const(getMethodCode(), mapping.get(regTo), value);
  }
}
