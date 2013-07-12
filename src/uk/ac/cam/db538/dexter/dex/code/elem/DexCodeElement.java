package uk.ac.cam.db538.dexter.dex.code.elem;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.val;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.reg.DexRegister;

public abstract class DexCodeElement {

  public final DexCodeElement getNextCodeElement(DexCode methodCode) {
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

  protected Set<? extends DexCodeElement> cfgJumpTargets() {
    val set = new HashSet<DexCodeElement>();
	val next = this.getNextCodeElement();
    if (next != null)
    	set.add(next);
    return set;
  }

  public Set<DexCodeElement> cfgGetSuccessors() {
    val set = new HashSet<DexCodeElement>();
    for (DexCodeElement target : cfgJumpTargets())
    	set.add(target);
    return set;
  }
  
  public Set<DexCodeElement> cfgGetExceptionSuccessors() {
	  return Collections.emptySet();
  }

  // LIVE VARIABLE ANALYSIS

  public Set<? extends DexRegister> lvaDefinedRegisters() {
    return Collections.emptySet();
  }

  public Set<? extends DexRegister> lvaReferencedRegisters() {
    return Collections.emptySet();
  }

  public final Set<? extends DexRegister> lvaUsedRegisters() {
    val set = new HashSet<DexRegister>();
    set.addAll(lvaDefinedRegisters());
    set.addAll(lvaReferencedRegisters());
    return set;
  }
}
