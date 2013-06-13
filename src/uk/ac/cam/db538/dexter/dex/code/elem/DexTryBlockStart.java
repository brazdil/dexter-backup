package uk.ac.cam.db538.dexter.dex.code.elem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;

public class DexTryBlockStart extends DexCodeElement {

  private static long TRYBLOCK_COUNTER = -1L;

  @Getter private final long originalAbsoluteOffset;

  @Getter @Setter private DexCatchAll catchAllHandler;
  private final List<DexCatch> catchHandlers;

  public DexTryBlockStart(DexCode methodCode, long originalAbsoluteOffset, DexCatchAll catchAllHandler, List<DexCatch> catchHandlers) {
    super(methodCode);

    this.originalAbsoluteOffset = originalAbsoluteOffset;
    this.catchAllHandler = catchAllHandler;
    this.catchHandlers = catchHandlers == null ? new ArrayList<DexCatch>() : new ArrayList<DexCatch>(catchHandlers);
  }

  public DexTryBlockStart(DexCode methodCode) {
    this(methodCode, TRYBLOCK_COUNTER, null, new ArrayList<DexCatch>());

    TRYBLOCK_COUNTER--;
    if (TRYBLOCK_COUNTER >= 0L)
      TRYBLOCK_COUNTER = -1L;
  }

  public DexTryBlockStart(DexTryBlockStart toClone) {
    this(toClone.getMethodCode(),
         toClone.originalAbsoluteOffset,
         toClone.catchAllHandler,
         toClone.catchHandlers);
  }

  public void addCatchHandler(DexCatch catchHandler) {
    catchHandlers.add(catchHandler);
  }

  @Override
  public String getOriginalAssembly() {
    return "TRY" + getOriginalAbsoluteOffsetString() + " {";
  }

  public String getOriginalAbsoluteOffsetString() {
    return Long.toString(originalAbsoluteOffset);
  }

  public List<DexCatch> getCatchHandlers() {
    return Collections.unmodifiableList(catchHandlers);
  }

  @Override
  public boolean cfgStartsBasicBlock() {
    return true;
  }

  @Override
  protected DexCodeElement gcReplaceWithTemporaries(Map<DexRegister, DexRegister> mapping, boolean toRefs, boolean toDefs) {
    return this;
  }
}
