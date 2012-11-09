package uk.ac.cam.db538.dexter.analysis.cfg;

import lombok.Getter;
import uk.ac.cam.db538.dexter.dex.code.DexCodeElement;
import uk.ac.cam.db538.dexter.utils.NoDuplicatesList;

public class BasicBlock extends Block {

  @Getter private final NoDuplicatesList<DexCodeElement> Instructions;

  public BasicBlock(NoDuplicatesList<DexCodeElement> instructions) {
    if (instructions == null || instructions.isEmpty())
      throw new UnsupportedOperationException("BasicBlock must contain at least one instruction");

    Instructions = instructions;
  }

  public DexCodeElement getFirstInstruction() {
    return Instructions.peekFirst();
  }

  public DexCodeElement getLastInstruction() {
    return Instructions.peekLast();
  }

}
