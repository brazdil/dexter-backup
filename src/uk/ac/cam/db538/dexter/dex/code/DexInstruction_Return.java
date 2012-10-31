package uk.ac.cam.db538.dexter.dex.code;

import lombok.Getter;

public class DexInstruction_Return extends DexInstruction {

  @Getter private final DexRegister RegFrom;
  @Getter private final boolean ObjectMoving;

  public DexInstruction_Return(DexRegister from, boolean objectMoving) {
    RegFrom = from;
    ObjectMoving = objectMoving;
  }

  @Override
  public String getOriginalAssembly() {
    return "return" + (ObjectMoving ? "-object" : "") +
           " v" + RegFrom.getId();
  }

  @Override
  public DexInstruction[] instrument(TaintRegisterMap mapping) {
    return new DexInstruction[] { this };
  }
}
