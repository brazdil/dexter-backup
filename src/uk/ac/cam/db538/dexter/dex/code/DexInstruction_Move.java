package uk.ac.cam.db538.dexter.dex.code;

import lombok.Getter;

public class DexInstruction_Move extends DexInstruction {

  @Getter private final DexRegister RegTo;
  @Getter private final DexRegister RegFrom;
  @Getter private final boolean ObjectMoving;

  // CAREFUL: registers can only be allocated to 0-15 regular move !!!

  public DexInstruction_Move(DexRegister to, DexRegister from, boolean objectMoving) {
    RegTo = to;
    RegFrom = from;
    ObjectMoving = objectMoving;
  }

  @Override
  public String getOriginalAssembly() {
    return "move" + (ObjectMoving ? "-object" : "") +
           " v" + RegTo.getId() + ", v" + RegFrom.getId();
  }

  @Override
  public DexInstruction[] instrument(TaintRegisterMap mapping) {
    return new DexInstruction[] { this };
  }
}
