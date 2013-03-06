package uk.ac.cam.db538.dexter.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.val;
import uk.ac.cam.db538.dexter.analysis.cfg.CfgBasicBlock;
import uk.ac.cam.db538.dexter.analysis.cfg.ControlFlowGraph;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.utils.ListReverser;

public class DominanceAnalysis {

  @Getter private final DexCode code;
  @Getter private ControlFlowGraph cfg;
  private Map<CfgBasicBlock, CfgBasicBlock> dominance;

  public DominanceAnalysis(DexCode code) {
    this.code = code;
    generateDominance();
  }

  private void generateDominance() {
    dominance = new HashMap<>();
    cfg = new ControlFlowGraph(code);
    val listAllBlocks = Collections.unmodifiableList(cfg.getBasicBlocks());

    // initialize the first block
    val startBlockSucc = cfg.getStartBlock().getSuccessors();
    if (startBlockSucc.size() != 1)
      throw new RuntimeException("ControlFlowGraph has multiple starting points");
    if (!(startBlockSucc.iterator().next() instanceof CfgBasicBlock))
      return;
    val startBasicBlock = (CfgBasicBlock) startBlockSucc.iterator().next();
    dominance.put(startBasicBlock, startBasicBlock);

    boolean somethingChanged;
    do {
      somethingChanged = false;

      for (val block : new ListReverser<CfgBasicBlock>(blocksInPostOrder(startBasicBlock, listAllBlocks.size()))) {
        if (block == startBasicBlock)
          continue;

        // init new dominance to one of processed predecessor's
        CfgBasicBlock newDom = getSomePredecessorsDominance(block);

        // intersect with dominance set of all predecessors
        for (val pred : block.getPredecessors()) {
          if (pred instanceof CfgBasicBlock) {
            val predDom = dominance.get(((CfgBasicBlock) pred));
            if (predDom != null)
              newDom = intersection(newDom, predDom);
          }
        }

        // set as the new dominance
        val origDom = dominance.get(block);
        if (origDom != newDom) {
          dominance.put(block, newDom);
          somethingChanged = true;
        }
      }
    } while (somethingChanged);
  }

  private CfgBasicBlock getSomePredecessorsDominance(CfgBasicBlock block) {
    for (val pred : block.getPredecessors()) {
      if (pred instanceof CfgBasicBlock) {
        val predDom = dominance.get(((CfgBasicBlock) pred));
        if (predDom != null)
          return predDom;
      }
    }
    return null;
  }

  private List<CfgBasicBlock> blocksInPostOrder(CfgBasicBlock startBlock, int blockCount) {
    val visitOrder = new ArrayList<CfgBasicBlock>(blockCount);
    val visited = new HashSet<CfgBasicBlock>();
    postOrderTraversal(startBlock, visitOrder, visited);
    return visitOrder;
  }

  private void postOrderTraversal(CfgBasicBlock node, List<CfgBasicBlock> visitOrder, Set<CfgBasicBlock> visited) {
    if (!visited.contains(node)) {
      visited.add(node);

      for (val succ : node.getSuccessors())
        if (succ instanceof CfgBasicBlock)
          postOrderTraversal((CfgBasicBlock) succ, visitOrder, visited);

      visitOrder.add(node);
    }
  }

  private CfgBasicBlock intersection(CfgBasicBlock b1, CfgBasicBlock b2) {
    val path1 = new LinkedList<CfgBasicBlock>();
    val path2 = new LinkedList<CfgBasicBlock>();

    CfgBasicBlock prev1;
    do {
      path1.addFirst(b1);
      prev1 = b1;
      b1 = dominance.get(b1);
    } while (b1 != prev1);

    CfgBasicBlock prev2;
    do {
      path2.addFirst(b2);
      prev2 = b2;
      b2 = dominance.get(b2);
    } while (b2 != prev2);

    int i = 0;
    while (path1.size() > i && path2.size() > i && path1.get(i) == path2.get(i))
      i++;
    return path1.get(i-1);
  }

  public boolean isDominant(CfgBasicBlock dominator, CfgBasicBlock dominatee) {
    CfgBasicBlock prev, curr;

    curr = dominatee;
    do {
      if (curr == dominator)
        return true;
      prev = curr;
      curr = dominance.get(curr);
    } while (prev != curr);

    return false;
  }
}
