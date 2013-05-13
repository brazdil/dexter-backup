package com.rx201.dx.translator;

import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.jf.dexlib.Code.Analysis.RegisterType;

import uk.ac.cam.db538.dexter.dex.DexParsingCache;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Invoke;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_Invoke;
import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;

public class AnalyzedDexInstruction {

	    /**
	     * The actual instruction
	     */
	    protected final DexInstruction instruction;


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
	    protected final HashMap<DexRegister, RegisterType> preRegisterMap;

	    /**
	     * This contains the register types *after* the instruction has executed
	     */
	    protected final HashMap<DexRegister, RegisterType> postRegisterMap;

	    /**
	     * An analyzed instruction's "deadness" is set during analysis (i.e. MethodAnalyzer.analyzer()). A dead instruction
	     * is one that the analyzer never reaches. This occurs either with natural "dead code" - code that simply has no
	     * code path that can ever reach it, or code that follows an odexed instruction that can't be deodexed.
	     */
	    protected boolean dead = false;

		private final DexParsingCache cache;

	    private static final RegisterType unknown = RegisterType.getRegisterType(RegisterType.Category.Unknown, null);

	    public final int instructionIndex;
	    
	    public AnalyzedDexInstruction(int index, DexInstruction instruction, DexParsingCache cache) {
	        this.instruction = instruction;
	        this.postRegisterMap = new HashMap<DexRegister, RegisterType>();
	        this.preRegisterMap = new HashMap<DexRegister, RegisterType>();
	        this.cache = cache;
	        this.instructionIndex = index;
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

	    public DexInstruction getInstruction() {
	        return instruction;
	    }

	    public boolean isDead() {
	        return dead;
	    }

	    private RegisterType getPreRegister(DexRegister reg) {
	    	if (!preRegisterMap.containsKey(reg))
	    		preRegisterMap.put(reg, unknown);
	    	
	    	return preRegisterMap.get(reg);
	    }

	    private RegisterType getPostRegister(DexRegister reg) {
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
	    protected boolean mergeRegister(DexRegister registerNumber, RegisterType registerType, BitSet verifiedInstructions) {
	        assert registerType != null;

	        RegisterType oldRegisterType = getPreRegister(registerNumber);
	        RegisterType mergedRegisterType = oldRegisterType.merge(registerType);

	        if (mergedRegisterType == oldRegisterType) {
	            return false;
	        }

	        preRegisterMap.put(registerNumber, mergedRegisterType);
	        verifiedInstructions.clear(instructionIndex);
	        
	        if (!setsRegister(registerNumber)) {
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
	    protected RegisterType mergePreRegisterTypeFromPredecessors(DexRegister registerNumber) {
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
	     protected boolean setPostRegisterType(DexRegister registerNumber, RegisterType registerType) {
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
	        return instruction.lvaDefinedRegisters().size() > 0;
	    }

	    public boolean setsWideRegister() {
	        return instruction.lvaDefinedRegisters().size() > 1;
	    }

	    public boolean setsRegister(DexRegister registerNumber) {
	        //When constructing a new object, the register type will be an uninitialized reference after the new-instance
	        //instruction, but becomes an initialized reference once the <init> method is called. So even though invoke
	        //instructions don't normally change any registers, calling an <init> method will change the type of its
	        //object register. If the uninitialized reference has been copied to other registers, they will be initialized
	        //as well, so we need to check for that too
	        if (isInvokeInit()) {
	            DexRegister destinationRegister;
	            DexInstruction_Invoke inst = (DexInstruction_Invoke)instruction;
	            destinationRegister = inst.getArgumentRegisters().get(0);

	            if (registerNumber == destinationRegister) {
	                return true;
	            }
	            RegisterType preInstructionDestRegisterType = getPreRegister(registerNumber);
	            if (preInstructionDestRegisterType.category != RegisterType.Category.UninitRef &&
	                preInstructionDestRegisterType.category != RegisterType.Category.UninitThis) {

	                return false;
	            }
	            //check if the uninit ref has been copied to another register
	            if (getPreRegister(registerNumber) == preInstructionDestRegisterType) {
	                return true;
	            }
	            return false;
	        }

	        if (!setsRegister()) {
	            return false;
	        }
	        DexRegister destinationRegister = getDestinationRegister();

	        if (registerNumber == destinationRegister) {
	            return true;
	        }
	        if (setsWideRegister() && registerNumber.getOriginalIndex() == (destinationRegister.getOriginalIndex() + 1)) {
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
	        	if (r1.getOriginalIndex() < r.getOriginalIndex())
	        		r = r1;
	        }
	        assert !iter.hasNext();
	        return null;
	    }

	    public int getRegisterCount() {
	        return postRegisterMap.size();
	    }

	    public RegisterType getPostRegisterType(DexRegister registerNumber) {
	    	return postRegisterMap.get(registerNumber);
	    }
	    
	    public RegisterType getPreRegisterType(DexRegister registerNumber) {
	    	return preRegisterMap.get(registerNumber);
	    }
	    
	    public DexRegisterType getPostInstructionRegisterType(DexRegister registerNumber) {
	        return DexRegisterTypeHelper.fromRegisterType(postRegisterMap.get(registerNumber), cache);
	    }

	    public DexRegisterType getPreInstructionRegisterType(DexRegister registerNumber) {
	        return DexRegisterTypeHelper.fromRegisterType(preRegisterMap.get(registerNumber), cache);
	    }

	    public int getInstructionIndex() {
	    	return instructionIndex;
	    }
}
