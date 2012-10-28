package uk.ac.cam.db538.dexter.dex.code;

import lombok.Getter;

public class DexInstruction_MoveWide extends DexInstruction {

  @Getter private final DexRegister RegTo1;
  @Getter private final DexRegister RegTo2;
  @Getter private final DexRegister RegFrom1;
  @Getter private final DexRegister RegFrom2;

  public DexInstruction_MoveWide(DexRegister to1, DexRegister to2, DexRegister from1, DexRegister from2) {
    RegTo1 = to1;
    RegTo2 = to2;
    RegFrom1 = from1;
    RegFrom2 = from2;
  }

  @Override
  public String getOriginalAssembly() {
    return "move-wide v" + RegTo1.getOriginalId() + ", v" + RegFrom1.getOriginalId();
  }
}
