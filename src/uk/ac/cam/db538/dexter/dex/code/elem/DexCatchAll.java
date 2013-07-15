package uk.ac.cam.db538.dexter.dex.code.elem;

import lombok.Getter;

public class DexCatchAll extends DexCodeElement {

  @Getter private final int id;
  
  public DexCatchAll(int id) {
    this.id = id;
  }

  @Override
  public String toString() {
    return "CATCHALL" + Integer.toString(id);
  }

  @Override
  public boolean cfgStartsBasicBlock() {
    return true;
  }
}
