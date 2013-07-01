package uk.ac.cam.db538.dexter.dex.code.elem;

import uk.ac.cam.db538.dexter.dex.code.DexCode;

public class DexCodeStart extends DexCodeElement {

  public DexCodeStart(DexCode methodCode) {
    super(methodCode);
  }

  @Override
  public String getOriginalAssembly() {
    return "START:";
  }
}
