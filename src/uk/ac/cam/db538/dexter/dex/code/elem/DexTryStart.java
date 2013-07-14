package uk.ac.cam.db538.dexter.dex.code.elem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.Getter;

public class DexTryStart extends DexCodeElement {

  @Getter private final int id;
  @Getter private final DexTryEnd endMarker;
  @Getter private final List<DexCatch> catchHandlers;
  @Getter private final DexCatchAll catchAllHandler;

  public DexTryStart(int id, DexTryEnd endMarker, DexCatchAll catchAllHandler, List<DexCatch> catchHandlers) {
    this.id = id;
    this.endMarker = endMarker;
    this.catchAllHandler = catchAllHandler;
    if (catchHandlers == null)
    	this.catchHandlers = Collections.emptyList();
    else
    	this.catchHandlers = Collections.unmodifiableList(new ArrayList<DexCatch>(catchHandlers));
  }

  @Override
  public String toString() {
    return "TRYSTART" + Integer.toString(this.id);
  }

  @Override
  public boolean cfgStartsBasicBlock() {
    return true;
  }
}
