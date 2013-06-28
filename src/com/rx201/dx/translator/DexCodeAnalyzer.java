package com.rx201.dx.translator;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;


import com.rx201.dx.translator.RopType.Category;
import com.rx201.dx.translator.util.DexRegisterHelper;

import uk.ac.cam.db538.dexter.analysis.cfg.CfgBasicBlock;
import uk.ac.cam.db538.dexter.analysis.cfg.CfgBlock;
import uk.ac.cam.db538.dexter.analysis.cfg.ControlFlowGraph;
import uk.ac.cam.db538.dexter.dex.Dex;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction;

import uk.ac.cam.db538.dexter.dex.method.DexMethodWithCode;
import uk.ac.cam.db538.dexter.dex.method.DexPrototype;
import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;


public class DexCodeAnalyzer {
	private DexMethodWithCode method;
	private DexCode code;

    private HashMap<DexCodeElement, AnalyzedDexInstruction> instructionMap;
    private ArrayList<AnalyzedDexInstruction> instructions;

    private static final int NOT_ANALYZED = 0;
    private static final int ANALYZED = 1;
    private static final int VERIFIED = 2;
    
    private int analyzerState = NOT_ANALYZED;

    private BitSet analyzedInstructions;

    private int maxInstructionIndex;
    
    //This is a dummy instruction that occurs immediately before the first real instruction. We can initialize the
    //register types for this instruction to the parameter types, in order to have them propagate to all of its
    //successors, e.g. the first real instruction, the first instructions in any exception handlers covering the first
    //instruction, etc.
    private AnalyzedDexInstruction startOfMethod;

    public DexCodeAnalyzer(DexMethodWithCode method) {
    	this.method = method;
        this.code = method.getCode();
        maxInstructionIndex = 0;
        buildInstructionList();

        analyzedInstructions = new BitSet(instructions.size());
    }

    public boolean isAnalyzed() {
        return analyzerState >= ANALYZED;
    }

    public boolean isVerified() {
        return analyzerState == VERIFIED;
    }

    private void analyzeParameters() {
    	boolean isStatic = code.getParentMethod().isStatic();
    	DexPrototype prototype = code.getParentMethod().getPrototype();
    	List<DexRegister> parameterMapping = code.getParentMethod().getParameterMappedRegisters();
    	
    	for(int i=0; i<prototype.getParameterCount(isStatic); i++) {
    		DexRegisterType dexRegType = prototype.getParameterType(i, isStatic, code.getParentClass());
    		RopType regType = RopType.getRopType(dexRegType.getDescriptor());
			int paramRegIndex = prototype.getFirstParameterRegisterIndex(i, isStatic);
			DexRegister paramReg = parameterMapping.get(paramRegIndex);
			
			switch (dexRegType.getTypeSize()) {
	        case SINGLE:
	        	setPostRegisterTypeAndPropagateChanges(startOfMethod, paramReg, regType);
	        	break;
	        case WIDE:
	        	setPostRegisterTypeAndPropagateChanges(startOfMethod, paramReg, regType);
	        	setPostRegisterTypeAndPropagateChanges(startOfMethod, DexRegisterHelper.next(paramReg), regType.lowToHigh());
	        	break;
			}
	        	
    	}


//        RegisterType uninit = RegisterType.getRegisterType(RegisterType.Category.Uninit, null);
//        for (int i=0; i<nonParameterRegisters; i++) {
//            setPostRegisterTypeAndPropagateChanges(startOfMethod, i, uninit);
//        }
    	
    }
    // Perform type propagation.
    public void analyze() {
        assert code.getParentMethod() != null;

        if (analyzerState >= ANALYZED) {
            //the instructions have already been analyzed, so there is nothing to do
            return;
        }

        analyzeParameters();
        
        // Do type propagation.
        /*
        for(PendingRegType pending : pendingRegisterTypes) {
        	setPostRegisterTypeAndPropagateChanges(pending.instruction, pending.regNum, pending.regType);
        }
        */
        
        BitSet instructionsToAnalyze = new BitSet(instructions.size());

        //make sure all of the "first instructions" are marked for processing
        for (AnalyzedDexInstruction successor: startOfMethod.successors) {
            instructionsToAnalyze.set(successor.getInstructionIndex());
        }

        do {
            boolean didSomething = false;

            while (!instructionsToAnalyze.isEmpty()) {
                for(int i=instructionsToAnalyze.nextSetBit(0); i>=0; i=instructionsToAnalyze.nextSetBit(i+1)) {
                    instructionsToAnalyze.clear(i);
                    if (analyzedInstructions.get(i)) {
                        continue;
                    }
                    AnalyzedDexInstruction instructionToAnalyze = instructions.get(i);

                    analyzeInstruction(instructionToAnalyze);
                    didSomething = true;

                    analyzedInstructions.set(instructionToAnalyze.getInstructionIndex());

                    for (AnalyzedDexInstruction successor: instructionToAnalyze.successors) {
                        instructionsToAnalyze.set(successor.getInstructionIndex());
                    }
                }
            }

            if (!didSomething) {
                break;
            }

        } while (true);

        
        preciseTypeAnalysis();
        
        analyzerState = ANALYZED;
    }

