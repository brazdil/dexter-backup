package uk.ac.cam.db538.dexter.dex.code;

import java.util.HashSet;
import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.CodeItem;

import uk.ac.cam.db538.dexter.dex.code.elem.DexTryEnd;
import uk.ac.cam.db538.dexter.dex.code.elem.DexTryStart;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Invoke;
import uk.ac.cam.db538.dexter.dex.code.reg.DexRegister;
import uk.ac.cam.db538.dexter.hierarchy.RuntimeHierarchy;

public class DexCode {

  @Getter private final RuntimeHierarchy hierarchy;
  @Getter private final InstructionList instructionList;

  public DexCode(CodeItem codeItem, RuntimeHierarchy hierarchy) {
	  this.hierarchy = hierarchy;
	  this.instructionList = CodeParser.parse(codeItem, hierarchy); 
  }
  
  public Set<DexRegister> getUsedRegisters() {
    val set = new HashSet<DexRegister>();
    for (val elem : instructionList)
      set.addAll(elem.lvaUsedRegisters());
    return set;
  }

  public Set<DexTryStart> getTryBlocks() {
    val set = new HashSet<DexTryStart>();
    for (val elem : instructionList)
      if (elem instanceof DexTryEnd)
        set.add((DexTryStart) elem);
    return set;
  }

  public int getOutWords() {
	  // outWords is the max of all inWords of methods in the code
	  int maxWords = 0;
	
	  for (val insn : this.instructionList) {
		  if (insn instanceof DexInstruction_Invoke) {
			  val insnInvoke = (DexInstruction_Invoke) insn;
			  int insnOutWords = insnInvoke.getMethodId().getPrototype().countParamWords(insnInvoke.getCallType().isStatic());
			  if (insnOutWords > maxWords)
				  maxWords = insnOutWords;
		  }
	  }

	  return maxWords;
  }
}
