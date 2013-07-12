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

public class DexInstruction_Convert extends DexInstruction {

  @Getter private final DexRegister regTo;
  @Getter private final DexRegister regFrom;
  @Getter private final Opcode_Convert insnOpcode;

  public DexInstruction_Convert(DexCode methodCode, DexRegister to, DexRegister from, Opcode_Convert opcode) {
    super(methodCode);

    regTo = to;
    regFrom = from;
    insnOpcode = opcode;
  }

  public DexInstruction_Convert(DexCode methodCode, Instruction insn, CodeParserState parsingState) throws InstructionParseError {
    super(methodCode);

    if (insn instanceof Instruction12x && Opcode_Convert.convert(insn.opcode) != null) {

      val insnConvert = (Instruction12x) insn;
      regTo = parsingState.getRegister(insnConvert.getRegisterA());
      regFrom = parsingState.getRegister(insnConvert.getRegisterB());
      insnOpcode = Opcode_Convert.convert(insn.opcode);

    } else
      throw FORMAT_EXCEPTION;
  }

  @Override
  public String getOriginalAssembly() {
    return insnOpcode.getAssemblyName() + " " + regTo.getOriginalIndexString() + ", " + regFrom.getOriginalIndexString();
  }

  @Override
  public void instrument(DexCode_InstrumentationState state) {
    // need to copy to taint across
    val code = getMethodCode();
    code.replace(this,
                 new DexCodeElement[] {
                   this,
                   new DexInstruction_Move(code, state.getTaintRegister(regTo), state.getTaintRegister(regFrom), false)
                 });
  }

  @Override
  public Set<? extends DexRegister> lvaDefinedRegisters() {
    return Sets.newHashSet(regTo);
  }

  @Override
  public Set<? extends DexRegister> lvaReferencedRegisters() {
    return Sets.newHashSet(regFrom);
  }

  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }
}
