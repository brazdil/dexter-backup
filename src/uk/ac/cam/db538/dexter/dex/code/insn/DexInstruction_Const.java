package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.HashSet;
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

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;

public class DexInstruction_Const extends DexInstruction {

  @Getter private final DexRegister RegTo;
  @Getter private final long Value;

  // CAREFUL: if Value is 32-bit and bottom 16-bits are zero,
  //          turn it into const/high16 instruction

  public DexInstruction_Const(DexCode methodCode, DexRegister to, long value) {
    super(methodCode);

    RegTo = to;
    Value = value;
  }

  public DexInstruction_Const(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) {
    super(methodCode);

    if (insn instanceof Instruction11n && insn.opcode == Opcode.CONST_4) {

      val insnConst4 = (Instruction11n) insn;
      RegTo = parsingState.getRegister(insnConst4.getRegisterA());
      Value = insnConst4.getLiteral();

    } else if (insn instanceof Instruction21s && insn.opcode == Opcode.CONST_16) {

      val insnConst16 = (Instruction21s) insn;
      RegTo = parsingState.getRegister(insnConst16.getRegisterA());
      Value = insnConst16.getLiteral();

    } else if (insn instanceof Instruction31i && insn.opcode == Opcode.CONST) {

      val insnConst = (Instruction31i) insn;
      RegTo = parsingState.getRegister(insnConst.getRegisterA());
      Value = insnConst.getLiteral();

    } else if (insn instanceof Instruction21h && insn.opcode == Opcode.CONST_HIGH16) {

      val insnConstHigh16 = (Instruction21h) insn;
      RegTo = parsingState.getRegister(insnConstHigh16.getRegisterA());
      Value = insnConstHigh16.getLiteral() << 16;

    } else
      throw new InstructionParsingException("Unknown instruction format or opcode");
  }

  @Override
  public String getOriginalAssembly() {
    return "const v" + RegTo.getId() + ", #" + Value;
  }

  @Override
  public DexCodeElement[] instrument(DexCode_InstrumentationState mapping) {
    return new DexCodeElement[] {
             this,
             new DexInstruction_Const(
               this.getMethodCode(),
               mapping.getTaintRegister(RegTo),
               (Value == 0xdec0ded) ? 1 : 0)
           };
  }

  @Override
  public Instruction[] assembleBytecode(Map<DexRegister, Integer> regAlloc) {
    int rTo = regAlloc.get(RegTo);

    if (fitsIntoBits_Unsigned(rTo, 4) && fitsIntoBits_Signed(Value, 4))
      return new Instruction[] { new Instruction11n(Opcode.CONST_4, (byte) rTo, (byte) Value) };
    else if (fitsIntoBits_Unsigned(rTo, 8) && fitsIntoBits_Signed(Value, 16))
      return new Instruction[] { new Instruction21s(Opcode.CONST_16, (short) rTo, (short) Value) };
    else if (fitsIntoBits_Unsigned(rTo, 8) && fitsIntoHighBits_Signed(Value, 16, 16))
      return new Instruction[] { new Instruction21h(Opcode.CONST_HIGH16, (short) rTo, (short) (Value >> 16)) };
    else if (fitsIntoBits_Unsigned(rTo, 8) && fitsIntoBits_Signed(Value, 32))
      return new Instruction[] { new Instruction31i(Opcode.CONST, (short) rTo, (int) Value) };
    else
      return throwCannotAssembleException();
  }

  @Override
  public Set<DexRegister> lvaDefinedRegisters() {
    val regs = new HashSet<DexRegister>();
    regs.add(RegTo);
    return regs;
  }
}