	public AnalyzedDexInstruction getStartOfMethod() {
        return startOfMethod;
    }


    protected void setPostRegisterTypeAndPropagateChanges(AnalyzedDexInstruction analyzedInstruction, DexRegister registerNumber,
    		RopType registerType) {

        BitSet changedInstructions = new BitSet(instructions.size());

        if (!analyzedInstruction.setPostRegisterType(registerNumber, registerType)) {
            return;
        }

        propagateRegisterToSuccessors(analyzedInstruction, registerNumber, changedInstructions);

        //Using a for loop inside the while loop optimizes for the common case of the successors of an instruction
        //occurring after the instruction. Any successors that occur prior to the instruction will be picked up on
        //the next iteration of the while loop.
        //This could also be done recursively, but in large methods it would likely cause very deep recursion,
        //which requires the user to specify a larger stack size. This isn't really a problem, but it is slightly
        //annoying.
        while (!changedInstructions.isEmpty()) {
            for (int instructionIndex=changedInstructions.nextSetBit(0);
                     instructionIndex>=0;
                     instructionIndex=changedInstructions.nextSetBit(instructionIndex+1)) {

                changedInstructions.clear(instructionIndex);

                propagateRegisterToSuccessors(instructions.get(instructionIndex), registerNumber,
                        changedInstructions);
            }
        }

        if (registerType.category == Category.LongLo) {
//            checkWidePair(registerNumber, analyzedInstruction);
            setPostRegisterTypeAndPropagateChanges(analyzedInstruction, DexRegisterHelper.next(registerNumber),
            		RopType.LongHi);
        } else if (registerType.category == Category.DoubleLo) {
//            checkWidePair(registerNumber, analyzedInstruction);
            setPostRegisterTypeAndPropagateChanges(analyzedInstruction, DexRegisterHelper.next(registerNumber),
            		RopType.DoubleHi);
        } else if (registerType.category == Category.Wide) {
            setPostRegisterTypeAndPropagateChanges(analyzedInstruction, DexRegisterHelper.next(registerNumber),
            		RopType.Wide);
        }
    }

    private void propagateRegisterToSuccessors(AnalyzedDexInstruction instruction, DexRegister registerNumber,
                                               BitSet changedInstructions) {
    	RopType postRegisterType = instruction.getPostRegisterType(registerNumber);
        for (AnalyzedDexInstruction successor: instruction.successors) {
            if (successor.mergeRegister(registerNumber, postRegisterType, analyzedInstructions)) {
                changedInstructions.set(successor.getInstructionIndex());
            }
        }
    }

    
    private void buildInstructionList() {
    	instructions = new ArrayList<AnalyzedDexInstruction>();
    	instructionMap = new HashMap<DexCodeElement, AnalyzedDexInstruction>();
    	for(DexCodeElement inst : code.getInstructionList()) {
    		AnalyzedDexInstruction analyzedInst = buildFromDexCodeElement(instructions.size(), inst);
    		instructionMap.put(inst, analyzedInst);
    		instructions.add(analyzedInst);
    		
    	}

    	ControlFlowGraph cfg = new ControlFlowGraph(code);
    	
    	for(CfgBasicBlock bb : cfg.getBasicBlocks()) {
        	AnalyzedDexInstruction prevA = null;
        	// Connect predecessor/successor within a basic block
    		for(DexCodeElement cur: bb.getInstructions()) {
    			AnalyzedDexInstruction curA = instructionMap.get(cur);
    			if (prevA != null) {
    				prevA.addSuccessor(curA);
        			curA.addPredecessor(prevA);
    			}
    			prevA = curA;
    		}
    		// Connect with successor basic block
    		for(CfgBlock nextBB : bb.getSuccessors()) {
    			if (nextBB instanceof CfgBasicBlock) {
    				DexCodeElement cur = ((CfgBasicBlock)nextBB).getFirstInstruction();
        			AnalyzedDexInstruction curA = instructionMap.get(cur);
    				prevA.addSuccessor(curA);
        			curA.addPredecessor(prevA);
    			}
    		}
    	}
    	
        //override AnalyzedInstruction and provide custom implementations of some of the methods, so that we don't
        //have to handle the case this special case of instruction being null, in the main class
        startOfMethod = new AnalyzedDexInstruction(-1, null, method.getParentFile()) {
            public boolean setsRegister() {
                return false;
            }

            @Override
            public boolean setsWideRegister() {
                return false;
            }

            @Override
            public boolean setsRegister(DexRegister registerNumber) {
                return false;
            }

            @Override
            public DexRegister getDestinationRegister() {
                assert false;
                return null;
            };
        };
        for (CfgBlock startBB: cfg.getStartBlock().getSuccessors()) {
        	if (startBB instanceof CfgBasicBlock) {
        		startOfMethod.addSuccessor(instructionMap.get(
        				((CfgBasicBlock)startBB).getFirstInstruction()));
        	}
        }
    }

