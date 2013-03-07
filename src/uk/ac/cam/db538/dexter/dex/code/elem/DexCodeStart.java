package uk.ac.cam.db538.dexter.dex.code.elem;

import java.util.Map;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;

public class DexCodeStart extends DexCodeElement {

  public DexCodeStart(DexCode methodCode) {
    super(methodCode);
  }

  @Override
  public String getOriginalAssembly() {
    return "START:";
  }

  @Override
  protected DexCodeElement gcReplaceWithTemporaries(Map<DexRegister, DexRegister> mapping, boolean toRefs, boolean toDefs) {
    return this;
  }
}
