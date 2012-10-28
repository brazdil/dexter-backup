package uk.ac.cam.db538.dexter.dex.code;

import lombok.Getter;

public class DexInstruction_MoveResult extends DexInstruction {

  @Getter private final DexRegister RegTo;
  @Getter private final boolean ObjectMoving;

  public DexInstruction_MoveResult(DexRegister to, boolean objectMoving) {
    RegTo = to;
    ObjectMoving = objectMoving;
  }

  @Override
  public String getOriginalAssembly() {
    return "move-result" + (ObjectMoving ? "-object" : "") +
           " v" + RegTo.getOriginalId();
  }
}
