package uk.ac.cam.db538.dexter.dex.code;

import uk.ac.cam.db538.dexter.dex.type.DexArrayType;

import lombok.Getter;

public class DexInstruction_NewArray extends DexInstruction {

  @Getter private final DexRegister RegTo;
  @Getter private final DexRegister RegSize;
  @Getter private final DexArrayType Value;

  public DexInstruction_NewArray(DexRegister to, DexRegister size, DexArrayType value) {
    RegTo = to;
    RegSize = size;
    Value = value;
  }

  @Override
  public String getOriginalAssembly() {
    return "new-array v" + RegTo.getOriginalId() + ", v" + RegSize.getOriginalId() +
           ", " + Value.getDescriptor();
  }
}
