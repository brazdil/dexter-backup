package uk.ac.cam.db538.dexter.analysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.val;
import uk.ac.cam.db538.dexter.analysis.cfg.CfgBasicBlock;
import uk.ac.cam.db538.dexter.analysis.cfg.ControlFlowGraph;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.utils.ListReverser;

public class LiveVarAnalysis {

  @Getter private final DexCode code;
  private Map<DexCodeElement, Set<DexRegister>> liveVarsOut;
  private Map<DexCodeElement, Set<DexRegister>> liveVarsIn;

  public LiveVarAnalysis(DexCode code) {
    this.code = code;
    generateLVA();
  }

  private void generateLVA() {
	val CFG = new ControlFlowGraph(code);
	val basicBlocks = CFG.getBasicBlocks();

	// create tables that will hold variable lists
    liveVarsOut = new HashMap<DexCodeElement, Set<DexRegister>>();
    liveVarsIn = new HashMap<DexCodeElement, Set<DexRegister>>();
    
    // initialize variable lists
    for (val insn : code.getInstructionList())
      liveVarsOut.put(insn, new HashSet<DexRegister>());

    // propagate and combine lists until convergence
    boolean somethingChanged;
    do {
      somethingChanged = false;

      // iterate through basic blocks in reverse order (converges faster)
      for (val block : new ListReverser<CfgBasicBlock>(basicBlocks)) {

    	// combine variable lists of successors   
        Set<DexRegister> insnLiveIn = new HashSet<>();
        for (val succ : block.getSuccessors())
          if (succ instanceof CfgBasicBlock)
            insnLiveIn.addAll(liveVarsOut.get(
            	((CfgBasicBlock) succ).getFirstInstruction()));

        // propagate the variable list backwards 
        // through the instructions of the basic block 
        for (val insn : new ListReverser<DexCodeElement>(block.getInstructions())) {
        	
          // store the list coming to the instruction from successors
          liveVarsIn.put(insn, insnLiveIn);

          // acquire the variable list of this instruction  
          val insnLiveOut = liveVarsOut.get(insn);
          int insnLiveOut_PrevSize = insnLiveOut.size();

          // add the incoming vars, remove defined and add referenced
          insnLiveOut.addAll(insnLiveIn);
          insnLiveOut.removeAll(insn.lvaDefinedRegisters());
          insnLiveOut.addAll(insn.lvaReferencedRegisters());

          // if size of the var list changed, something was added
          if (insnLiveOut_PrevSize < insnLiveOut.size())
            somethingChanged = true;

          // pass the list to the preceding instruction
          insnLiveIn = insnLiveOut;
        }
      }
    } while (somethingChanged);
  }

  public Set<DexRegister> getLiveVarsBefore(DexCodeElement insn) {
    return liveVarsOut.get(insn);
  }

  public Set<DexRegister> getLiveVarsAfter(DexCodeElement insn) {
    return liveVarsIn.get(insn);
  }
}
