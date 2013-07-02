package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Format.Instruction22t;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.elem.DexLabel;

public class DexInstruction_IfTest extends DexInstruction {

  @Getter private final DexRegister regA;
  @Getter private final DexRegister regB;
  @Getter private final DexLabel target;
  @Getter private final Opcode_IfTest insnOpcode;

  public DexInstruction_IfTest(DexCode methodCode, DexRegister regA, DexRegister regB, DexLabel target, Opcode_IfTest opcode) {
    super(methodCode);

    this.regA = regA;
    this.regB = regB;
    this.target = target;
    this.insnOpcode = opcode;
  }

  public DexInstruction_IfTest(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException {
    super(methodCode);

    if (insn instanceof Instruction22t && Opcode_IfTest.convert(insn.opcode) != null) {

      val insnIfTest = (Instruction22t) insn;
      regA = parsingState.getRegister(insnIfTest.getRegisterA());
      regB = parsingState.getRegister(insnIfTest.getRegisterB());
      target = parsingState.getLabel(insnIfTest.getTargetAddressOffset());
      insnOpcode = Opcode_IfTest.convert(insn.opcode);

    } else
      throw FORMAT_EXCEPTION;
  }

  @Override
  public String getOriginalAssembly() {
    return "if-" + insnOpcode.name() + " " + regA.getOriginalIndexString() +
           ", " + regB.getOriginalIndexString() + ", L" + target.getOriginalAbsoluteOffset();
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
    return createSet(regA, regB);
  }

  @Override
  public void instrument(DexCode_InstrumentationState state) { }


  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }
}
