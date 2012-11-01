package uk.ac.cam.db538.dexter.dex.code;

import uk.ac.cam.db538.dexter.dex.type.DexReferenceType;

import lombok.Getter;

public class DexInstruction_ConstClass extends DexInstruction {

  @Getter private final DexRegister RegTo;
  @Getter private final DexReferenceType Value;

  public DexInstruction_ConstClass(DexRegister to, DexReferenceType value) {
    RegTo = to;
    Value = value;
  }

  @Override
  public String getOriginalAssembly() {
    return "const-class v" + RegTo.getId() + ", " + Value.getDescriptor();
  }
}
