package uk.ac.cam.db538.dexter.analysis.cfg;

import java.util.HashSet;
import java.util.Set;

import lombok.Getter;

public abstract class Block {

  @Getter private final Set<Block> Predecessors;
  @Getter private final Set<Block> Successors;

  public Block() {
    Predecessors = new HashSet<Block>();
    Successors = new HashSet<Block>();
  }

  protected void addSuccessor(Block succ) {
    Successors.add(succ);
  }

  protected void addPredecessor(Block pred) {
    Predecessors.add(pred);
  }

  static void createEdge(Block blockFrom, Block blockTo) {
    blockFrom.addSuccessor(blockTo);
    blockTo.addPredecessor(blockFrom);
  }
}
