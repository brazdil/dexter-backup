package uk.ac.cam.db538.dexter.dex.code;

import lombok.Getter;

public class DexMoveInstruction extends DexInstruction {

  @Getter private final DexRegister RegTo;
  @Getter private final DexRegister RegFrom;
  @Getter private final boolean ObjectMoving;

  public DexMoveInstruction(DexRegister to, DexRegister from, boolean objectMoving) {
    RegTo = to;
    RegFrom = from;
    ObjectMoving = objectMoving;
  }

  @Override
  public String getOriginalInstruction() {
    return "move" + (ObjectMoving ? "-object" : "") +
           " r" + RegTo.getOriginalId() + ", r" + RegFrom.getOriginalId();
  }
}