    private AnalyzedDexInstruction buildFromDexCodeElement(int index, DexCodeElement element) {
    	if (index > maxInstructionIndex)
    		maxInstructionIndex = index;
    	if (element instanceof DexInstruction) {
    		return new AnalyzedDexInstruction(index, (DexInstruction) element, method.getParentFile());
    	} else /* DexCatch, DexCatchAll, DexLabel, DexTryBlockStart, DexTryBlockEnd */ {
    		return new AnalyzedDexInstruction(index, null, element, method.getParentFile());
    	}
	}

    private void analyzeInstruction(AnalyzedDexInstruction instruction) {
    	DexInstruction inst = instruction.instruction;
    	
    	if (inst != null) {
	    	DexInstructionAnalyzer analyzer = new DexInstructionAnalyzer(this);
	    	analyzer.setAnalyzedInstruction(instruction);
	    	inst.accept(analyzer);
    	}
    }

    public AnalyzedDexInstruction reverseLookup(DexCodeElement element) {
    	assert element != null;
    	return instructionMap.get(element);
    }
    
    public int getMaxInstructionIndex() {
    	return maxInstructionIndex;
    }
    

    private void preciseTypeAnalysis() {
    	for(AnalyzedDexInstruction inst : instructions) {
    		for (DexRegister reg : inst.getDefinedRegisters()) {
    			if (!inst.getPostRegisterType(reg).isPolymorphic())
    				continue;
    			RopType type = getPrecisePostRegisterType(reg, inst, null);
    			assert type.category != Category.Conflicted;
    			setPostRegisterTypeAndPropagateChanges(inst, reg, type);
    		}
    	}
	}
    
	private RopType getPrecisePostRegisterType(DexRegister reg, AnalyzedDexInstruction instruction, RopType priorTypeInfo) {
		// bfs for register type propagation
		HashMap<AnalyzedDexInstruction, HashSet<Integer>> visited = new HashMap<AnalyzedDexInstruction, HashSet<Integer>>(); 
		// These two linkedList are always paired together, maybe a better way is to have a separate class for them?
		LinkedList<AnalyzedDexInstruction> queue = new LinkedList<AnalyzedDexInstruction>();
		LinkedList<Integer> targetReg_queue = new LinkedList<Integer>();
		
		Dex parentFile = instruction.instruction.getParentFile();
		
		int regNum = DexRegisterHelper.normalize(reg);
		for(AnalyzedDexInstruction successor : instruction.getSuccesors()) {
			queue.add(successor);
			targetReg_queue.add(regNum);
		}
		
		RopType type;
		if (priorTypeInfo != null)
		    type = priorTypeInfo;
		else
		    type = RopType.Unknown;
		
		while(queue.size() > 0) {
			AnalyzedDexInstruction head = queue.remove();
			int head_reg = targetReg_queue.remove();
			
			if (!visited.containsKey(head))
				visited.put(head, new HashSet<Integer>());
			if (visited.get(head).contains(head_reg) ) continue;
			visited.get(head).add(head_reg);
			
			boolean deadEnd = false;
            RopType typeInfo = null;
			if (head.instruction != null) {

	            typeInfo = head.getUsedRegType(head_reg);

				// Do not search further is this instruction overwrites the target register.
				if (head.getDefinedRegType(head_reg) != null)
					deadEnd = true;
			}
			//A different control flow may provide additional type information.
            if (typeInfo == null && head.getPredecessorCount() > 1) {
                typeInfo = head.peekPreRegister(head_reg);
                if (typeInfo != null && typeInfo.category == Category.Conflicted)
                    typeInfo = null;
            }
            
            if (typeInfo != null) {
                //TODO: UGLY HACK WARNING
                /* This is valid, so deal with this special case here
                 *     const/4 v8, 0x0
                 *     const/16 v7, 0x20
                 *     shl-long/2addr v5, v7
                 *     
                 * a.k.a merging Integer with LongLo/Hi should be allowed    
                 */
                RopType newType = type.merge(typeInfo);
                assert newType.category != Category.Conflicted;
                type = newType;
            }
			
			if (!deadEnd) {
				for(AnalyzedDexInstruction successor : head.getSuccesors()) {
					queue.add(successor);
					targetReg_queue.add(head_reg);
					if (head.getMovedReg(head_reg) != -1) {
						queue.add(successor);
						targetReg_queue.add(head.getMovedReg(head_reg));
					}
				}
			}
		}
		
		return type;
	} 
	    
}
