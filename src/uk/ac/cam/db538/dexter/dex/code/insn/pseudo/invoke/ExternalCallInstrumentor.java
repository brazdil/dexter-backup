package uk.ac.cam.db538.dexter.dex.code.insn.pseudo.invoke;

import java.util.ArrayList;
import java.util.List;

import lombok.val;

import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.utils.Pair;

public abstract class ExternalCallInstrumentor {

  public abstract boolean canBeApplied(DexPseudoinstruction_Invoke insn);
  public abstract Pair<List<DexCodeElement>, List<DexCodeElement>> generateInstrumentation(DexPseudoinstruction_Invoke insn, DexCode_InstrumentationState state);

  public static List<ExternalCallInstrumentor> getInstrumentors() {
    val instrumentors = new ArrayList<ExternalCallInstrumentor>();
    instrumentors.add(new Source_ContentResolver());
    instrumentors.add(new Sink_SendIntent());
    instrumentors.add(new Sink_HttpClient());
    return instrumentors;
  }
}
