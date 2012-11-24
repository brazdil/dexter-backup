package uk.ac.cam.db538.dexter.analysis.cfg;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class CfgBlock {

  private final Set<CfgBlock> predecessors;
  private final Set<CfgBlock> successors;

  public CfgBlock() {
    predecessors = new HashSet<CfgBlock>();
    successors = new HashSet<CfgBlock>();
  }

  protected void addSuccessor(CfgBlock succ) {
    successors.add(succ);
  }

  protected void addPredecessor(CfgBlock pred) {
    predecessors.add(pred);
  }

  static void createEdge(CfgBlock blockFrom, CfgBlock blockTo) {
    blockFrom.addSuccessor(blockTo);
    blockTo.addPredecessor(blockFrom);
  }

  public Set<CfgBlock> getPredecessors() {
    return Collections.unmodifiableSet(predecessors);
  }

  public Set<CfgBlock> getSuccessors() {
    return Collections.unmodifiableSet(successors);
  }
}
