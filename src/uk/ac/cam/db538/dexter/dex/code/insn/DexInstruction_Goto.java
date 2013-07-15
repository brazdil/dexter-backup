package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.Set;

import lombok.Getter;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction10t;
import org.jf.dexlib.Code.Format.Instruction20t;
import org.jf.dexlib.Code.Format.Instruction30t;

import uk.ac.cam.db538.dexter.dex.code.CodeParserState;
import uk.ac.cam.db538.dexter.dex.code.InstructionList;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.elem.DexLabel;
import uk.ac.cam.db538.dexter.hierarchy.RuntimeHierarchy;

import com.google.common.collect.Sets;

public class DexInstruction_Goto extends DexInstruction {

  @Getter private final DexLabel target;

  public DexInstruction_Goto(DexLabel target, RuntimeHierarchy hierarchy) {
    super(hierarchy);

    this.target = target;
  }

  public static DexInstruction_Goto parse(Instruction insn, CodeParserState parsingState) {
    long targetOffset;
    if (insn instanceof Instruction10t && insn.opcode == Opcode.GOTO) {
      targetOffset = ((Instruction10t) insn).getTargetAddressOffset();
    } else if (insn instanceof Instruction20t && insn.opcode == Opcode.GOTO_16) {
      targetOffset = ((Instruction20t) insn).getTargetAddressOffset();
    } else if (insn instanceof Instruction30t && insn.opcode == Opcode.GOTO_32) {
      targetOffset = ((Instruction30t) insn).getTargetAddressOffset();
    } else
      throw FORMAT_EXCEPTION;

    return new DexInstruction_Goto(
    		parsingState.getLabel(targetOffset, insn),
    		parsingState.getHierarchy());
  }

  @Override
  public String toString() {
    return "goto L" + target.toString();
  }

  @Override
  public boolean cfgEndsBasicBlock() {
    return true;
  }

  @Override
  public Set<? extends DexCodeElement> cfgJumpTargets(InstructionList code) {
	  return Sets.newHashSet(target);
  }

  @Override
  public void instrument() { }

  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }
}
