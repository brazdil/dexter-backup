package uk.ac.cam.db538.dexter.dex.code;

import lombok.Getter;

public class DexMoveWideInstruction extends DexInstruction {

  @Getter private final DexRegister RegTo1;
  @Getter private final DexRegister RegTo2;
  @Getter private final DexRegister RegFrom1;
  @Getter private final DexRegister RegFrom2;

  public DexMoveWideInstruction(DexRegister to1, DexRegister to2, DexRegister from1, DexRegister from2) {
    RegTo1 = to1;
    RegTo2 = to2;
    RegFrom1 = from1;
    RegFrom2 = from2;
  }

  @Override
  public String getOriginalInstruction() {
    return "move-wide r" + RegTo1.getOriginalId() + ", r" + RegFrom1.getOriginalId();
  }
}
