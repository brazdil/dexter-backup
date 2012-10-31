package uk.ac.cam.db538.dexter.dex.code;

import uk.ac.cam.db538.dexter.dex.type.DexReferenceType;

import lombok.Getter;

public class DexInstruction_CheckCast extends DexInstruction {

  @Getter private final DexRegister RegTo;
  @Getter private final DexReferenceType Value;

  // CAREFUL: likely to throw exception

  public DexInstruction_CheckCast(DexRegister to, DexReferenceType value) {
    RegTo = to;
    Value = value;
  }

  @Override
  public String getOriginalAssembly() {
    return "check-cast v" + RegTo.getId() + ", " + Value.getDescriptor();
  }

  @Override
  public DexInstruction[] instrument(TaintRegisterMap mapping) {
    return new DexInstruction[] { this };
  }
}
