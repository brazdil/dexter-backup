package uk.ac.cam.db538.dexter.analysis.cfg;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import lombok.Getter;
import lombok.val;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCodeElement;
import uk.ac.cam.db538.dexter.utils.NoDuplicatesLinkedList;

public class ControlFlowGraph {

  @Getter private final DexCode Code;

  @Getter private StartBlock StartBlock;
  @Getter private ExitBlock ExitBlock;
  @Getter private List<BasicBlock> BasicBlocks;

  public ControlFlowGraph(DexCode code) {
    Code = code;
    update();
  }

  public void update() {
    StartBlock = new StartBlock();
    ExitBlock = new ExitBlock();
    BasicBlocks = new LinkedList<BasicBlock>();

    val insns = Code.getInstructionList();

    // this is a map going from the first instruction
    // of a block to its reference; used later to create
    // edges between blocks
    val insnBlockMap = new HashMap<DexCodeElement, BasicBlock>();

    // split instruction list into basic blocks
    NoDuplicatesLinkedList<DexCodeElement> currentBlock = new NoDuplicatesLinkedList<DexCodeElement>();
    for (val insn : insns) {
      if (insn.cfgStartsBasicBlock() && !currentBlock.isEmpty()) {
        val block = new BasicBlock(currentBlock);
        BasicBlocks.add(block);
        insnBlockMap.put(block.getFirstInstruction(), block);
        currentBlock = new NoDuplicatesLinkedList<DexCodeElement>();
      }

      currentBlock.add(insn);

      if ((insn.cfgEndsBasicBlock() || insn.cfgExitsMethod()) && !currentBlock.isEmpty()) {
        val block = new BasicBlock(currentBlock);
        BasicBlocks.add(block);
        insnBlockMap.put(block.getFirstInstruction(), block);
        currentBlock = new NoDuplicatesLinkedList<DexCodeElement>();
      }
    }
    if (!currentBlock.isEmpty()) {
      val block = new BasicBlock(currentBlock);
      BasicBlocks.add(block);
      insnBlockMap.put(block.getFirstInstruction(), block);
    }

    // connect blocks together

    if (BasicBlocks.isEmpty()) {
      // no basic blocks => just connect START and EXIT
      Block.createEdge(StartBlock, ExitBlock);
    } else {
      // connect first block with START
      Block.createEdge(StartBlock, BasicBlocks.get(0));

      // connect blocks together using the list of successors
      // provided by each instruction
      for (val block : BasicBlocks) {
        val lastInsn = block.getLastInstruction();
        val lastInsnSuccs = lastInsn.cfgGetSuccessors();

        // if a block ends with a returning instruction
        // connect it to EXIT
        if (lastInsn.cfgExitsMethod() || lastInsnSuccs.length == 0)
          Block.createEdge(block, ExitBlock);

        for (val succ : lastInsnSuccs) {
          val blockSucc = insnBlockMap.get(succ);
          if (blockSucc == null)
            throw new CfgException("Successor of a block doesn't point to a different block");
          Block.createEdge(block, blockSucc);
        }
      }
    }
  }
}
