package uk.ac.cam.db538.dexter.dex.code;

import java.util.HashMap;
import java.util.Map;

import lombok.val;
import uk.ac.cam.db538.dexter.dex.code.reg.DexRegister;

public class DexCode_InstrumentationState {
  private final Map<DexRegister, DexRegister> RegisterMap;
  private final int IdOffset;

  public DexCode_InstrumentationState(DexCode code) {
    RegisterMap = new HashMap<DexRegister, DexRegister>();

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
    IdOffset = maxId + 1;
  }

  public DexRegister getTaintRegister(DexRegister reg) {
    val taintReg = RegisterMap.get(reg);
    if (taintReg == null) {
      val newReg = new DexRegister(reg.getId() + IdOffset);
      RegisterMap.put(reg, newReg);
      return newReg;
    } else
      return taintReg;
  }
}