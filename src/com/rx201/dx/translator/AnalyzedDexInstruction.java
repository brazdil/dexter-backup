package com.rx201.dx.translator;

import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jf.dexlib.Code.Analysis.RegisterType;
import org.jf.dexlib.Code.Analysis.RegisterType.Category;

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
	    protected final HashMap<Integer, RegisterType> preRegisterMap;

	    /**
	     * This contains the register types *after* the instruction has executed
	     */
	    protected final HashMap<Integer, RegisterType> postRegisterMap;

	    /**
	     * An analyzed instruction's "deadness" is set during analysis (i.e. MethodAnalyzer.analyzer()). A dead instruction
	     * is one that the analyzer never reaches. This occurs either with natural "dead code" - code that simply has no
	     * code path that can ever reach it, or code that follows an odexed instruction that can't be deodexed.
	     */
	    protected boolean dead = false;

		private Dex parentFile;

		private final DexParsingCache cache;

	    static final RegisterType unknown = RegisterType.getRegisterType(RegisterType.Category.Unknown, null);

	    public final int instructionIndex;
	    
	    public AnalyzedDexInstruction(int index, DexInstruction instruction, Dex parentFile) {
	        this.instruction = instruction;
	        this.postRegisterMap = new HashMap<Integer, RegisterType>();
	        this.preRegisterMap = new HashMap<Integer, RegisterType>();
	        this.parentFile = parentFile;
	        this.cache = parentFile.getParsingCache();
	        this.instructionIndex = index;
	        this.auxillaryElement = null;
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

	    private RegisterType getPreRegister(int reg) {
	    	if (!preRegisterMap.containsKey(reg))
	    		preRegisterMap.put(reg, unknown);
	    	
	    	return preRegisterMap.get(reg);
	    }
	    
	    RegisterType peekPreRegister(int reg) {
	        return preRegisterMap.get(reg);
	    }

	    private RegisterType getPostRegister(int reg) {
	    	if (!postRegisterMap.containsKey(reg))
	    		postRegisterMap.put(reg, unknown);
	    	
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
	    protected boolean mergeRegister(DexRegister dexRegister, RegisterType registerType, BitSet verifiedInstructions) {
	    	int registerNumber = DexRegisterHelper.normalize(dexRegister);
	        assert registerType != null;

	        RegisterType oldRegisterType = getPreRegister(registerNumber);
	        RegisterType mergedRegisterType = TypeUnification.permissiveMerge(parentFile, oldRegisterType, registerType);
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
	        if(mergedRegisterType.category == Category.Conflicted && 
	        		oldRegisterType.category == Category.LongLo && registerType.category == Category.LongHi) {
	        	mergedRegisterType = registerType;
	        }
	        if (mergedRegisterType == oldRegisterType) {
	            return false;
	        }

	        preRegisterMap.put(registerNumber, mergedRegisterType);
	        verifiedInstructions.clear(instructionIndex);
	        
	        if (!setsRegister(dexRegister)) {
	            postRegisterMap.put(registerNumber, mergedRegisterType);
	            return true;
	        }

	        return false;
	    }

	    /**
	     * Iterates over the predecessors of this instruction, and merges all the post-instruction register types for the
	     * given register. Any dead, unreachable, or odexed predecessor is ignored
	     * @param registerNumber the register number
	     * @return The register type resulting from merging the post-instruction register types from all predecessors
	     */
	    protected RegisterType mergePreRegisterTypeFromPredecessors(DexRegister dexRegister) {
	    	int registerNumber = DexRegisterHelper.normalize(dexRegister);
	        RegisterType mergedRegisterType = null;
	        for (AnalyzedDexInstruction predecessor: predecessors) {
	            RegisterType predecessorRegisterType = predecessor.getPostRegister(registerNumber);
	            assert predecessorRegisterType != null;
	            mergedRegisterType = predecessorRegisterType.merge(mergedRegisterType);
	        }
	        return mergedRegisterType;
	    }

	    /*
	      * Sets the "post-instruction" register type as indicated.
	      * @param registerNumber Which register to set
	      * @param registerType The "post-instruction" register type
	      * @returns true if the given register type is different than the existing post-instruction register type
	      */
	     protected boolean setPostRegisterType(DexRegister dexRegister, RegisterType registerType) {
	    	 int registerNumber = DexRegisterHelper.normalize(dexRegister);
	         assert registerType != null;

	         RegisterType oldRegisterType = getPostRegister(registerNumber);
	         if (oldRegisterType == registerType) {
	             return false;
	         }

	         postRegisterMap.put(registerNumber, registerType);
	         return true;
	     }


	    protected boolean isInvokeInit() {
	        if (instruction == null || !(instruction instanceof DexInstruction_Invoke)) {
	            return false;
	        }
	        DexInstruction_Invoke inst = (DexInstruction_Invoke)instruction;
	        if (inst.getCallType() != Opcode_Invoke.Direct) {
	        	return false;
	        }

	        return inst.getMethodName().equals("<init>");
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
	        //When constructing a new object, the register type will be an uninitialized reference after the new-instance
	        //instruction, but becomes an initialized reference once the <init> method is called. So even though invoke
	        //instructions don't normally change any registers, calling an <init> method will change the type of its
	        //object register. If the uninitialized reference has been copied to other registers, they will be initialized
	        //as well, so we need to check for that too
	        if (isInvokeInit()) {
	            DexRegister destinationRegister;
	            DexInstruction_Invoke inst = (DexInstruction_Invoke)instruction;
	            destinationRegister = inst.getArgumentRegisters().get(0);

	            if (registerNumber == DexRegisterHelper.normalize(destinationRegister)) {
	                return true;
	            }
	            RegisterType preInstructionDestRegisterType = getPreRegister(registerNumber);
	            if (preInstructionDestRegisterType.category != RegisterType.Category.UninitRef &&
	                preInstructionDestRegisterType.category != RegisterType.Category.UninitThis) {

	                return false;
	            }
	            //check if the uninit ref has been copied to another register
	            // i.e. if dexRegister and destinationRegister hold the same uninit object instance.
	            if (getPreRegisterType(destinationRegister) == preInstructionDestRegisterType) {
	                return true;
	            }
	            return false;
	        }

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

	    public RegisterType getPostRegisterType(DexRegister dexRegister) {
	    	int registerNumber = DexRegisterHelper.normalize(dexRegister);
	    	return getPostRegisterType(registerNumber);
	    }
	    
	    public RegisterType getPostRegisterType(int registerNumber) {
	    	return getPostRegister(registerNumber);
	    }
	    
	    public RegisterType getPreRegisterType(DexRegister dexRegister) {
	    	int registerNumber = DexRegisterHelper.normalize(dexRegister);
	    	return getPreRegister(registerNumber);
	    }
	    
	    public RegisterType getPreRegisterType(int registerNumber) {
	    	return getPreRegisterType(registerNumber);
	    }
	    
	    public int getInstructionIndex() {
	    	return instructionIndex;
	    }
	    
}
