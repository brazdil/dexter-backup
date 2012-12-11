package uk.ac.cam.db538.dexter.dex.code.elem;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;

import lombok.Getter;
import lombok.Setter;

public class DexTryBlockStart extends DexCodeElement {

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
    this(methodCode, -1, null, new HashSet<DexCatch>());
  }

  public void addCatchHandler(DexCatch catchHandler) {
    catchHandlers.add(catchHandler);
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

  public Set<DexCatch> getCatchHandlers() {
    return Collections.unmodifiableSet(catchHandlers);
  }

  @Override
  public boolean cfgStartsBasicBlock() {
    return true;
  }

  @Override
  protected DexCodeElement gcReplaceWithTemporaries(Map<DexRegister, DexRegister> mapping) {
    return this;
  }
}
