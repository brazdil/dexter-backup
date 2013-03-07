package uk.ac.cam.db538.dexter.dex.code.elem;

import java.util.Collections;
import java.util.HashSet;
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
  private final Set<DexCatch> catchHandlers;

  public DexTryBlockStart(DexCode methodCode, long originalAbsoluteOffset, DexCatchAll catchAllHandler, Set<DexCatch> catchHandlers) {
    super(methodCode);

    this.originalAbsoluteOffset = originalAbsoluteOffset;
    this.catchAllHandler = catchAllHandler;
    this.catchHandlers = catchHandlers == null ? new HashSet<DexCatch>() : new HashSet<DexCatch>(catchHandlers);
  }

  public DexTryBlockStart(DexCode methodCode) {
    this(methodCode, TRYBLOCK_COUNTER, null, new HashSet<DexCatch>());

    TRYBLOCK_COUNTER--;
    if (TRYBLOCK_COUNTER >= 0L)
      TRYBLOCK_COUNTER = -1L;
  }

  public DexTryBlockStart(DexTryBlockStart toClone) {
    this(toClone.getMethodCode(),
         toClone.originalAbsoluteOffset,
         toClone.catchAllHandler,
         new HashSet<DexCatch>(toClone.catchHandlers));
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

  public Set<DexCatch> getCatchHandlers() {
    return Collections.unmodifiableSet(catchHandlers);
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
