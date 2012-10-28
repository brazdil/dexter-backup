package uk.ac.cam.db538.dexter.dex.code;

import lombok.Getter;

public class DexMoveExceptionInstruction extends DexInstruction {

  @Getter private final DexRegister RegTo;

  public DexMoveExceptionInstruction(DexRegister to) {
    RegTo = to;
  }

  @Override
  public String getOriginalAssembly() {
    return "move-exception v" + RegTo.getOriginalId();
  }
}
