package uk.ac.cam.db538.dexter.dex.code;

import lombok.Getter;

public class DexInstruction_IfTestZero extends DexInstruction {

  public static enum Operation {
    eqz,
    nez,
    ltz,
    gez,
    gtz,
    lez
  }

  @Getter private final DexRegister Reg;
  @Getter private final DexLabel Target;
  @Getter private final Operation Type;

  public DexInstruction_IfTestZero(DexRegister reg, DexLabel target, Operation type) {
    Reg = reg;
    Target = target;
    Type = type;
  }

  @Override
  public String getOriginalAssembly() {
    return "if-" + Type.name() + " v" + Reg.getOriginalId() +
           ", L" + Target.getOriginalOffset();
  }
}
