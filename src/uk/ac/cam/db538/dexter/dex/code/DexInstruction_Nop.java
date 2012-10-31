package uk.ac.cam.db538.dexter.dex.code;

public class DexInstruction_Nop extends DexInstruction {

  @Override
  public String getOriginalAssembly() {
    return "nop";
  }

  @Override
  public DexInstruction[] instrument(TaintRegisterMap mapping) {
    return new DexInstruction[] { this };
  }
}
