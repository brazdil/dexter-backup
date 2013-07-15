package uk.ac.cam.db538.dexter.dex.code;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Invoke;
import uk.ac.cam.db538.dexter.dex.code.reg.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.reg.DexStandardRegister;
import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;
import uk.ac.cam.db538.dexter.hierarchy.RuntimeHierarchy;
import uk.ac.cam.db538.dexter.utils.Utils;

public class DexCode {

  @Getter private final RuntimeHierarchy hierarchy;
  @Getter private final InstructionList instructionList;
  @Getter private final List<Parameter> parameters;
  
  public DexCode(InstructionList instructionList, List<Parameter> parameters, RuntimeHierarchy hierarchy) {
	  this.instructionList = instructionList;
	  this.parameters = Utils.finalList(parameters);
	  this.hierarchy = hierarchy;
  }

  public Set<DexRegister> getUsedRegisters() {
	    val set = new HashSet<DexRegister>();
	    for (val elem : instructionList)
	      set.addAll(elem.lvaUsedRegisters());
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

	  @AllArgsConstructor
	  @Getter
	  public static class Parameter {
		  private final DexRegisterType type;
		  private final DexStandardRegister register;
	  }
}
