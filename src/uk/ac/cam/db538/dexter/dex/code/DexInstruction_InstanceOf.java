package uk.ac.cam.db538.dexter.dex.code;

import uk.ac.cam.db538.dexter.dex.type.DexReferenceType;

import lombok.Getter;

public class DexInstruction_InstanceOf extends DexInstruction {

  @Getter private final DexRegister RegTo;
  @Getter private final DexRegister RegFrom;
  @Getter private final DexReferenceType Value;

  // CAREFUL: likely to throw exception

  public DexInstruction_InstanceOf(DexRegister to, DexRegister from, DexReferenceType value) {
    RegTo = to;
    RegFrom = from;
    Value = value;
  }

  @Override
  public String getOriginalAssembly() {
    return "instance-of v" + RegTo.getId() + ", v" + RegFrom.getId() +
           ", " + Value.getDescriptor();
  }
}
