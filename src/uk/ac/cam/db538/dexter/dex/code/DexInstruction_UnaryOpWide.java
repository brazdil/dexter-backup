package uk.ac.cam.db538.dexter.dex.code;

import lombok.Getter;

public class DexInstruction_UnaryOpWide extends DexInstruction {

  public static enum Operation {
    neg,
    not
  }

  public static enum Operand {
    Long,
    Double
  }

  @Getter private final DexRegister RegTo1;
  @Getter private final DexRegister RegTo2;
  @Getter private final DexRegister RegFrom1;
  @Getter private final DexRegister RegFrom2;
  @Getter private final Operation InsnOperation;
  @Getter private final Operand InsnOperand;

  public DexInstruction_UnaryOpWide(DexRegister to1, DexRegister to2, DexRegister from1, DexRegister from2, Operation op, Operand type) {
    RegTo1 = to1;
    RegTo2 = to2;
    RegFrom1 = from1;
    RegFrom2 = from2;
    InsnOperation = op;
    InsnOperand = type;
  }

  @Override
  public String getOriginalAssembly() {
    return InsnOperation.name() + "-" + InsnOperand.name().toLowerCase() +
           " v" + RegTo1.getOriginalId() + ", v" + RegFrom1.getOriginalId();
  }
}
