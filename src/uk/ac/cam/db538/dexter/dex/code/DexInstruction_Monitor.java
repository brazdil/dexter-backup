package uk.ac.cam.db538.dexter.dex.code;

import lombok.Getter;

public class DexInstruction_Monitor extends DexInstruction {

  @Getter private final DexRegister Reg;
  @Getter private final boolean Enter;

  public DexInstruction_Monitor(DexRegister reg, boolean entering) {
    Reg = reg;
    Enter = entering;
  }

  @Override
  public String getOriginalAssembly() {
    return "monitor-" + (Enter ? "enter" : "exit") +
           " v" + Reg.getId();
  }
}
