package uk.ac.cam.db538.dexter.dex.code.elem;

import lombok.Getter;

public class DexLabel extends DexCodeElement {

  @Getter private final int id;

  public DexLabel(int id) {
	  this.id = id;
  }
  
  @Override
  public String toString() {
    return "L" + Integer.toString(id);
  }

  @Override
  public boolean cfgStartsBasicBlock() {
    return true;
  }
}
