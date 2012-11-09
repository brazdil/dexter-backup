package uk.ac.cam.db538.dexter.analysis.cfg;

import java.util.HashSet;
import java.util.Set;

import lombok.Getter;

public abstract class CfgBlock {

  @Getter private final Set<CfgBlock> Predecessors;
  @Getter private final Set<CfgBlock> Successors;

  public CfgBlock() {
    Predecessors = new HashSet<CfgBlock>();
    Successors = new HashSet<CfgBlock>();
  }

  protected void addSuccessor(CfgBlock succ) {
    Successors.add(succ);
  }

  protected void addPredecessor(CfgBlock pred) {
    Predecessors.add(pred);
  }

  static void createEdge(CfgBlock blockFrom, CfgBlock blockTo) {
    blockFrom.addSuccessor(blockTo);
    blockTo.addPredecessor(blockFrom);
  }
}
