package uk.ac.cam.db538.dexter.dex.code.insn.pseudo;

import java.util.List;

import lombok.Getter;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Invoke;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_MoveResult;

public class DexPseudoinstruction_InvokeWithResult extends DexPseudoinstruction {

  @Getter private final DexInstruction_Invoke instructionInvoke;
  @Getter private final DexInstruction_MoveResult instructionMoveResult;

  public DexPseudoinstruction_InvokeWithResult(DexInstruction_Invoke insnInvoke, DexInstruction_MoveResult insnMoveResult) {
    super(insnInvoke.getMethodCode());

    this.instructionInvoke = insnInvoke;
    this.instructionMoveResult = insnMoveResult;
  }

  @Override
  public List<DexCodeElement> unwrap() {
    return createList(
             (DexCodeElement) instructionInvoke,
             (DexCodeElement) instructionMoveResult);
  }

}
