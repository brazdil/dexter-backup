package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import lombok.val;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Format.PackedSwitchDataPseudoInstruction;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.CodeParserState;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.elem.DexLabel;

public class DexInstruction_PackedSwitchData extends DexInstruction {

  @Getter @Setter private DexInstruction_Switch parentInstruction;
  @Getter private final int firstKey;
  @Getter private final List<DexLabel> targets;

  public DexInstruction_PackedSwitchData(DexCode methodCode, DexInstruction_Switch parentInsn, int firstKey, List<DexLabel> targets) {
    super(methodCode);

    this.parentInstruction = parentInsn;
    this.firstKey = firstKey;
    this.targets = targets;
  }

  public DexInstruction_PackedSwitchData(DexCode methodCode, Instruction insn, CodeParserState parsingState) {
    super(methodCode);

    if (insn instanceof PackedSwitchDataPseudoInstruction) {

      val insnPackedSwitchData = (PackedSwitchDataPseudoInstruction) insn;

      val parentInsn = parsingState.getCurrentOffsetParent();
      if (parentInsn == null || !(parentInsn instanceof DexInstruction_Switch) ||
          !((DexInstruction_Switch) parentInsn).isPacked())
        throw new InstructionParseError("Cannot find PackedSwitchData's parent instruction");
      this.parentInstruction = (DexInstruction_Switch) parentInsn;

      this.firstKey = insnPackedSwitchData.getFirstKey();

      int targetCount = insnPackedSwitchData.getTargetCount();
      this.targets = new ArrayList<DexLabel>(targetCount);
      for (int i = 0; i < targetCount; ++i)
        // find label by taking the given offset relative to the original PackedSwitch instruction
        this.targets.add(parsingState.getLabel(insnPackedSwitchData.getTargets()[i], parentInsn));

    } else
      throw FORMAT_EXCEPTION;
  }

  @Override
  public String toString() {
    return "packed-switch-data";
  }

  @Override
  public void instrument(DexCode_InstrumentationState state) { }

  @Override
  public boolean cfgEndsBasicBlock() {
    return true;
  }

  @Override
  public Set<? extends DexCodeElement> cfgJumpTargets(DexCode code) {
	return new HashSet<DexCodeElement>(targets);
  }

  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }
}
