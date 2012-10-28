package uk.ac.cam.db538.dexter.dex.code;

import uk.ac.cam.db538.dexter.dex.type.DexClassType;

import lombok.Getter;

public class DexInstruction_NewInstance extends DexInstruction {

  @Getter private final DexRegister RegTo;
  @Getter private final DexClassType Value;

  public DexInstruction_NewInstance(DexRegister to, DexClassType value) {
    RegTo = to;
    Value = value;
  }

  @Override
  public String getOriginalAssembly() {
    return "new-instance v" + RegTo.getOriginalId() + ", " + Value.getDescriptor();
  }
}
