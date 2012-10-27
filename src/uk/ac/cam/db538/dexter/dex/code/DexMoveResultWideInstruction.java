package uk.ac.cam.db538.dexter.dex.code;

import lombok.Getter;

public class DexMoveResultWideInstruction extends DexInstruction {

  @Getter private final DexRegister RegTo1;
  @Getter private final DexRegister RegTo2;

  public DexMoveResultWideInstruction(DexRegister to1, DexRegister to2) {
    RegTo1 = to1;
    RegTo2 = to2;
  }

  @Override
  public String getOriginalInstruction() {
    return "move-result-wide v" + RegTo1.getOriginalId();
  }
}
