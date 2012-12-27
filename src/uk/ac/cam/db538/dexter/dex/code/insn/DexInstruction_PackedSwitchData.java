package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Format.PackedSwitchDataPseudoInstruction;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.elem.DexLabel;

public class DexInstruction_PackedSwitchData extends DexInstruction {

  @Getter private final DexInstruction_PackedSwitch parentInstruction;
  @Getter private final int firstKey;
  @Getter private final List<DexLabel> targets;

  public DexInstruction_PackedSwitchData(DexCode methodCode, DexInstruction_PackedSwitch parentInsn, int firstKey, List<DexLabel> targets) {
    super(methodCode);

    this.parentInstruction = parentInsn;
    this.firstKey = firstKey;
    this.targets = targets;
  }

  public DexInstruction_PackedSwitchData(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) {
    super(methodCode);

    if (insn instanceof PackedSwitchDataPseudoInstruction) {

      val insnPackedSwitchData = (PackedSwitchDataPseudoInstruction) insn;

      val parentInsn = parsingState.getCurrentOffsetParent();
      if (parentInsn == null || !(parentInsn instanceof DexInstruction_PackedSwitch))
        throw new InstructionParsingException("Cannot find PackedSwitchData's parent instruction");
      this.parentInstruction = (DexInstruction_PackedSwitch) parentInsn;

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
  public String getOriginalAssembly() {
    return "packed-switch-data";
  }

  @Override
  protected DexCodeElement gcReplaceWithTemporaries(Map<DexRegister, DexRegister> mapping) {
    return new DexInstruction_PackedSwitchData(getMethodCode(), parentInstruction, firstKey, targets);
  }
}
