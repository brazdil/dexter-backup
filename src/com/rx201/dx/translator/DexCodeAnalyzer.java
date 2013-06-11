package com.rx201.dx.translator;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

import org.jf.dexlib.ClassDataItem;
import org.jf.dexlib.CodeItem;
import org.jf.dexlib.FieldIdItem;
import org.jf.dexlib.Item;
import org.jf.dexlib.ItemType;
import org.jf.dexlib.MethodIdItem;
import org.jf.dexlib.TypeIdItem;
import org.jf.dexlib.TypeListItem;
import org.jf.dexlib.Code.FiveRegisterInstruction;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.InstructionWithReference;
import org.jf.dexlib.Code.LiteralInstruction;
import org.jf.dexlib.Code.MultiOffsetInstruction;
import org.jf.dexlib.Code.OffsetInstruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.RegisterRangeInstruction;
import org.jf.dexlib.Code.SingleRegisterInstruction;
import org.jf.dexlib.Code.ThreeRegisterInstruction;
import org.jf.dexlib.Code.TwoRegisterInstruction;
import org.jf.dexlib.Code.Analysis.AnalyzedInstruction;
import org.jf.dexlib.Code.Analysis.ClassPath;
import org.jf.dexlib.Code.Analysis.DeodexUtil;
import org.jf.dexlib.Code.Analysis.InlineMethodResolver;
import org.jf.dexlib.Code.Analysis.OdexedFieldInstructionMapper;
import org.jf.dexlib.Code.Analysis.RegisterType;
import org.jf.dexlib.Code.Analysis.RegisterType.Category;
import org.jf.dexlib.Code.Analysis.ValidationException;
import org.jf.dexlib.Code.Format.ArrayDataPseudoInstruction;
import org.jf.dexlib.Code.Format.Format;
import org.jf.dexlib.Code.Format.Instruction10x;
import org.jf.dexlib.Code.Format.Instruction21c;
import org.jf.dexlib.Code.Format.Instruction22c;
import org.jf.dexlib.Code.Format.Instruction22cs;
import org.jf.dexlib.Code.Format.Instruction35c;
import org.jf.dexlib.Code.Format.Instruction35mi;
import org.jf.dexlib.Code.Format.Instruction35ms;
import org.jf.dexlib.Code.Format.Instruction3rc;
import org.jf.dexlib.Code.Format.Instruction3rmi;
import org.jf.dexlib.Code.Format.Instruction3rms;
import org.jf.dexlib.Code.Format.Instruction41c;
import org.jf.dexlib.Code.Format.Instruction52c;
import org.jf.dexlib.Code.Format.Instruction5rc;
import org.jf.dexlib.Code.Format.UnresolvedOdexInstruction;
import org.jf.dexlib.Util.AccessFlags;
import org.jf.dexlib.Util.ExceptionWithContext;
import org.jf.dexlib.Util.SparseArray;

import com.rx201.dx.translator.util.DexRegisterHelper;

