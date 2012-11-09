package uk.ac.cam.db538.dexter.analysis.lva;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.val;
import uk.ac.cam.db538.dexter.analysis.cfg.CfgBasicBlock;
import uk.ac.cam.db538.dexter.analysis.cfg.ControlFlowGraph;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.reg.DexRegister;
import uk.ac.cam.db538.dexter.utils.ListReverser;

public class LiveVarAnalysis {

  @Getter private final DexCode Code;
  private Map<DexCodeElement, Set<DexRegister>> LiveVars;

  public LiveVarAnalysis(DexCode code) {
    Code = code;

    update();
  }

  public void update() {
    LiveVars = new HashMap<DexCodeElement, Set<DexRegister>>();
    for (val insn : Code.getInstructionList())
      LiveVars.put(insn, new HashSet<DexRegister>());

    val CFG = new ControlFlowGraph(Code);
    boolean somethingChanged;
    do {
      somethingChanged = false;

      for (val block : new ListReverser<CfgBasicBlock>(CFG.getBasicBlocks())) {
        Set<DexRegister> insnLiveIn = new HashSet<DexRegister>();

        // union of successors's liveOut
        for (val succ : block.getSuccessors())
          if (succ instanceof CfgBasicBlock)
            insnLiveIn.addAll(LiveVars.get(((CfgBasicBlock) succ).getFirstInstruction()));

        // iterate instructions of the block in reverse order
        // and propagate live var info
        for (val insn : new ListReverser<DexCodeElement>(block.getInstructions())) {

          val insnLiveOut = LiveVars.get(insn);
          int insnLiveOut_PrevSize = insnLiveOut.size();

          insnLiveOut.addAll(insnLiveIn);
          insnLiveOut.removeAll(Arrays.asList(insn.lvaDefinedRegisters()));
          insnLiveOut.addAll(Arrays.asList(insn.lvaReferencedRegisters()));

          if (insnLiveOut_PrevSize < insnLiveOut.size())
            somethingChanged = true;

          insnLiveIn = insnLiveOut;
        }
      }
    } while (somethingChanged);
  }

  public Set<DexRegister> getLiveVarsAt(DexCodeElement insn) {
    return LiveVars.get(insn);
  }

}
