package uk.ac.cam.db538.dexter.dex.code;

import lombok.Getter;

public class DexInstruction_UnaryOp extends DexInstruction {

  public static enum Operation {
    neg,
    not
  }

  public static enum Operand {
    Int,
    Float
  }

  @Getter private final DexRegister RegTo;
  @Getter private final DexRegister RegFrom;
  @Getter private final Operation InsnOperation;
  @Getter private final Operand InsnOperand;

  public DexInstruction_UnaryOp(DexRegister to, DexRegister from, Operation op, Operand type) {
    RegTo = to;
    RegFrom = from;
    InsnOperation = op;
    InsnOperand = type;
  }

  @Override
  public String getOriginalAssembly() {
    return InsnOperation.name() + "-" + InsnOperand.name().toLowerCase() +
           " v" + RegTo.getOriginalId() + ", v" + RegFrom.getOriginalId();
  }
}
