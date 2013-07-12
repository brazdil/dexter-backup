package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Format.Instruction21t;

import com.google.common.collect.Sets;

import uk.ac.cam.db538.dexter.dex.code.CodeParserState;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.elem.DexLabel;
import uk.ac.cam.db538.dexter.dex.code.reg.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.reg.DexSingleRegister;
import uk.ac.cam.db538.dexter.hierarchy.RuntimeHierarchy;

public class DexInstruction_IfTestZero extends DexInstruction {

  @Getter private final DexSingleRegister reg;
  @Getter private final DexLabel target;
  @Getter private final Opcode_IfTestZero insnOpcode;

  public DexInstruction_IfTestZero(DexSingleRegister reg, DexLabel target, Opcode_IfTestZero opcode, RuntimeHierarchy hierarchy) {
    super(hierarchy);

    this.reg = reg;
    this.target = target;
    this.insnOpcode = opcode;
  }

  public static DexInstruction_IfTestZero parse(Instruction insn, CodeParserState parsingState) {
    if (insn instanceof Instruction21t && Opcode_IfTestZero.convert(insn.opcode) != null) {

      val insnIfTestZero = (Instruction21t) insn;
      return new DexInstruction_IfTestZero(
    		  parsingState.getSingleRegister(insnIfTestZero.getRegisterA()),
    		  parsingState.getLabel(insnIfTestZero.getTargetAddressOffset()),
    		  Opcode_IfTestZero.convert(insn.opcode),
    		  parsingState.getHierarchy());

    } else
      throw FORMAT_EXCEPTION;
  }

  @Override
  public String toString() {
    return "if-" + insnOpcode.name() + " " + reg.toString() + ", " + target.toString();
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
    return Sets.newHashSet(reg);
  }

  @Override
  public void instrument(DexCode_InstrumentationState state) { }


  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }
}
