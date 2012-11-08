package uk.ac.cam.db538.dexter.dex.code.cfg;

import java.util.HashSet;
import java.util.Set;

import lombok.Getter;

public abstract class DexBlock {

  @Getter private final Set<DexBlock> Predecessors;
  @Getter private final Set<DexBlock> Successors;

  public DexBlock() {
    Predecessors = new HashSet<DexBlock>();
    Successors = new HashSet<DexBlock>();
  }

  protected void addSuccessor(DexBlock succ) {
	  Successors.add(succ);
  }
  
  protected void addPredecessor(DexBlock pred) {
	  Predecessors.add(pred);
  }

  static void linkBlocks(DexBlock blockFrom, DexBlock blockTo) {
	  blockFrom.addSuccessor(blockTo);
	  blockTo.addPredecessor(blockFrom);
  }
}
