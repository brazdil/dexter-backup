package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.HashSet;
import java.util.Set;

import lombok.val;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.elem.DexTryEnd;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.hierarchy.BaseClassDefinition;
import uk.ac.cam.db538.dexter.hierarchy.RuntimeHierarchy;

public abstract class DexInstruction extends DexCodeElement {

	protected final RuntimeHierarchy hierarchy; 
	
	protected DexInstruction(RuntimeHierarchy hierarchy) {
		this.hierarchy = hierarchy;
	}
	
  // PARSING

  protected static final InstructionParseError FORMAT_EXCEPTION = new InstructionParseError("Unknown instruction format or opcode");

  // INSTRUCTION INSTRUMENTATION

  public void instrument() {
    throw new UnsupportedOperationException("Instruction " + this.getClass().getSimpleName() + " doesn't have instrumentation implemented");
  }
  
  // THROWING INSTRUCTIONS

  @Override
  public final boolean cfgExitsMethod() {
	val jumpTargets = this.cfgJumpTargets(null);
	if (jumpTargets.isEmpty())
		return true;
    val exceptions = this.throwsExceptions();
    if (exceptions != null)
    	for (val exception : exceptions)
    		if (throwingInsn_CanExitMethod(exception))
    			return true;
    return false;
  }
  
  abstract public void accept(DexInstructionVisitor visitor);
  
  // Subclasses should overrides this if the instruction may throw exceptions during execution.
  protected DexClassType[] throwsExceptions() {
	return null;  
  }
  
  @Override
  public boolean cfgEndsBasicBlock() {
	DexClassType[] exceptions = throwsExceptions();
	return  exceptions != null && exceptions.length > 0;
  }

  @Override
  public final Set<DexCodeElement> cfgGetSuccessors() {
	  // uses the DexCodeElement definition of cfgGetSuccessors
	  // (non-throwing semantics) but adds behavior after exceptions
	  // are thrown
	  
	  val set = super.cfgGetSuccessors();
	  set.addAll(cfgGetExceptionSuccessors());
	  
	  return set;
  }
  
  @Override
  public final Set<DexCodeElement> cfgGetExceptionSuccessors() {
	  val set = new HashSet<DexCodeElement>();
	  
	  DexClassType[] exceptions = throwsExceptions();
	  if (exceptions != null) {
		  
		  //for(DexClassType exception : exceptions)
		  //  set.addAll(throwingInsn_CatchHandlers(exception));
		  // Instead of finding out precisely which catch handlers may 
		  // catch the thrown exceptions by the instruction, we assume 
		  // that it is possible for every catch handlers to catch all 
		  // throwing instructions. This is consistent with what dx is doing.
		  // Also, as an example, the following is VALID generated dalvik code, 
		  // but would not be handled correctly (*sigh*) if we track the 
		  // exception catchers more precisely 
		  // try {
		  //     const-string xxx (will only throw java.lang.Error)
		  // } catch (java.lang.Exception) {
		  //    // Dead code if we analyze it precisely 
		  // }
		  set.addAll(throwingInsn_CatchHandlers(null));
	  }
	  
	  return set;
  }

  static boolean fitsIntoBits_Signed(long value, int bits) {
	  assert bits > 0;
	  assert bits <= 64;

	  long upperBound = 1L << bits - 1;
	  return (value < upperBound) && (value >= -upperBound);
  }
}
