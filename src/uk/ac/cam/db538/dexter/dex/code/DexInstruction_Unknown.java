package uk.ac.cam.db538.dexter.dex.code;

public class DexInstruction_Unknown extends DexInstruction {

  @Override
  public String getOriginalAssembly() {
    return "???";
  }

  @Override
  public DexInstruction[] instrument(TaintRegisterMap mapping) {
    return new DexInstruction[] { this };
  }
}
