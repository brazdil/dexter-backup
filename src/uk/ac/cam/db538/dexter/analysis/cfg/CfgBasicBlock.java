package uk.ac.cam.db538.dexter.analysis.cfg;

import java.util.Collections;
import java.util.List;

import uk.ac.cam.db538.dexter.dex.code.DexCodeElement;
import uk.ac.cam.db538.dexter.utils.NoDuplicatesList;

public class CfgBasicBlock extends CfgBlock {

  private final NoDuplicatesList<DexCodeElement> instructions;

  public CfgBasicBlock(NoDuplicatesList<DexCodeElement> instructions) {
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

  public List<DexCodeElement> getInstructions() {
    return Collections.unmodifiableList(instructions);
  }
}
