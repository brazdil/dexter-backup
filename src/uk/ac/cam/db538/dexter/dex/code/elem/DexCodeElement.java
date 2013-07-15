package uk.ac.cam.db538.dexter.dex.code.elem;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import lombok.val;
import uk.ac.cam.db538.dexter.dex.code.InstructionList;
import uk.ac.cam.db538.dexter.dex.code.reg.DexRegister;

import com.google.common.collect.Sets;

public abstract class DexCodeElement {

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

  protected Set<? extends DexCodeElement> cfgJumpTargets(InstructionList code) {
	return Sets.newHashSet(code.getFollower(this));
  }

  public Set<DexCodeElement> cfgGetSuccessors() {
    val set = new HashSet<DexCodeElement>();
    for (DexCodeElement target : cfgJumpTargets(null))
    	set.add(target);
    return set;
  }
  
  public Set<? extends DexCodeElement> cfgGetExceptionSuccessors(InstructionList code) {
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
