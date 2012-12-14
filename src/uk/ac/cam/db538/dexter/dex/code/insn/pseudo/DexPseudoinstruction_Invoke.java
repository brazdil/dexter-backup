package uk.ac.cam.db538.dexter.dex.code.insn.pseudo;

import java.util.List;

import lombok.Getter;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Invoke;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_MoveResult;

public class DexPseudoinstruction_Invoke extends DexPseudoinstruction {

  @Getter private final DexInstruction_Invoke instructionInvoke;
  @Getter private final DexInstruction_MoveResult instructionMoveResult;

  public DexPseudoinstruction_Invoke(DexInstruction_Invoke insnInvoke, DexInstruction_MoveResult insnMoveResult) {
    super(insnInvoke.getMethodCode());

    this.instructionInvoke = insnInvoke;
    this.instructionMoveResult = insnMoveResult;
  }

  public DexPseudoinstruction_Invoke(DexInstruction_Invoke insnInvoke) {
    this(insnInvoke, null);
  }

  private boolean movesResult() {
    return instructionMoveResult != null;
  }

  @Override
  public List<DexCodeElement> unwrap() {
    if (movesResult())
      return createList(
               (DexCodeElement) instructionInvoke,
               (DexCodeElement) instructionMoveResult);
    else
      return createList((DexCodeElement) instructionInvoke);
  }

  private DexCodeElement[] instrumentDirectExternal(DexCode_InstrumentationState state) {
    return new DexCodeElement[] { this };
  }

  private DexCodeElement[] instrumentDirectInternal(DexCode_InstrumentationState state) {
    return new DexCodeElement[] { this };
  }

  private DexCodeElement[] instrumentVirtual(DexCode_InstrumentationState state) {
    return new DexCodeElement[] { this };
  }

  @Override
  public DexCodeElement[] instrument(DexCode_InstrumentationState state) {
    switch (instructionInvoke.getCallType()) {
    case Direct:
    case Static:
      if (instructionInvoke.getClassType().isDefinedInternally())
        return instrumentDirectInternal(state);
      else
        return instrumentDirectExternal(state);
    case Interface:
    case Super:
    case Virtual:
    default:
      return instrumentVirtual(state);
    }
  }
}
