package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import lombok.val;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Format.SparseSwitchDataPseudoInstruction;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.CodeParserState;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.elem.DexLabel;
import uk.ac.cam.db538.dexter.utils.Pair;

public class DexInstruction_SparseSwitchData extends DexInstruction {

  // CAREFUL: keys must be sorted low-to-high

  @Getter @Setter private DexInstruction_Switch parentInstruction;
  @Getter private final List<Pair<Integer, DexLabel>> keyTargetPairs;

  public DexInstruction_SparseSwitchData(DexCode methodCode, DexInstruction_Switch parentInsn, List<Pair<Integer, DexLabel>> keyTargetPairs) {
    super(methodCode);

    this.parentInstruction = parentInsn;
    this.keyTargetPairs = keyTargetPairs;
  }

  public DexInstruction_SparseSwitchData(DexCode methodCode, Instruction insn, CodeParserState parsingState) {
    super(methodCode);

    if (insn instanceof SparseSwitchDataPseudoInstruction) {

      val insnSparseSwitchData = (SparseSwitchDataPseudoInstruction) insn;

      val parentInsn = parsingState.getCurrentOffsetParent();
      if (parentInsn == null || !(parentInsn instanceof DexInstruction_Switch) ||
          ((DexInstruction_Switch) parentInsn).isPacked())
        throw new InstructionParseError("Cannot find SparseSwitchData's parent instruction");
      this.parentInstruction = (DexInstruction_Switch) parentInsn;

      int targetCount = insnSparseSwitchData.getTargetCount();
      this.keyTargetPairs = new ArrayList<Pair<Integer, DexLabel>>(targetCount);
      for (int i = 0; i < targetCount; ++i)
        // find label by taking the given offset relative to the original PackedSwitch instruction
        this.keyTargetPairs.add(
          new Pair<Integer, DexLabel>(
            insnSparseSwitchData.getKeys()[i],
            parsingState.getLabel(insnSparseSwitchData.getTargets()[i], parentInsn)));

    } else
      throw FORMAT_EXCEPTION;
  }

  @Override
  public String getOriginalAssembly() {
    return "sparse-switch-data";
  }

  @Override
  public void instrument(DexCode_InstrumentationState state) { }

  @Override
  public boolean cfgEndsBasicBlock() {
    return true;
  }

  @Override
  public Set<DexCodeElement> cfgJumpTargets() {
    val succ = new HashSet<DexCodeElement>();
    for (val pair : keyTargetPairs)
      succ.add(pair.getValB());
    return succ;
  }

  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }
}
