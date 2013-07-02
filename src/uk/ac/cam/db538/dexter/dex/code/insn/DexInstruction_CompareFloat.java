package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction23x;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;

public class DexInstruction_CompareFloat extends DexInstruction {

  @Getter private final DexRegister regTo;
  @Getter private final DexRegister regSourceA;
  @Getter private final DexRegister regSourceB;
  @Getter private final boolean ltBias;

  public DexInstruction_CompareFloat(DexCode methodCode, DexRegister target, DexRegister sourceA, DexRegister sourceB, boolean ltBias) {
    super(methodCode);

    this.regTo = target;
    this.regSourceA = sourceA;
    this.regSourceB = sourceB;
    this.ltBias = ltBias;
  }

  public DexInstruction_CompareFloat(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException {
    super(methodCode);

    if (insn instanceof Instruction23x &&
        (insn.opcode == Opcode.CMPL_FLOAT || insn.opcode == Opcode.CMPG_FLOAT)) {

      val insnCompare = (Instruction23x) insn;
      this.regTo = parsingState.getRegister(insnCompare.getRegisterA());
      this.regSourceA = parsingState.getRegister(insnCompare.getRegisterB());
      this.regSourceB = parsingState.getRegister(insnCompare.getRegisterC());
      this.ltBias = insnCompare.opcode == Opcode.CMPL_FLOAT;

    } else
      throw FORMAT_EXCEPTION;
  }

  @Override
  public String getOriginalAssembly() {
    return (ltBias ? "cmpl" : "cmpg") + "-float " + regTo.getOriginalIndexString() +
           ", " + regSourceA.getOriginalIndexString() + ", " + regSourceB.getOriginalIndexString();
  }

  @Override
  public Set<DexRegister> lvaDefinedRegisters() {
    return createSet(regTo);
  }

  @Override
  public Set<DexRegister> lvaReferencedRegisters() {
    return createSet(regSourceA, regSourceB);
  }

  @Override
  public void instrument(DexCode_InstrumentationState state) {
    // need to combine the taint of the two compared registers and assign that to the operation result
    val code = getMethodCode();
    code.replace(this,
                 new DexCodeElement[] {
                   this,
                   new DexInstruction_BinaryOp(code, state.getTaintRegister(regTo), state.getTaintRegister(regSourceA), state.getTaintRegister(regSourceB), Opcode_BinaryOp.OrInt),
                 });
  }

  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }
}
