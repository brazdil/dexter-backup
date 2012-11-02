package uk.ac.cam.db538.dexter.dex.code.insn;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction21h;
import org.jf.dexlib.Code.Format.Instruction21s;
import org.jf.dexlib.Code.Format.Instruction31i;
import org.jf.dexlib.Code.Format.Instruction51l;

import uk.ac.cam.db538.dexter.dex.code.DexRegister;

import lombok.Getter;
import lombok.val;

public class DexInstruction_ConstWide extends DexInstruction {

  @Getter private final DexRegister RegTo1;
  @Getter private final DexRegister RegTo2;
  @Getter private final long Value;

  // CAREFUL: if Value is 64-bit and bottom 48-bits are zero,
  //          turn it into const-wide/high16 instruction

  public DexInstruction_ConstWide(DexRegister to1, DexRegister to2, long value) {
    RegTo1 = to1;
    RegTo2 = to2;
    Value = value;
  }

  public DexInstruction_ConstWide(Instruction insn, ParsingState parsingState) throws InstructionParsingException {
    if (insn instanceof Instruction21s && insn.opcode == Opcode.CONST_WIDE_16) {

      val insnConstWide16 = (Instruction21s) insn;
      RegTo1 = parsingState.getRegister(insnConstWide16.getRegisterA());
      RegTo2 = parsingState.getRegister(insnConstWide16.getRegisterA() + 1);
      Value = insnConstWide16.getLiteral();

    } else if (insn instanceof Instruction31i && insn.opcode == Opcode.CONST_WIDE_32) {

      val insnConstWide32 = (Instruction31i) insn;
      RegTo1 = parsingState.getRegister(insnConstWide32.getRegisterA());
      RegTo2 = parsingState.getRegister(insnConstWide32.getRegisterA() + 1);
      Value = insnConstWide32.getLiteral();

    } else if (insn instanceof Instruction51l && insn.opcode == Opcode.CONST_WIDE) {

      val insnConstWide = (Instruction51l) insn;
      RegTo1 = parsingState.getRegister(insnConstWide.getRegisterA());
      RegTo2 = parsingState.getRegister(insnConstWide.getRegisterA() + 1);
      Value = insnConstWide.getLiteral();

    } else if (insn instanceof Instruction21h && insn.opcode == Opcode.CONST_WIDE_HIGH16) {

      val insnConstHigh16 = (Instruction21h) insn;
      RegTo1 = parsingState.getRegister(insnConstHigh16.getRegisterA());
      RegTo2 = parsingState.getRegister(insnConstHigh16.getRegisterA() + 1);
      Value = insnConstHigh16.getLiteral() << 48;

    } else
      throw new InstructionParsingException("Unknown instruction format or opcode");
  }

  @Override
  public String getOriginalAssembly() {
    return "const-wide v" + RegTo1.getId() + ", #" + Value;
  }
}
