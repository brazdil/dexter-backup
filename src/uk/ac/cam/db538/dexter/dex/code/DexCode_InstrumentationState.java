package uk.ac.cam.db538.dexter.dex.code;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.val;
import uk.ac.cam.db538.dexter.dex.DexInstrumentationCache;

public class DexCode_InstrumentationState {
  private final int idOffset;

  @Getter private final DexInstrumentationCache cache;
  @Getter private final boolean needsCallInstrumentation;
  @Getter private final DexRegister internalClassAnnotationRegister;

  public DexCode_InstrumentationState(DexCode code, DexInstrumentationCache cache) {
    this.cache = cache;

    val parentMethod = code.getParentMethod();
    this.needsCallInstrumentation = parentMethod != null && !parentMethod.getMethodDef().isAbstract();
    this.internalClassAnnotationRegister = (parentMethod != null && parentMethod.isVirtual()) ? new DexRegister() : null;

    // find the maximal register id in the code
    // this is strictly for GUI purposes
    // actual register allocation happens later;
    int maxId = -1;
    for (val reg : code.getUsedRegisters()) {
      val id = reg.getOriginalIndex();
      if (maxId < id)
        maxId = id;
    }
    idOffset = maxId + 1;
  }
}