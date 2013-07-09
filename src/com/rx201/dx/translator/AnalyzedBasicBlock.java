package com.rx201.dx.translator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import uk.ac.cam.db538.dexter.dex.code.DexRegister;

import com.rx201.dx.translator.util.DexRegisterHelper;

class AnalyzedBasicBlock {
	class RegUsage {
		public ArrayList<AnalyzedDexInstruction> sites;
		public ArrayList<DexRegister> regs;
		public RegUsage() {
			sites = new ArrayList<AnalyzedDexInstruction>();
			regs = new ArrayList<DexRegister>();
		}
	}
	protected final ArrayList<AnalyzedBasicBlock> predecessors;
    protected final Set<AnalyzedBasicBlock> exceptionPredecessors;
    protected final ArrayList<AnalyzedBasicBlock> successors;
	
	public AnalyzedDexInstruction first;
	public AnalyzedDexInstruction last;
	
    protected final HashSet<Integer> usedRegisters;
    protected final HashMap<Integer, RegUsage> usedRegisterMap;

    protected final HashSet<Integer> definedRegisters;
    protected final HashMap<Integer, TypeSolver> definedRegisterMap;
    
    public AnalyzedBasicBlock(AnalyzedDexInstruction instruction) {
    	predecessors = new ArrayList<AnalyzedBasicBlock>();
    	successors = new ArrayList<AnalyzedBasicBlock>();
    	exceptionPredecessors = new HashSet<AnalyzedBasicBlock>();
    	
    	usedRegisters = new HashSet<Integer>();
    	usedRegisterMap = new HashMap<Integer, RegUsage>();
    	
    	definedRegisters = new HashSet<Integer>();
    	definedRegisterMap = new HashMap<Integer, TypeSolver>();
    	
    	first = last = instruction;
    }
    
    public AnalyzedBasicBlock() {
    	this(null);
    }
    
    public void addInstruction(AnalyzedDexInstruction instruction) {
    	if (first == null)
    		first = instruction;
    	
    	last = instruction;
    }
    
    public void linkToSuccessor(AnalyzedBasicBlock next, boolean exceptionPath) {
    	this.successors.add(next);
    	next.predecessors.add(this);
    	if (exceptionPath) 
    		next.exceptionPredecessors.add(this);    	
    }
    
    // Perform liveness analysis within this basic block, linking internal use-def
    // TypeSolver sites and computing external use & def set.
    public void analyzeLiveness() {
    	assert predecessors.size() == first.getPredecessorCount();
    	assert successors.size() == last.getSuccessorCount();
    		
    	AnalyzedDexInstruction current = first;
    	
    	usedRegisters.clear();
    	usedRegisterMap.clear();
    	definedRegisters.clear();
    	definedRegisterMap.clear();
    	while (true) {
    		if (current != first) 
        		assert current.getPredecessorCount() == 1;
    		if (current != last)
        		assert current.getSuccessorCount() == 1;
    			
    		Set<DexRegister> curUseSet = current.getUsedRegisters();
    		for(DexRegister useReg : curUseSet) {
    			int regNum = DexRegisterHelper.normalize(useReg);
    			if (definedRegisters.contains(regNum)) {
    				TypeSolver defSite = definedRegisterMap.get(regNum);
    				current.associateDefinitionSite(useReg, defSite);
    			} else {
    				usedRegisters.add(regNum);
    				if (!usedRegisterMap.containsKey(regNum))
    					usedRegisterMap.put(regNum, new RegUsage());
    				RegUsage usageList = usedRegisterMap.get(regNum);
    				usageList.sites.add(current);
    				usageList.regs.add(useReg);
    			}
    		}    		
    		
    		Set<DexRegister> curDefSet = current.getDefinedRegisters();
    		for(DexRegister defReg : curDefSet) {
    			int regNum = DexRegisterHelper.normalize(defReg);
    			definedRegisters.add(regNum);
    			definedRegisterMap.put(regNum, current.getDefinedRegisterSolver(defReg));
    		}
    		if (current != last)
    			current = current.getOnlySuccesor();
    		else
    			break;
    	}
    }

	public Set<Integer> getUsedRegisters() {
		return usedRegisters;
	}

	public void associateDefinitionSite(Integer usedReg, TypeSolver master) {
		RegUsage usageList = usedRegisterMap.get(usedReg);
		for(int i=0; i<usageList.sites.size(); i++) {
			usageList.sites.get(i).associateDefinitionSite(usageList.regs.get(i), master);
		}
	}

	public Boolean isExceptionPredecessor(AnalyzedBasicBlock pred) {
		return exceptionPredecessors.contains(pred);
	}

	public TypeSolver getDefinedRegisterSolver(Integer usedReg) {
		if (!definedRegisterMap.containsKey(usedReg))
			return null;
		return definedRegisterMap.get(usedReg);
	}
	
}