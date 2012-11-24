package uk.ac.cam.db538.dexter.dex.code;

import java.util.HashMap;
import java.util.Map;

import lombok.val;

public class DexCode_InstrumentationState {
  private final Map<DexRegister, DexRegister> registerMap;
  private final int idOffset;

  public DexCode_InstrumentationState(DexCode code) {
    registerMap = new HashMap<DexRegister, DexRegister>();

    // find the maximal register id in the code
    // this is strictly for GUI purposes
    // actual register allocation happens later;
    // that said, it still organises the registers
    // according to this
    int maxId = -1;
    for (val reg : code.getUsedRegisters()) {
      val id = reg.getId();
      if (id != null && maxId < id)
        maxId = id;
    }
    idOffset = maxId + 1;
  }

  public DexRegister getTaintRegister(DexRegister reg) {
    val taintReg = registerMap.get(reg);
    if (taintReg == null) {
      val newReg = new DexRegister(reg.getId() + idOffset);
      registerMap.put(reg, newReg);
      return newReg;
    } else
      return taintReg;
  }
}