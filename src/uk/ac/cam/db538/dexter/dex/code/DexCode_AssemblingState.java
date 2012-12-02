package uk.ac.cam.db538.dexter.dex.code;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.val;
import uk.ac.cam.db538.dexter.dex.DexAssemblingCache;

public class DexCode_AssemblingState {

  @Getter private DexCode code;
  @Getter private DexAssemblingCache cache;
  private Map<DexCodeElement, Integer> elementOffsets;
  private Map<DexRegister, Integer> registerAllocation;

  public DexCode_AssemblingState(DexCode code, DexAssemblingCache cache, Map<DexRegister, Integer> regAlloc) {
    this.cache = cache;
    this.registerAllocation = regAlloc;
    this.code = code;

    // initialise elementOffsets
    this.elementOffsets = new HashMap<DexCodeElement, Integer>();
    for (val elem : code.getInstructionList())
      elementOffsets.put(elem, Integer.MAX_VALUE);
  }

  public Map<DexRegister, Integer> getRegisterAllocation() {
    return Collections.unmodifiableMap(registerAllocation);
  }

  public Map<DexCodeElement, Integer> getElementOffsets() {
    return Collections.unmodifiableMap(elementOffsets);
  }

  public void setElementOffset(DexCodeElement elem, int offset) {
    elementOffsets.put(elem, offset);
  }
}
