package uk.ac.cam.db538.dexter.analysis.cfg;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.val;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.utils.InstructionList;

public class CfgBasicBlock extends CfgBlock {

  private final InstructionList instructions;
  @Getter private final int blockStartIndex, blockEndIndex;

  public CfgBasicBlock(DexCode code, InstructionList instructions) {
    if (instructions == null || instructions.isEmpty())
      throw new UnsupportedOperationException("BasicBlock must contain at least one instruction");

    this.instructions = instructions;
    blockStartIndex = code.getInstructionList().indexOf(getFirstInstruction());
    blockEndIndex = code.getInstructionList().indexOf(getLastInstruction());
  }

  public DexCodeElement getFirstInstruction() {
    return instructions.peekFirst();
  }

  public DexCodeElement getLastInstruction() {
    return instructions.peekLast();
  }

  public List<DexCodeElement> getInstructions() {
    return Collections.unmodifiableList(instructions);
  }

  public Set<DexRegister> getAllDefinedRegisters() {
    val set = new HashSet<DexRegister>();
    for (val insn : instructions)
      set.addAll(insn.lvaDefinedRegisters());
    return set;
  }
}
