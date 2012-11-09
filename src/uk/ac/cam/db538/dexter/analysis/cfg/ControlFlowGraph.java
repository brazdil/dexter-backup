package uk.ac.cam.db538.dexter.analysis.cfg;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import lombok.Getter;
import lombok.val;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCodeElement;
import uk.ac.cam.db538.dexter.utils.NoDuplicatesList;

public class ControlFlowGraph {

  @Getter private final DexCode Code;

  @Getter private CfgStartBlock StartBlock;
  @Getter private CfgExitBlock ExitBlock;
  @Getter private List<CfgBasicBlock> BasicBlocks;

  public ControlFlowGraph(DexCode code) {
    Code = code;
    update();
  }

  public void update() {
    StartBlock = new CfgStartBlock();
    ExitBlock = new CfgExitBlock();
    BasicBlocks = new LinkedList<CfgBasicBlock>();

    val insns = Code.getInstructionList();

    // this is a map going from the first instruction
    // of a block to its reference; used later to create
    // edges between blocks
    val insnBlockMap = new HashMap<DexCodeElement, CfgBasicBlock>();

    // split instruction list into basic blocks
    NoDuplicatesList<DexCodeElement> currentBlock = new NoDuplicatesList<DexCodeElement>();
    for (val insn : insns) {
      if (insn.cfgStartsBasicBlock() && !currentBlock.isEmpty()) {
        val block = new CfgBasicBlock(currentBlock);
        BasicBlocks.add(block);
        insnBlockMap.put(block.getFirstInstruction(), block);
        currentBlock = new NoDuplicatesList<DexCodeElement>();
      }

      currentBlock.add(insn);

      if ((insn.cfgEndsBasicBlock() || insn.cfgExitsMethod()) && !currentBlock.isEmpty()) {
        val block = new CfgBasicBlock(currentBlock);
        BasicBlocks.add(block);
        insnBlockMap.put(block.getFirstInstruction(), block);
        currentBlock = new NoDuplicatesList<DexCodeElement>();
      }
    }
    if (!currentBlock.isEmpty()) {
      val block = new CfgBasicBlock(currentBlock);
      BasicBlocks.add(block);
      insnBlockMap.put(block.getFirstInstruction(), block);
    }

    // connect blocks together

    if (BasicBlocks.isEmpty()) {
      // no basic blocks => just connect START and EXIT
      CfgBlock.createEdge(StartBlock, ExitBlock);
    } else {
      // connect first block with START
      CfgBlock.createEdge(StartBlock, BasicBlocks.get(0));

      // connect blocks together using the list of successors
      // provided by each instruction
      for (val block : BasicBlocks) {
        val lastInsn = block.getLastInstruction();
        val lastInsnSuccs = lastInsn.cfgGetSuccessors();

        // if a block ends with a returning instruction
        // connect it to EXIT
        if (lastInsn.cfgExitsMethod() || lastInsnSuccs.length == 0)
          CfgBlock.createEdge(block, ExitBlock);

        for (val succ : lastInsnSuccs) {
          val blockSucc = insnBlockMap.get(succ);
          if (blockSucc == null)
            throw new CfgException("Successor of a block doesn't point to a different block");
          CfgBlock.createEdge(block, blockSucc);
        }
      }
    }
  }
}
