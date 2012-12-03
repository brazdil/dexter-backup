package uk.ac.cam.db538.dexter.dex.code.insn;

import lombok.Getter;

public class InstructionOffsetException extends RuntimeException {

  private static final long serialVersionUID = -5210852859056051200L;

  @Getter private DexInstruction problematicInstruction;

  public InstructionOffsetException(DexInstruction insn) {
    super();
    this.problematicInstruction = insn;
  }
}
