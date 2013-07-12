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

public class DexInstruction_UnaryOp extends DexInstruction {

  @Getter private final DexRegister regTo;
  @Getter private final DexRegister regFrom;
  @Getter private final Opcode_UnaryOp insnOpcode;

  public DexInstruction_UnaryOp(DexCode methodCode, DexRegister to, DexRegister from, Opcode_UnaryOp opcode) {
    super(methodCode);
    regTo = to;
    regFrom = from;
    insnOpcode = opcode;
  }

  public DexInstruction_UnaryOp(DexCode methodCode, Instruction insn, CodeParserState parsingState) {
    super(methodCode);
    if (insn instanceof Instruction12x && Opcode_UnaryOp.convert(insn.opcode) != null) {

      val insnUnaryOp = (Instruction12x) insn;
      regTo = parsingState.getRegister(insnUnaryOp.getRegisterA());
      regFrom = parsingState.getRegister(insnUnaryOp.getRegisterB());
      insnOpcode = Opcode_UnaryOp.convert(insn.opcode);

    } else
      throw FORMAT_EXCEPTION;
  }

  @Override
  public String getOriginalAssembly() {
    return insnOpcode.getAssemblyName() + " " + regTo.getOriginalIndexString() + ", " + regFrom.getOriginalIndexString();
  }

  @Override
  public void instrument(DexCode_InstrumentationState state) {
    val code = getMethodCode();
    code.replace(this, new DexCodeElement[] {
                   this,
                   new DexInstruction_Move(code, state.getTaintRegister(regTo), state.getTaintRegister(regFrom), false)
                 });
  }

  @Override
  public Set<? extends uk.ac.cam.db538.dexter.dex.code.reg.DexRegister> lvaDefinedRegisters() {
    return createSet(regTo);
  }

  @Override
  public Set<? extends uk.ac.cam.db538.dexter.dex.code.reg.DexRegister> lvaReferencedRegisters() {
    return createSet(regFrom);
  }

  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }
}