import uk.ac.cam.db538.dexter.analysis.cfg.CfgBasicBlock;
import uk.ac.cam.db538.dexter.analysis.cfg.CfgBlock;
import uk.ac.cam.db538.dexter.analysis.cfg.ControlFlowGraph;
import uk.ac.cam.db538.dexter.dex.DexParsingCache;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCatch;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCatchAll;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeStart;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstructionVisitor;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ArrayGet;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ArrayGetWide;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ArrayLength;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ArrayPut;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ArrayPutWide;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_BinaryOp;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_BinaryOpLiteral;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_BinaryOpWide;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_CheckCast;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_CompareFloat;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_CompareWide;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Const;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ConstClass;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ConstString;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ConstWide;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Convert;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ConvertFromWide;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ConvertToWide;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ConvertWide;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_FillArray;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_FillArrayData;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_FilledNewArray;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Goto;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_IfTest;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_IfTestZero;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_InstanceGet;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_InstanceGetWide;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_InstanceOf;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_InstancePut;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_InstancePutWide;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Invoke;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Monitor;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Move;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_MoveException;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_MoveResult;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_MoveResultWide;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_MoveWide;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_NewArray;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_NewInstance;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Nop;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_PackedSwitchData;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Return;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ReturnVoid;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ReturnWide;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_SparseSwitchData;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_StaticGet;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_StaticGetWide;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_StaticPut;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_StaticPutWide;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Switch;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Throw;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_UnaryOp;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_UnaryOpWide;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Unknown;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_ConvertWide;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_GetPut;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_Invoke;
import uk.ac.cam.db538.dexter.dex.method.DexMethodWithCode;
import uk.ac.cam.db538.dexter.dex.method.DexPrototype;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
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


    //This is a dummy instruction that occurs immediately before the first real instruction. We can initialize the
    //register types for this instruction to the parameter types, in order to have them propagate to all of its
    //successors, e.g. the first real instruction, the first instructions in any exception handlers covering the first
    //instruction, etc.
    private AnalyzedDexInstruction startOfMethod;

    public DexCodeAnalyzer(DexMethodWithCode method) {
    	this.method = method;
        this.code = method.getCode();

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
    	boolean isConstructor = code.getParentMethod().isConstructor();
    	List<DexRegister> parameterMapping = code.getParentMethod().getParameterMappedRegisters();
    	
    	for(int i=0; i<prototype.getParameterCount(isStatic); i++) {
    		DexRegisterType dexRegType = prototype.getParameterType(i, isStatic, code.getParentClass());
    		RegisterType regType = DexRegisterTypeHelper.toRegisterType(dexRegType);
			int paramRegIndex = prototype.getFirstParameterRegisterIndex(i, isStatic);
			DexRegister paramReg = parameterMapping.get(paramRegIndex);
			
			if (!isStatic && isConstructor && i == 0) // Instance constructor has an uninit this ptr
				regType = RegisterType.getRegisterType(RegisterType.Category.UninitThis, regType.type);
			
			switch (dexRegType.getTypeSize()) {
	        case SINGLE:
	        	setPostRegisterTypeAndPropagateChanges(startOfMethod, paramReg, regType);
	        	break;
	        case WIDE:
	        	RegisterType regTypeHi = null;
	        	
	        	if (regType.category == RegisterType.Category.DoubleLo)
	        		regTypeHi = RegisterType.getRegisterType(RegisterType.Category.DoubleHi, null);
	        	else if (regType.category == RegisterType.Category.LongLo)
	        		regTypeHi = RegisterType.getRegisterType(RegisterType.Category.LongHi, null);
	        	else
	        		throw new ValidationException("Bad register type.");
	        	
	        	setPostRegisterTypeAndPropagateChanges(startOfMethod, paramReg, regType);
	        	setPostRegisterTypeAndPropagateChanges(startOfMethod, DexRegisterHelper.next(paramReg), regTypeHi);
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
        
        analyzerState = ANALYZED;
    }

    public AnalyzedDexInstruction getStartOfMethod() {
        return startOfMethod;
    }


    protected void setPostRegisterTypeAndPropagateChanges(AnalyzedDexInstruction analyzedInstruction, DexRegister registerNumber,
                                                RegisterType registerType) {

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

        if (registerType.category == RegisterType.Category.LongLo) {
//            checkWidePair(registerNumber, analyzedInstruction);
            setPostRegisterTypeAndPropagateChanges(analyzedInstruction, DexRegisterHelper.next(registerNumber),
                    RegisterType.getRegisterType(RegisterType.Category.LongHi, null));
        } else if (registerType.category == RegisterType.Category.DoubleLo) {
//            checkWidePair(registerNumber, analyzedInstruction);
            setPostRegisterTypeAndPropagateChanges(analyzedInstruction, DexRegisterHelper.next(registerNumber),
                    RegisterType.getRegisterType(RegisterType.Category.DoubleHi, null));
        }
    }

    private void propagateRegisterToSuccessors(AnalyzedDexInstruction instruction, DexRegister registerNumber,
                                               BitSet changedInstructions) {
        RegisterType postRegisterType = instruction.getPostRegisterType(registerNumber);
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
}
