package uk.ac.cam.db538.dexter.dex.code.insn.pseudo.invoke;

import java.util.List;

import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.utils.Pair;

public interface ExternalCallInstrumentor {

  public boolean canBeApplied(DexPseudoinstruction_Invoke insn);
  public Pair<List<DexCodeElement>, List<DexCodeElement>> generateInstrumentation(DexPseudoinstruction_Invoke insn, DexCode_InstrumentationState state);

}
