package uk.ac.cam.db538.dexter.dex.code;

import java.util.HashMap;
import java.util.Map;

import uk.ac.cam.db538.dexter.dex.DexInstrumentationCache;

import lombok.Getter;
import lombok.val;

public class DexCode_InstrumentationState {
  private final Map<DexRegister, DexRegister> registerMap;
  private final int idOffset;

  @Getter private final DexInstrumentationCache cache;

  public DexCode_InstrumentationState(DexCode code, DexInstrumentationCache cache) {
    this.cache = cache;

    registerMap = new HashMap<DexRegister, DexRegister>();

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

  public DexRegister getTaintRegister(DexRegister reg) {
    val taintReg = registerMap.get(reg);
    if (taintReg == null) {
      val newReg = new DexRegister(reg.getOriginalIndex() + idOffset);
      registerMap.put(reg, newReg);
      return newReg;
    } else
      return taintReg;
  }
}