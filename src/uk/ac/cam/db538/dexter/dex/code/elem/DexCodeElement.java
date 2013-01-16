package uk.ac.cam.db538.dexter.dex.code.elem;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import lombok.val;
import uk.ac.cam.db538.dexter.analysis.coloring.ColorRange;
import uk.ac.cam.db538.dexter.dex.Dex;
import uk.ac.cam.db538.dexter.dex.DexClass;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Move;
import uk.ac.cam.db538.dexter.dex.method.DexMethodWithCode;
import uk.ac.cam.db538.dexter.utils.Pair;

public abstract class DexCodeElement {

  @Getter private final DexCode methodCode;
  @Getter @Setter private boolean originalElement = false;
  @Getter @Setter private boolean auxiliaryElement = false;

  public DexCodeElement(DexCode methodCode) {
    this.methodCode = methodCode;
  }

  public abstract String getOriginalAssembly();

  public final DexCodeElement getNextCodeElement() {
    val insns = methodCode.getInstructionList();

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

  public Set<DexCodeElement> cfgGetSuccessors() {
    val set = new HashSet<DexCodeElement>();

    val next = this.getNextCodeElement();
    if (next != null)
      set.add(next);

    return set;
  }

  // LIVE VARIABLE ANALYSIS

  public Set<DexRegister> lvaDefinedRegisters() {
    return Collections.emptySet();
  }

  public Set<DexRegister> lvaReferencedRegisters() {
    return Collections.emptySet();
  }

  public final Set<DexRegister> lvaUsedRegisters() {
    val set = new HashSet<DexRegister>();
    set.addAll(lvaDefinedRegisters());
    set.addAll(lvaReferencedRegisters());
    return set;
  }

  // GRAPH COLORING

  public static class GcRangeConstraint extends Pair<DexRegister, ColorRange> {
    public GcRangeConstraint(DexRegister valA, ColorRange valB) {
      super(valA, valB);
    }
  }

  public static class GcFollowConstraint extends Pair<DexRegister, DexRegister> {
    public GcFollowConstraint(DexRegister valA, DexRegister valB) {
      super(valA, valB);
    }
  }

  public Set<GcRangeConstraint> gcRangeConstraints() {
    return Collections.emptySet();
  }

  public Set<GcFollowConstraint> gcFollowConstraints() {
    return Collections.emptySet();
  }

  public final List<DexCodeElement> gcAddTemporaries(Collection<DexRegister> spilledRegs) {
    val tempMapping = new HashMap<DexRegister, DexRegister>();
    val methodCode = getMethodCode();

    for (val usedReg : lvaUsedRegisters())
      if (spilledRegs.contains(usedReg))
        tempMapping.put(usedReg, new DexRegister());
      else
        tempMapping.put(usedReg, usedReg);

    val referencedRegs = lvaReferencedRegisters();
    val definedRegs = lvaDefinedRegisters();

    val newElem = new LinkedList<DexCodeElement>();
    for (val spilledReg : spilledRegs)
      if (referencedRegs.contains(spilledReg))
        newElem.add(new DexInstruction_Move(methodCode, tempMapping.get(spilledReg), spilledReg, false));
    newElem.add(gcReplaceWithTemporaries(tempMapping));
    for (val spilledReg : spilledRegs)
      if (definedRegs.contains(spilledReg))
        newElem.add(new DexInstruction_Move(methodCode, spilledReg, tempMapping.get(spilledReg), false));

    return newElem;
  }

  protected abstract DexCodeElement gcReplaceWithTemporaries(Map<DexRegister, DexRegister> mapping);

  // UTILS

  @SafeVarargs
  protected final <T> Set<T> createSet(T ... members) {
    return new HashSet<T>(Arrays.asList(members));
  }

  @SafeVarargs
  protected final <T> List<T> createList(T ... elements) {
    return Arrays.asList(elements);
  }

  public Dex getParentFile() {
    return methodCode.getParentFile();
  }

  public DexClass getParentClass() {
    return methodCode.getParentClass();
  }

  public DexMethodWithCode getParentMethod() {
    return methodCode.getParentMethod();
  }
}
