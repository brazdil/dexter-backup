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
    liveVarsOut = new HashMap<DexCodeElement, Set<DexRegister>>();
    liveVarsIn = new HashMap<DexCodeElement, Set<DexRegister>>();
    for (val insn : code.getInstructionList())
      liveVarsOut.put(insn, new HashSet<DexRegister>());

    val CFG = new ControlFlowGraph(code);
    boolean somethingChanged;
    do {
      somethingChanged = false;

      for (val block : new ListReverser<CfgBasicBlock>(CFG.getBasicBlocks())) {
        Set<DexRegister> insnLiveIn = new HashSet<DexRegister>();

        // union of successors's liveOut
        for (val succ : block.getSuccessors())
          if (succ instanceof CfgBasicBlock)
            insnLiveIn.addAll(liveVarsOut.get(((CfgBasicBlock) succ).getFirstInstruction()));

        // iterate instructions of the block in reverse order
        // and propagate live var info
        for (val insn : new ListReverser<DexCodeElement>(block.getInstructions())) {
          // store liveVarIn
          liveVarsIn.put(insn, insnLiveIn);

          val insnLiveOut = liveVarsOut.get(insn);
          int insnLiveOut_PrevSize = insnLiveOut.size();

          insnLiveOut.addAll(insnLiveIn);
          insnLiveOut.removeAll(insn.lvaDefinedRegisters());
          insnLiveOut.addAll(insn.lvaReferencedRegisters());

          if (insnLiveOut_PrevSize < insnLiveOut.size())
            somethingChanged = true;

          insnLiveIn = insnLiveOut;
        }
      }
    } while (somethingChanged);
  }

  public Set<DexRegister> getLiveVarsOut(DexCodeElement insn) {
    return liveVarsOut.get(insn);
  }

  public Set<DexRegister> getLiveVarsIn(DexCodeElement insn) {
    return liveVarsIn.get(insn);
  }
}
