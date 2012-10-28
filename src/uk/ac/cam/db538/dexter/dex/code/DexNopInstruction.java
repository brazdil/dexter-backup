package uk.ac.cam.db538.dexter.dex.code;

public class DexNopInstruction extends DexInstruction {

  @Override
  public String getOriginalAssembly() {
    return "nop";
  }

}
