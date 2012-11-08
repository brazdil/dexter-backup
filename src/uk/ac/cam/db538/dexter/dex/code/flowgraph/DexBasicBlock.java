package uk.ac.cam.db538.dexter.dex.code.flowgraph;

import java.util.HashSet;
import java.util.Set;

import lombok.Getter;

public abstract class DexBasicBlock {

  @Getter private final Set<DexBasicBlock> Predecessors;
  @Getter private final Set<DexBasicBlock> Successors;

  public DexBasicBlock() {
    Predecessors = new HashSet<DexBasicBlock>();
    Successors = new HashSet<DexBasicBlock>();
  }

  static void linkBlocks(DexBasicBlock blockFrom, DexBasicBlock blockTo) {
    blockFrom.Successors.add(blockTo);
    blockTo.Predecessors.add(blockFrom);
  }
}
