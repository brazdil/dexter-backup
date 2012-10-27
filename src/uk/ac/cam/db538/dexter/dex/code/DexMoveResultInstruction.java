package uk.ac.cam.db538.dexter.dex.code;

import lombok.Getter;

public class DexMoveResultInstruction extends DexInstruction {

  @Getter private final DexRegister RegTo;
  @Getter private final boolean ObjectMoving;

  public DexMoveResultInstruction(DexRegister to, boolean objectMoving) {
    RegTo = to;
    ObjectMoving = objectMoving;
  }

  @Override
  public String getOriginalInstruction() {
    return "move-result" + (ObjectMoving ? "-object" : "") +
           " v" + RegTo.getOriginalId();
  }
}
