package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction11n;
import org.jf.dexlib.Code.Format.Instruction21h;
import org.jf.dexlib.Code.Format.Instruction21s;
import org.jf.dexlib.Code.Format.Instruction31i;
import org.jf.dexlib.Code.Format.Instruction51l;

import uk.ac.cam.db538.dexter.dex.code.CodeParserState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.reg.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.reg.RegisterWidth;

import com.google.common.collect.Sets;

public class DexInstruction_Const extends DexInstruction {

  @Getter private final DexRegister regTo;
  @Getter private final long value;

  // CAREFUL: if Value is 32-bit and bottom 16-bits are zero,
  //          turn it into const/high16 instruction

  public DexInstruction_Const(DexRegister to, long value) {
    this.regTo = to;
    this.value = value;
    
    if (this.regTo.getWidth() == RegisterWidth.SINGLE &&
    	!fitsIntoBits_Signed(this.value, 32))
    	throw new Error("Constant too big for a single-width const instruction");
  }

  public DexInstruction_Const(Instruction insn, CodeParserState parsingState) {
    if (insn instanceof Instruction11n && insn.opcode == Opcode.CONST_4) {

      val insnConst4 = (Instruction11n) insn;
      regTo = parsingState.getSingleRegister(insnConst4.getRegisterA());
      value = insnConst4.getLiteral();

    } else if (insn instanceof Instruction21s && insn.opcode == Opcode.CONST_16) {

      val insnConst16 = (Instruction21s) insn;
      regTo = parsingState.getSingleRegister(insnConst16.getRegisterA());
      value = insnConst16.getLiteral();

    } else if (insn instanceof Instruction31i && insn.opcode == Opcode.CONST) {

      val insnConst = (Instruction31i) insn;
      regTo = parsingState.getSingleRegister(insnConst.getRegisterA());
      value = insnConst.getLiteral();

    } else if (insn instanceof Instruction21h && insn.opcode == Opcode.CONST_HIGH16) {

      val insnConstHigh16 = (Instruction21h) insn;
      regTo = parsingState.getSingleRegister(insnConstHigh16.getRegisterA());
      value = insnConstHigh16.getLiteral() << 16;

    } else if (insn instanceof Instruction21s && insn.opcode == Opcode.CONST_WIDE_16) {

      val insnConstWide16 = (Instruction21s) insn;
      regTo = parsingState.getWideRegister(insnConstWide16.getRegisterA());
      value = insnConstWide16.getLiteral();

    } else if (insn instanceof Instruction31i && insn.opcode == Opcode.CONST_WIDE_32) {

      val insnConstWide32 = (Instruction31i) insn;
      regTo = parsingState.getWideRegister(insnConstWide32.getRegisterA());
      value = insnConstWide32.getLiteral();

    } else if (insn instanceof Instruction51l && insn.opcode == Opcode.CONST_WIDE) {

      val insnConstWide = (Instruction51l) insn;
      regTo = parsingState.getWideRegister(insnConstWide.getRegisterA());
      value = insnConstWide.getLiteral();

    } else if (insn instanceof Instruction21h && insn.opcode == Opcode.CONST_WIDE_HIGH16) {

      val insnConstHigh16 = (Instruction21h) insn;
      regTo = parsingState.getWideRegister(insnConstHigh16.getRegisterA());
      value = insnConstHigh16.getLiteral() << 48;

    } else
      throw FORMAT_EXCEPTION;
  }

  @Override
  public String toString() {
	  if (this.regTo.getWidth() == RegisterWidth.SINGLE)
		  return "const " + regTo.toString() + ", #" + value;
	  else
		  return "const-wide " + regTo.toString() + ", #" + value;
  }

  @Override
  public void instrument(DexCode_InstrumentationState state) {
//    getMethodCode().replace(this,
//                            new DexCodeElement[] {
//                              this,
//                              new DexInstruction_Const(
//                                this.getMethodCode(),
//                                state.getTaintRegister(regTo),
//                                (value == 0xdec0ded) ? 1 : 0)
//                            });
  }

  @Override
  public Set<DexRegister> lvaDefinedRegisters() {
    return Sets.newHashSet(regTo);
  }

  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }
}
