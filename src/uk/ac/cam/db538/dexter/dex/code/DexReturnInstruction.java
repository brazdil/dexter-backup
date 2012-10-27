package uk.ac.cam.db538.dexter.dex.code;

import lombok.Getter;

public class DexReturnInstruction extends DexInstruction {

  @Getter private final DexRegister RegFrom;
  @Getter private final boolean ObjectMoving;

  public DexReturnInstruction(DexRegister from, boolean objectMoving) {
    RegFrom = from;
    ObjectMoving = objectMoving;
  }

  @Override
  public String getOriginalInstruction() {
    return "return" + (ObjectMoving ? "-object" : "") +
           " v" + RegFrom.getOriginalId();
  }
}
