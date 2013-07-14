package uk.ac.cam.db538.dexter.analysis.cfg;

import java.util.HashSet;
import java.util.Set;

import lombok.Getter;
import lombok.val;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.InstructionList;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.reg.DexRegister;

public class CfgBasicBlock extends CfgBlock {

  @Getter private final InstructionList instructions;

  public CfgBasicBlock(DexCode code, InstructionList instructions) {
    if (instructions == null || instructions.isEmpty())
      throw new UnsupportedOperationException("BasicBlock must contain at least one instruction");

    this.instructions = instructions;
  }

  public DexCodeElement getFirstInstruction() {
    return instructions.peekFirst();
  }

  public DexCodeElement getLastInstruction() {
    return instructions.peekLast();
  }

  public Set<DexRegister> getAllDefinedRegisters() {
    val set = new HashSet<DexRegister>();
    for (val insn : instructions)
      set.addAll(insn.lvaDefinedRegisters());
    return set;
  }
}
