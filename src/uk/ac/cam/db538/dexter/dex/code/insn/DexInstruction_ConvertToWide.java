package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Format.Instruction12x;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;

public class DexInstruction_ConvertToWide extends DexInstruction {

  @Getter private final DexRegister regTo1;
  @Getter private final DexRegister regTo2;
  @Getter private final DexRegister regFrom;
  @Getter private final Opcode_ConvertToWide insnOpcode;

  public DexInstruction_ConvertToWide(DexCode methodCode, DexRegister to1, DexRegister to2, DexRegister from, Opcode_ConvertToWide opcode) {
    super(methodCode);

    regTo1 = to1;
    regTo2 = to2;
    regFrom = from;
    insnOpcode = opcode;
  }

  public DexInstruction_ConvertToWide(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException {
    super(methodCode);

    if (insn instanceof Instruction12x && Opcode_ConvertToWide.convert(insn.opcode) != null) {

      val insnConvert = (Instruction12x) insn;
      regTo1 = parsingState.getRegister(insnConvert.getRegisterA());
      regTo2 = parsingState.getRegister(insnConvert.getRegisterA() + 1);
      regFrom = parsingState.getRegister(insnConvert.getRegisterB());
      insnOpcode = Opcode_ConvertToWide.convert(insn.opcode);

    } else
      throw FORMAT_EXCEPTION;
  }

  @Override
  public String getOriginalAssembly() {
    return insnOpcode.getAssemblyName() + " " + regTo1.getOriginalIndexString()
           + "|" + regTo2.getOriginalIndexString()
           + ", " + regFrom.getOriginalIndexString();
  }

  @Override
  public void instrument(DexCode_InstrumentationState state) {
    // copy taint of the original value to both the result registers
    val code = getMethodCode();
    code.replace(this,
                 new DexCodeElement[] {
                   this,
                   new DexInstruction_Move(code, state.getTaintRegister(regTo1), state.getTaintRegister(regFrom), false)
                 });
  }

  @Override
  public Set<DexRegister> lvaDefinedRegisters() {
    return createSet(regTo1, regTo2);
  }

  @Override
  public Set<DexRegister> lvaReferencedRegisters() {
    return createSet(regFrom);
  }

  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }
}
