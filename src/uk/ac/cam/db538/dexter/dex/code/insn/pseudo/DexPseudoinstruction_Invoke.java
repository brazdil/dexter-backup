package uk.ac.cam.db538.dexter.dex.code.insn.pseudo;

import java.util.List;

import lombok.Getter;
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
    return instructionMoveResult == null;
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

}
