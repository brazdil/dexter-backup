package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Format.Instruction12x;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.CodeParserState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;

public class DexInstruction_ConvertFromWide extends DexInstruction {

  @Getter private final DexRegister regTo;
  @Getter private final DexRegister regFrom1;
  @Getter private final DexRegister regFrom2;
  @Getter private final Opcode_ConvertFromWide insnOpcode;

  public DexInstruction_ConvertFromWide(DexCode methodCode, DexRegister to, DexRegister from1, DexRegister from2, Opcode_ConvertFromWide opcode) {
    super(methodCode);

    regTo = to;
    regFrom1 = from1;
    regFrom2 = from2;
    insnOpcode = opcode;
  }

  public DexInstruction_ConvertFromWide(DexCode methodCode, Instruction insn, CodeParserState parsingState) throws InstructionParseError {
    super(methodCode);

    if (insn instanceof Instruction12x && Opcode_ConvertFromWide.convert(insn.opcode) != null) {

      val insnConvert = (Instruction12x) insn;
      regTo = parsingState.getRegister(insnConvert.getRegisterA());
      regFrom1 = parsingState.getRegister(insnConvert.getRegisterB());
      regFrom2 = parsingState.getRegister(insnConvert.getRegisterB() + 1);
      insnOpcode = Opcode_ConvertFromWide.convert(insn.opcode);

    } else
      throw FORMAT_EXCEPTION;
  }

  @Override
  public String toString() {
    return insnOpcode.getAssemblyName() + " " + regTo.toString() + ", " + regFrom1.toString() + "|" + regFrom2.toString();
  }

  @Override
  public void instrument(DexCode_InstrumentationState state) {
    // need to combine the taint of the two wide registers and assign it the operation result
    val code = getMethodCode();
    code.replace(this,
                 new DexCodeElement[] {
                   this,
                   new DexInstruction_Move(code, state.getTaintRegister(regTo), state.getTaintRegister(regFrom1), false)
                 });
  }

  @Override
  public Set<? extends DexRegister> lvaDefinedRegisters() {
    return Sets.newHashSet(regTo);
  }

  @Override
  public Set<? extends DexRegister> lvaReferencedRegisters() {
    return Sets.newHashSet(regFrom1, regFrom2);
  }

  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }
}
