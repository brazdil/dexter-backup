package uk.ac.cam.db538.dexter.dex.code;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.val;
import uk.ac.cam.db538.dexter.dex.DexAssemblingCache;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction;

public class DexCode_AssemblingState {

  @Getter private DexCode code;
  @Getter private DexAssemblingCache cache;
  private Map<DexRegister, Integer> registerAllocation;
  private Map<DexCodeElement, Long> elementOffsets;
  private Map<DexCodeElement, Long> elementOffsets_NextPass;

  public DexCode_AssemblingState(DexCode code, DexAssemblingCache cache, Map<DexRegister, Integer> regAlloc) {
    this.cache = cache;
    this.registerAllocation = regAlloc;
    this.code = code;

    // initialise elementOffsets
    // start by setting the size of each instruction to 1
    // later on, it will get increased iteratively
    this.elementOffsets = new HashMap<DexCodeElement, Long>();
    this.elementOffsets_NextPass = new HashMap<DexCodeElement, Long>();
    if (this.code != null) {
      long offset = 0;
      for (val elem : this.code.getInstructionList()) {
        elementOffsets.put(elem, offset);
        if (elem instanceof DexInstruction)
          offset += 1;
      }
    }
  }

  public Map<DexRegister, Integer> getRegisterAllocation() {
    return Collections.unmodifiableMap(registerAllocation);
  }

  public Map<DexCodeElement, Long> getElementOffsets() {
    return Collections.unmodifiableMap(elementOffsets);
  }

  public void setNextPassElementOffset(DexCodeElement elem, long offset) {
    elementOffsets_NextPass.put(elem, offset);
  }

  public void swapElementOffsetBuffers() {
    val temp = elementOffsets;
    elementOffsets = elementOffsets_NextPass;
    elementOffsets_NextPass = temp;
  }
}
