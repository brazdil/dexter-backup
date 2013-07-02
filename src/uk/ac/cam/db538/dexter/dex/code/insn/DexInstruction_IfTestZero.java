package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Format.Instruction21t;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.elem.DexLabel;

public class DexInstruction_IfTestZero extends DexInstruction {

  @Getter private final DexRegister reg;
  @Getter private final DexLabel target;
  @Getter private final Opcode_IfTestZero insnOpcode;

  public DexInstruction_IfTestZero(DexCode methodCode, DexRegister reg, DexLabel target, Opcode_IfTestZero opcode) {
    super(methodCode);

    this.reg = reg;
    this.target = target;
    this.insnOpcode = opcode;
  }

  public DexInstruction_IfTestZero(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException {
    super(methodCode);

    if (insn instanceof Instruction21t && Opcode_IfTestZero.convert(insn.opcode) != null) {

      val insnIfTestZero = (Instruction21t) insn;
      reg = parsingState.getRegister(insnIfTestZero.getRegisterA());
      target = parsingState.getLabel(insnIfTestZero.getTargetAddressOffset());
      insnOpcode = Opcode_IfTestZero.convert(insn.opcode);

    } else
      throw FORMAT_EXCEPTION;
  }

  @Override
  public String getOriginalAssembly() {
    return "if-" + insnOpcode.name() + " " + reg.getOriginalIndexString() +
           ", L" + target.getOriginalAbsoluteOffset();
  }

  @Override
  public boolean cfgEndsBasicBlock() {
    return true;
  }

  @Override
  public Set<DexCodeElement> cfgJumpTargets() {
	val set = createSet((DexCodeElement) target);
	val next = this.getNextCodeElement();
	if (next != null)
		set.add(next);
	return set;
  }
  @Override
  public Set<DexRegister> lvaReferencedRegisters() {
    return createSet(reg);
  }

  @Override
  public void instrument(DexCode_InstrumentationState state) { }


  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }
}
