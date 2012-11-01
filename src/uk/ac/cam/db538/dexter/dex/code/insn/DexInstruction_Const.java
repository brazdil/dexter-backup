package uk.ac.cam.db538.dexter.dex.code.insn;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction11n;
import org.jf.dexlib.Code.Format.Instruction21h;
import org.jf.dexlib.Code.Format.Instruction21s;
import org.jf.dexlib.Code.Format.Instruction31i;

import uk.ac.cam.db538.dexter.dex.code.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;

import lombok.Getter;
import lombok.val;

public class DexInstruction_Const extends DexInstruction {

  @Getter private final DexRegister RegTo;
  @Getter private final long Value;

  // CAREFUL: if Value is 32-bit and bottom 16-bits are zero,
  //          turn it into const/high16 instruction

  public DexInstruction_Const(DexRegister to, long value) {
    RegTo = to;
    Value = value;
  }

  public DexInstruction_Const(Instruction insn, InstructionParsingState parsingState) throws DexInstructionParsingException {
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
      throw new DexInstructionParsingException("Unknown instruction format or opcode");
  }

  @Override
  public String getOriginalAssembly() {
    return "const v" + RegTo.getId() + ", #" + Value;
  }

  @Override
  public DexCodeElement[] instrument(TaintRegisterMap mapping) {
    return new DexCodeElement[] {
             this,
             new DexInstruction_Const(
               mapping.getTaintRegister(RegTo),
               (Value == 0xdec0ded) ? 1 : 0)
           };
  }

  @Override
  protected DexRegister[] getReferencedRegisters() {
    return new DexRegister[] { RegTo };
  }
}
