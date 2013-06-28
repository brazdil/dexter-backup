package com.rx201.dx.translator;

import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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
	    protected final HashMap<Integer, RopType> preRegisterMap;

	    /**
	     * This contains the register types *after* the instruction has executed
	     */
	    protected final HashMap<Integer, RopType> postRegisterMap;

	    /**
	     * An analyzed instruction's "deadness" is set during analysis (i.e. MethodAnalyzer.analyzer()). A dead instruction
	     * is one that the analyzer never reaches. This occurs either with natural "dead code" - code that simply has no
	     * code path that can ever reach it, or code that follows an odexed instruction that can't be deodexed.
	     */
	    protected boolean dead = false;

		private Dex parentFile;

		private final DexParsingCache cache;

	    public final int instructionIndex;
	    
		protected HashMap<Integer, RopType> useSet;
		protected HashMap<Integer, RopType> defSet;
		// <regSource, regDestination>
		protected HashMap<Integer, Integer> moveSet;
	    
	    
	    public AnalyzedDexInstruction(int index, DexInstruction instruction, Dex parentFile) {
	        this.instruction = instruction;
	        this.postRegisterMap = new HashMap<Integer, RopType>();
	        this.preRegisterMap = new HashMap<Integer, RopType>();
	        this.parentFile = parentFile;
	        this.cache = parentFile.getParsingCache();
	        this.instructionIndex = index;
	        this.auxillaryElement = null;
	        
			useSet = new HashMap<Integer, RopType>();
			defSet = new HashMap<Integer, RopType>();
			moveSet = new HashMap<Integer, Integer>();
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

	    public boolean isDead() {
	        return dead;
	    }

	    private RopType getPreRegister(int reg) {
	    	if (!preRegisterMap.containsKey(reg))
	    		preRegisterMap.put(reg, RopType.Unknown);
	    	
	    	return preRegisterMap.get(reg);
	    }
	    
	    RopType peekPreRegister(int reg) {
	        return preRegisterMap.get(reg);
	    }

	    private RopType getPostRegister(int reg) {
	    	if (!postRegisterMap.containsKey(reg))
	    		postRegisterMap.put(reg, RopType.Unknown);
	    	
	    	return postRegisterMap.get(reg);
	    }
	    /*
	     * Merges the given register type into the specified pre-instruction register, and also sets the post-instruction
	     * register type accordingly if it isn't a destination register for this instruction
	     * @param registerNumber Which register to set
	     * @param registerType The register type
	     * @returns true If the post-instruction register type was changed. This might be false if either the specified
	     * register is a destination register for this instruction, or if the pre-instruction register type didn't change
	     * after merging in the given register type
	     */
	    protected boolean mergeRegister(DexRegister dexRegister, RopType registerType, BitSet verifiedInstructions) {
	    	int registerNumber = DexRegisterHelper.normalize(dexRegister);
	        assert registerType != null;

	        RopType oldRegisterType = getPreRegister(registerNumber);
	        RopType mergedRegisterType = oldRegisterType.merge(registerType);
	        //TODO: UGLY HACK WARNING
	        /* 
	         * in aosp/027/arithmatic.
	         * v6: object reference 
	         * const/16 v5, 0x8
	         * shl-long/2addr v3, v5
	         * This is valid ?? It would create conflicted type information after merging.
	         * Maybe an alternative is to disable setting types LongHi/DoubleHi to shl-long's
	         * second operand?
	         */
	        // This instruction to be valid as well: or-long v0, v1, v3, so account for it.
//	        if(mergedRegisterType.category == Category.Conflicted && 
//	        		oldRegisterType.category == Category.LongLo && registerType.category == Category.LongHi) {
//	        	mergedRegisterType = registerType;
//	        }
	        if (mergedRegisterType == oldRegisterType) {
	            return false;
	        }

	        preRegisterMap.put(registerNumber, mergedRegisterType);
	        RopType t = getUsedRegType(registerNumber);
	        if (t != null && t.merge(mergedRegisterType).category == Category.Conflicted) {
	        	assert false;
	        }
	        
	        verifiedInstructions.clear(instructionIndex);
	        
	        if (!setsRegister(dexRegister)) {
	            postRegisterMap.put(registerNumber, mergedRegisterType);
	            return true;
	        }

	        return false;
	    }

	    /*
	      * Sets the "post-instruction" register type as indicated.
	      * @param registerNumber Which register to set
	      * @param registerType The "post-instruction" register type
	      * @returns true if the given register type is different than the existing post-instruction register type
	      */
	     protected boolean setPostRegisterType(DexRegister dexRegister, RopType registerType) {
	    	 int registerNumber = DexRegisterHelper.normalize(dexRegister);
	         assert registerType != null;

	         RopType oldRegisterType = getPostRegister(registerNumber);
	         if (oldRegisterType == registerType) {
	             return false;
	         }

	         postRegisterMap.put(registerNumber, registerType);
	         RopType t = getDefinedRegType(registerNumber);
	         if (t != null && t.merge(registerType).category == Category.Conflicted) {
	        	 assert false;
	         }
	         return true;
	     }

	    public boolean setsRegister() {
	    	if (instruction == null)
	    		return false;
	    	else
	    		return instruction.lvaDefinedRegisters().size() > 0;
	    }

	    public boolean setsWideRegister() {
	    	if (instruction == null)
	    		return false;
	    	else
	    		return instruction.lvaDefinedRegisters().size() > 1;
	    }

	    public boolean setsRegister(DexRegister dexRegister) {
	    	int registerNumber = DexRegisterHelper.normalize(dexRegister);

	        if (!setsRegister()) {
	            return false;
	        }
	        DexRegister destinationRegister = getDestinationRegister();

	        if (registerNumber == DexRegisterHelper.normalize(destinationRegister)) {
	            return true;
	        }
	        if (setsWideRegister() && registerNumber == DexRegisterHelper.normalize(destinationRegister) + 1) {
	            return true;
	        }
	        return false;
	    }

	    public DexRegister getDestinationRegister() {
	        if (!setsRegister()) {
	            throw new RuntimeException("Cannot call getDestinationRegister() for an instruction that doesn't " +
	                    "store a value");
	        }
	        Iterator<DexRegister> iter = instruction.lvaDefinedRegisters().iterator();
	        DexRegister r = iter.next();
	        if (iter.hasNext()) {
	        	DexRegister r1 = iter.next();
	        	if (DexRegisterHelper.normalize(r1) < DexRegisterHelper.normalize(r))
	        		r = r1;
	        }
	        assert !iter.hasNext();
	        return r;
	    }

	    public int getRegisterCount() {
	        return postRegisterMap.size();
	    }

	    public Set<Integer> getPostRegisters() {
	    	return postRegisterMap.keySet();
	    }

	    public RopType getPostRegisterType(DexRegister dexRegister) {
	    	int registerNumber = DexRegisterHelper.normalize(dexRegister);
	    	return getPostRegisterType(registerNumber);
	    }
	    
	    public RopType getPostRegisterType(int registerNumber) {
	    	return getPostRegister(registerNumber);
	    }
	    
	    public RopType getPreRegisterType(DexRegister dexRegister) {
	    	int registerNumber = DexRegisterHelper.normalize(dexRegister);
	    	return getPreRegister(registerNumber);
	    }
	    
	    public RopType getPreRegisterType(int registerNumber) {
	    	return getPreRegisterType(registerNumber);
	    }
	    
	    public int getInstructionIndex() {
	    	return instructionIndex;
	    }
	    
	    private HashSet<DexRegister> definedRegisters;
		public void defineRegister(DexRegister regTo, RopType registerType) {
			definedRegisters.add(regTo);
			defSet.put(DexRegisterHelper.normalize(regTo), registerType);
		}
		public RopType getDefinedRegType(int regTo) {
			if (defSet.containsKey(regTo))
				return defSet.get(regTo);
			else
				return null;
		}
		public HashSet<DexRegister> getDefinedRegisters() {
			return definedRegisters;
		}
		public void useRegister(DexRegister regFrom, RopType registerType) {
			useSet.put(DexRegisterHelper.normalize(regFrom), registerType);
		}
		public RopType getUsedRegType(int regFrom) {
			if (useSet.containsKey(regFrom))
				return useSet.get(regFrom);
			else
				return null;
		}
		
		public void moveRegister(DexRegister regFrom, DexRegister regTo) {
			moveSet.put(DexRegisterHelper.normalize(regFrom), DexRegisterHelper.normalize(regTo));
		}
		
		public int getMovedReg(int regFrom) {
			if (moveSet.containsKey(regFrom))
				return moveSet.get(regFrom);
			else
				return -1;
		}
			    
}
