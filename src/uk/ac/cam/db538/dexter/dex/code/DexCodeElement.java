package uk.ac.cam.db538.dexter.dex.code;

import java.util.HashSet;
import java.util.Set;

import lombok.Getter;
import lombok.val;
import uk.ac.cam.db538.dexter.analysis.coloring.GraphColoring.GcColorRange;
import uk.ac.cam.db538.dexter.dex.code.reg.DexRegister;
import uk.ac.cam.db538.dexter.utils.Pair;

public abstract class DexCodeElement {

  @Getter private DexCode MethodCode;

  public DexCodeElement(DexCode methodCode) {
    MethodCode = methodCode;
  }

  public abstract String getOriginalAssembly();

  protected DexCodeElement getNextCodeElement() {
    val insns = MethodCode.getInstructionList();

    int location = insns.indexOf(this);
    if (location < 0) // sanity check, should never happen
      throw new RuntimeException("Instruction not part of its DexCode");

    if (location + 1 < insns.size())
      return insns.get(location + 1);
    else
      return null;
  }

  // CONTROL FLOW GRAPHS
  //
  // The following methods represent default settings for code elements
  // and should be overridden in classes that need to be treated differently

  public boolean cfgStartsBasicBlock() {
    return false;
  }

  public boolean cfgEndsBasicBlock() {
    return false;
  }

  public boolean cfgExitsMethod() {
    return false;
  }

  public DexCodeElement[] cfgGetSuccessors() {
    val next = this.getNextCodeElement();
    if (next == null)
      return new DexCodeElement[] { };
    else
      return new DexCodeElement[] { next };
  }

  // LIVE VARIABLE ANALYSIS

  public Set<DexRegister> lvaDefinedRegisters() {
    return new HashSet<DexRegister>();
  }

  public Set<DexRegister> lvaReferencedRegisters() {
    return new HashSet<DexRegister>();
  }

  // GRAPH COLORING

  public static class GcRangeConstraint extends Pair<DexRegister, GcColorRange> {
    public GcRangeConstraint(DexRegister valA, GcColorRange valB) {
      super(valA, valB);
    }
  }

  public static class GcFollowConstraint extends Pair<DexRegister, DexRegister> {
    public GcFollowConstraint(DexRegister valA, DexRegister valB) {
      super(valA, valB);
    }
  }

  public Set<GcRangeConstraint> gcRangeConstraints() {
    return new HashSet<GcRangeConstraint>();
  }

  public Set<GcFollowConstraint> gcFollowConstraints() {
    return new HashSet<GcFollowConstraint>();
  }

  public DexCodeElement[] gcAddTemporaries() {
    return new DexCodeElement[] { this };
  }
}
