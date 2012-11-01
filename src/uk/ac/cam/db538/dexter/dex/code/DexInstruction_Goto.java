package uk.ac.cam.db538.dexter.dex.code;

import lombok.Getter;

public class DexInstruction_Goto extends DexInstruction {

  @Getter private final DexLabel Target;

  public DexInstruction_Goto(DexLabel target) {
    Target = target;
  }

  @Override
  public String getOriginalAssembly() {
    return "goto L" + Target.getOriginalOffset();
  }
}
