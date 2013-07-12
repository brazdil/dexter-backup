package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Format.Instruction22t;

import com.google.common.collect.Sets;

import uk.ac.cam.db538.dexter.dex.code.CodeParserState;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.elem.DexLabel;
import uk.ac.cam.db538.dexter.dex.code.reg.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.reg.DexSingleRegister;
import uk.ac.cam.db538.dexter.hierarchy.RuntimeHierarchy;

public class DexInstruction_IfTest extends DexInstruction {

  @Getter private final DexSingleRegister regA;
  @Getter private final DexSingleRegister regB;
  @Getter private final DexLabel target;
  @Getter private final Opcode_IfTest insnOpcode;

  public DexInstruction_IfTest(DexSingleRegister regA, DexSingleRegister regB, DexLabel target, Opcode_IfTest opcode, RuntimeHierarchy hierarchy) {
    super(hierarchy);

    this.regA = regA;
    this.regB = regB;
    this.target = target;
    this.insnOpcode = opcode;
  }

  public static DexInstruction_IfTest parse(Instruction insn, CodeParserState parsingState) throws InstructionParseError {
    if (insn instanceof Instruction22t && Opcode_IfTest.convert(insn.opcode) != null) {

      val insnIfTest = (Instruction22t) insn;
      return new DexInstruction_IfTest(
    		  parsingState.getSingleRegister(insnIfTest.getRegisterA()),
    		  parsingState.getSingleRegister(insnIfTest.getRegisterB()),
    		  parsingState.getLabel(insnIfTest.getTargetAddressOffset()),
    		  Opcode_IfTest.convert(insn.opcode),
    		  parsingState.getHierarchy());

    } else
      throw FORMAT_EXCEPTION;
  }

  @Override
  public String toString() {
    return "if-" + insnOpcode.name() + " " + regA.toString() +
           ", " + regB.toString() + ", L" + target.toString();
  }

  @Override
  public boolean cfgEndsBasicBlock() {
    return true;
  }

  @Override
  public Set<? extends DexCodeElement> cfgJumpTargets(DexCode code) {
	return Sets.newHashSet(target, code.getFollower(this));
  }

  @Override
  public Set<? extends DexRegister> lvaReferencedRegisters() {
    return Sets.newHashSet(regA, regB);
  }

  @Override
  public void instrument(DexCode_InstrumentationState state) { }


  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }
}
