package uk.ac.cam.db538.dexter.dex.code;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.Getter;

public class DexTryBlockStart extends DexCodeElement {

  @Getter private final long originalAbsoluteOffset;

  @Getter private final DexCatchAll catchAllHandler;
  private final List<DexCatch> catchHandlers;

  public DexTryBlockStart(DexCode methodCode, long originalAbsoluteOffset, DexCatchAll catchAllHandler, List<DexCatch> catchHandlers) {
    super(methodCode);

    this.originalAbsoluteOffset = originalAbsoluteOffset;
    this.catchAllHandler = catchAllHandler;
    this.catchHandlers = catchHandlers == null ? new ArrayList<DexCatch>() : new ArrayList<DexCatch>(catchHandlers);
  }

  public DexTryBlockStart(DexCode methodCode) {
    super(methodCode);

    this.originalAbsoluteOffset = -1;
    this.catchAllHandler = null;
    this.catchHandlers = new ArrayList<DexCatch>();
  }

  @Override
  public String getOriginalAssembly() {
    return "TRY" + getOriginalAbsoluteOffsetString() + " {";
  }

  public String getOriginalAbsoluteOffsetString() {
    if (originalAbsoluteOffset >= 0)
      return Long.toString(originalAbsoluteOffset);
    else
      return "???";
  }

  public List<DexCatch> getCatchHandlers() {
    return Collections.unmodifiableList(catchHandlers);
  }

  @Override
  public boolean cfgStartsBasicBlock() {
    return true;
  }
//
//  public static Cache<Long, DexTryBlockStart> createCache(final DexCode code) {
//    return new Cache<Long, DexTryBlockStart>() {
//      @Override
//      protected DexTryBlockStart createNewEntry(Long absoluteOffset) {
//        return new DexTryBlockStart(code, absoluteOffset);
//      }
//    };
//  }
}
