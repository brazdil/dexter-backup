package com.rx201.dx.translator;

import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.rx201.dx.translator.RopType.Category;
import com.rx201.dx.translator.util.DexRegisterHelper;

import uk.ac.cam.db538.dexter.dex.Dex;
import uk.ac.cam.db538.dexter.dex.DexParsingCache;
import uk.ac.cam.db538.dexter.dex.code.DexParameterRegister;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Invoke;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_Invoke;
import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;
import uk.ac.cam.db538.dexter.utils.Pair;

public class AnalyzedDexInstruction {

	    /**
	     * The actual instruction
	     */
	    protected final DexInstruction instruction;
	    protected DexCodeElement auxillaryElement;

	    /**
	     * Instructions that can pass on execution to this one during normal execution
	     */
	    protected final LinkedList<AnalyzedDexInstruction> predecessors = new LinkedList<AnalyzedDexInstruction>();

	    /**
	     * Instructions that can execution could pass on to next during normal execution
	     */
	    protected final LinkedList<AnalyzedDexInstruction> successors = new LinkedList<AnalyzedDexInstruction>();

	    /**
	     * This contains the register types *before* the instruction has executed
	     */
	    protected final HashSet<DexRegister> usedRegisters;
	    protected final HashMap<Integer, TypeSolver> usedRegisterMap;

	    /**
	     * This contains the register types *after* the instruction has executed
	     */
	    protected final HashSet<DexRegister> definedRegisters;
	    protected final HashMap<Integer, TypeSolver> definedRegisterMap;


	    protected final HashMap<Integer, Pair<Integer, TypeSolver.CascadeType>> constrainedRegisters;
	    
	    
		private Dex parentFile;

		private final DexParsingCache cache;

	    public final int instructionIndex;
	    
		protected HashMap<Integer, RopType> useSet;
		protected HashMap<Integer, RopType> defSet;
		// <regSource, regDestination>
		protected HashMap<Integer, Integer> moveSet;
	    
	    
	    public AnalyzedDexInstruction(int index, DexInstruction instruction, Dex parentFile) {
	        this.instruction = instruction;
	        this.usedRegisterMap = new HashMap<Integer, TypeSolver>();
	        this.definedRegisterMap = new HashMap<Integer, TypeSolver>();
	        this.constrainedRegisters = new HashMap<Integer, Pair<Integer, TypeSolver.CascadeType>>();
	        this.parentFile = parentFile;
	        this.cache = parentFile.getParsingCache();
	        this.instructionIndex = index;
	        this.auxillaryElement = null;
	        
			useSet = new HashMap<Integer, RopType>();
			defSet = new HashMap<Integer, RopType>();
			moveSet = new HashMap<Integer, Integer>();
			usedRegisters = new HashSet<DexRegister>();
			definedRegisters = new HashSet<DexRegister>();
	        
	    }

	    public AnalyzedDexInstruction(int index, DexInstruction instruction, DexCodeElement element, Dex parentFile) {
	    	this(index, instruction, parentFile);
	    	this.auxillaryElement = element;
	    }

	    public int getPredecessorCount() {
	        return predecessors.size();
	    }

	    public List<AnalyzedDexInstruction> getPredecessors() {
	        return Collections.unmodifiableList(predecessors);
	    }

	    protected boolean addPredecessor(AnalyzedDexInstruction predecessor) {
	        return predecessors.add(predecessor);
	    }

	    protected void addSuccessor(AnalyzedDexInstruction successor) {
	        successors.add(successor);
	    }

	    public int getSuccessorCount() {
	        return successors.size();
	    }

	    public List<AnalyzedDexInstruction> getSuccesors() {
	        return Collections.unmodifiableList(successors);
	    }

	    public AnalyzedDexInstruction getOnlySuccesor() {
	    	assert successors.size() == 1;
	    	return successors.get(0);
	    }

	    public DexInstruction getInstruction() {
	        return instruction;
	    }

		public Set<DexRegister> getUsedRegisters() {
			return usedRegisters;
		}
		
	    public RopType getUsedRegisterType(int reg) {
	    	return usedRegisterMap.get(reg).getType();
	    }
	    
	    public RopType getUsedRegisterType(DexRegister dexRegister) {
	    	int registerNumber = DexRegisterHelper.normalize(dexRegister);
	    	return getUsedRegisterType(registerNumber);
	    }

	    public void associateDefinitionSite(DexRegister usedReg, TypeSolver definition) {
	    	int registerNumber = DexRegisterHelper.normalize(usedReg);
	    	assert useSet.containsKey(registerNumber);
	    	usedRegisterMap.put(registerNumber, definition);
	    }

	    public RopType getDefinedRegisterType(int reg) {
	    	return definedRegisterMap.get(reg).getType();
	    }

	    public RopType getDefinedRegisterType(DexRegister dexRegister) {
	    	int registerNumber = DexRegisterHelper.normalize(dexRegister);
	    	return getDefinedRegisterType(registerNumber);
	    }
	    
	    public TypeSolver getDefinedRegisterSolver(DexRegister dexRegister) {
	    	int registerNumber = DexRegisterHelper.normalize(dexRegister);
	    	if (definedRegisterMap.containsKey(registerNumber))
	    		return definedRegisterMap.get(registerNumber);
	    	else
	    		return null;
	    	
	    }

	    public int getInstructionIndex() {
	    	return instructionIndex;
	    }
	    
		public void defineRegister(DexRegister regTo, RopType registerType) {
			int registerNumber = DexRegisterHelper.normalize(regTo);
			definedRegisters.add(regTo);
			definedRegisterMap.put(registerNumber, new TypeSolver());
			defSet.put(registerNumber, registerType);
		}
		
		public void useRegister(DexRegister regFrom, RopType registerType) {
			usedRegisters.add(regFrom);
			useSet.put(DexRegisterHelper.normalize(regFrom), registerType);
		}
		
		public void addRegisterConstraint(DexRegister regTo, DexRegister regFrom, TypeSolver.CascadeType type) {
			constrainedRegisters.put(DexRegisterHelper.normalize(regTo), 
					new Pair<Integer, TypeSolver.CascadeType>(DexRegisterHelper.normalize(regFrom), type));
		}

		public void createConstraintEdges() {
			for(Entry<Integer, Pair<Integer, TypeSolver.CascadeType>> constraint : constrainedRegisters.entrySet()) {
				TypeSolver target = definedRegisterMap.get(constraint.getKey());
				TypeSolver source = usedRegisterMap.get(constraint.getValue().getValA());
				target.addDependingTS(source, constraint.getValue().getValB());
			}
		}
		
		public void propagateDefinitionConstraints() {
			for(Entry<Integer, RopType> constraint : defSet.entrySet()) {
				TypeSolver target = definedRegisterMap.get(constraint.getKey());
				RopType type = constraint.getValue();
				target.addConstraint(type, false);
			}
		}

		public void propagateUsageConstraints() {
			for(Entry<Integer, RopType> constraint : useSet.entrySet()) {
				TypeSolver target = usedRegisterMap.get(constraint.getKey());
				RopType type = constraint.getValue();
				target.addConstraint(type, false);
			}
		}

		
}
