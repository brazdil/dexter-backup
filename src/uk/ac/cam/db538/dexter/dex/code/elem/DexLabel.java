package uk.ac.cam.db538.dexter.dex.code.elem;

import lombok.Getter;

public class DexLabel extends DexCodeElement {

  private static long LABEL_COUNTER = 1L;

  @Getter private final long id;

  public DexLabel() {
	this.id = LABEL_COUNTER++;
    if (LABEL_COUNTER < 0L)
      LABEL_COUNTER = 1L;
  }
  
  public static void resetCounter() {
	  LABEL_COUNTER = 1L;
  }

  @Override
  public String toString() {
    return "L" + Long.toString(id) + ":";
  }

  @Override
  public boolean cfgStartsBasicBlock() {
    return true;
  }
}
