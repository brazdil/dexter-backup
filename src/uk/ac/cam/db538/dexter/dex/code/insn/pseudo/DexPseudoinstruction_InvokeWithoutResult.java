package uk.ac.cam.db538.dexter.dex.code.insn.pseudo;

import java.util.List;

import lombok.Getter;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Invoke;

public class DexPseudoinstruction_InvokeWithoutResult extends DexPseudoinstruction {

  @Getter private final DexInstruction_Invoke instructionInvoke;

  public DexPseudoinstruction_InvokeWithoutResult(DexInstruction_Invoke insnInvoke) {
    super(insnInvoke.getMethodCode());

    this.instructionInvoke = insnInvoke;
  }

  @Override
  public List<DexCodeElement> unwrap() {
    return createList((DexCodeElement) instructionInvoke);
  }

}
