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
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_GetPut;
import uk.ac.cam.db538.dexter.dex.method.DexPrototype;
import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;

interface InstructionAnalyzer extends DexInstructionVisitor {
	public void setAnalyzedInstruction(AnalyzedDexInstruction i); 
}

public class DexCodeAnalyzer {
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

	private DexParsingCache cache;

    public DexCodeAnalyzer(DexCode code, DexParsingCache cache) {
        this.code = code;
        this.cache = cache;


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
    	boolean isConstructor = code.getParentMethod().getName().equals("<init>");
    	for(int i=0; i<prototype.getParameterCount(isStatic); i++) {
    		DexRegisterType dexRegType = prototype.getParameterType(i, isStatic, code.getParentClass());
    		RegisterType regType = DexRegisterTypeHelper.toRegisterType(dexRegType);
			int paramId = prototype.getFirstParameterRegisterIndex(i, isStatic);
			
			if (!isStatic && isConstructor && i == 0) // Instance constructor has an uninit this ptr
				regType = RegisterType.getRegisterType(RegisterType.Category.UninitThis, regType.type);
			
			switch (dexRegType.getTypeSize()) {
	        case SINGLE:
	        	setPostRegisterTypeAndPropagateChanges(startOfMethod, DexRegisterHelper.normalize(paramId), regType);
	        	break;
	        case WIDE:
	        	RegisterType regTypeHi = null;
	        	
	        	if (regType.category == RegisterType.Category.DoubleLo)
	        		regTypeHi = RegisterType.getRegisterType(RegisterType.Category.DoubleHi, null);
	        	else if (regType.category == RegisterType.Category.LongLo)
	        		regTypeHi = RegisterType.getRegisterType(RegisterType.Category.LongHi, null);
	        	else
	        		throw new ValidationException("Bad register type.");
	        	
	        	setPostRegisterTypeAndPropagateChanges(startOfMethod, DexRegisterHelper.normalize(paramId), regType);
	        	setPostRegisterTypeAndPropagateChanges(startOfMethod, DexRegisterHelper.normalize(paramId + 1), regTypeHi);
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

        BitSet undeodexedInstructions = new BitSet(instructions.size());

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

            if (!undeodexedInstructions.isEmpty()) {
                for (int i=undeodexedInstructions.nextSetBit(0); i>=0; i=undeodexedInstructions.nextSetBit(i+1)) {
                    instructionsToAnalyze.set(i);
                }
            }
        } while (true);
        
        analyzerState = ANALYZED;
    }

    public AnalyzedDexInstruction getStartOfMethod() {
        return startOfMethod;
    }

    /**
     * @return a read-only list containing the instructions for tihs method.
     */
    public List<AnalyzedDexInstruction> getInstructions() {
        return Collections.unmodifiableList(instructions);
    }


    private static RegisterType[] getParameterTypes(TypeListItem typeListItem, int parameterRegisterCount) {
        assert typeListItem != null;
        assert parameterRegisterCount == typeListItem.getRegisterCount();

        RegisterType[] registerTypes = new RegisterType[parameterRegisterCount];

        int registerNum = 0;
        for (TypeIdItem type: typeListItem.getTypes()) {
            if (type.getRegisterCount() == 2) {
                registerTypes[registerNum++] = RegisterType.getWideRegisterTypeForTypeIdItem(type, true);
                registerTypes[registerNum++] = RegisterType.getWideRegisterTypeForTypeIdItem(type, false);
            } else {
                registerTypes[registerNum++] = RegisterType.getRegisterTypeForTypeIdItem(type);
            }
        }

        return registerTypes;
    }

    /*
    private class PendingRegType {
    	public AnalyzedDexInstruction instruction;
    	public DexRegister regNum;
    	public RegisterType regType;
    	public PendingRegType(AnalyzedDexInstruction instruction, DexRegister regNum, RegisterType regType) {
    		this.instruction = instruction;
    		this.regNum = regNum;
    		this.regType =regType;
    	}
    }

    private ArrayList<PendingRegType> pendingRegisterTypes = new ArrayList<PendingRegType>();
    // Enqueue for later processing
    private void pendingSetPostRegisterType(AnalyzedDexInstruction analyzedInstruction, DexRegister registerNum, RegisterType registerType) {
    	pendingRegisterTypes.add(new PendingRegType(analyzedInstruction, registerNum, registerType));
    }
    */
    
    private void setDestinationRegisterTypeAndPropagateChanges(AnalyzedDexInstruction analyzedInstruction,
            RegisterType registerType) {
    	setPostRegisterTypeAndPropagateChanges(analyzedInstruction, analyzedInstruction.getDestinationRegister(), registerType);
	}
	

    private void setPostRegisterTypeAndPropagateChanges(AnalyzedDexInstruction analyzedInstruction, DexRegister registerNumber,
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
    }

    private AnalyzedDexInstruction buildFromDexCodeElement(int index, DexCodeElement inst) {
    	if (inst instanceof DexCodeStart) {
            //override AnalyzedInstruction and provide custom implementations of some of the methods, so that we don't
            //have to handle the case this special case of instruction being null, in the main class
            startOfMethod = new AnalyzedDexInstruction(index, null, cache) {
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
            return startOfMethod;
    	} else if (inst instanceof DexInstruction) {
    		return new AnalyzedDexInstruction(index, (DexInstruction) inst, cache);
    	} else /* DexCatch, DexCatchAll, DexLabel, DexTryBlockStart, DexTryBlockEnd */ {
    		return new AnalyzedDexInstruction(index, new DexInstruction_Nop(code), cache);
    	}
	}

    private InstructionAnalyzer instructionAnalyzer = new InstructionAnalyzer() {
    	private AnalyzedDexInstruction instruction;
    	
    	@Override
    	public void setAnalyzedInstruction(AnalyzedDexInstruction i) {
    		this.instruction = i;
    	}
    	
		@Override
		public void visit(DexInstruction_Nop dexInstruction_Nop) {}
		
		private void analyzeMove(DexRegister srcReg) {
			RegisterType valueType = instruction.getPreRegisterType(srcReg);
			setDestinationRegisterTypeAndPropagateChanges(instruction, valueType);
		}
		@Override
		public void visit(DexInstruction_Move dexInstruction_Move) {
			analyzeMove(dexInstruction_Move.getRegFrom());
//            analyzeMove(analyzedInstruction);

		}
		@Override
		public void visit(DexInstruction_MoveWide dexInstruction_MoveWide) {
			assert DexRegisterHelper.isPair(dexInstruction_MoveWide.getRegFrom1(),  dexInstruction_MoveWide.getRegFrom2());
			assert DexRegisterHelper.isPair(dexInstruction_MoveWide.getRegTo1(),  dexInstruction_MoveWide.getRegTo2());
			analyzeMove(dexInstruction_MoveWide.getRegFrom1());
//            analyzeMove(analyzedInstruction);

		}
		
		private void analyzeMoveResult(DexRegister srcReg) {
			assert instruction.getPredecessorCount() == 1;
			
	        AnalyzedDexInstruction prevAnalyzedInst = instruction.getPredecessors().get(0);
	        
	        DexRegisterType resultRegisterType;
	        if (prevAnalyzedInst.instruction instanceof DexInstruction_Invoke) {
	        	DexInstruction_Invoke i = (DexInstruction_Invoke) prevAnalyzedInst.instruction;
	        	resultRegisterType = (DexRegisterType)i.getMethodPrototype().getReturnType();
	        } else if (prevAnalyzedInst.instruction instanceof DexInstruction_FilledNewArray) {
	        	DexInstruction_FilledNewArray i = (DexInstruction_FilledNewArray)prevAnalyzedInst.instruction;
	        	resultRegisterType = i.getArrayType();
	        } else {
	            throw new ValidationException(instruction.instruction.getOriginalAssembly() + " must occur after an " +
	                    "invoke-*/fill-new-array instruction");
	        }
	        
	        		
	        setDestinationRegisterTypeAndPropagateChanges(instruction, 
	        		DexRegisterTypeHelper.toRegisterType(resultRegisterType));			
		}
		@Override
		public void visit(DexInstruction_MoveResult dexInstruction_MoveResult) {
			analyzeMoveResult(dexInstruction_MoveResult.getRegTo());
//			analyzeMoveResult(analyzedInstruction);

		}
		@Override
		public void visit(DexInstruction_MoveResultWide dexInstruction_MoveResultWide) {
			assert DexRegisterHelper.isPair(dexInstruction_MoveResultWide.getRegTo1(),  dexInstruction_MoveResultWide.getRegTo2());
			analyzeMoveResult(dexInstruction_MoveResultWide.getRegTo1());
//            analyzeMoveResult(analyzedInstruction);
		}
		@Override
		public void visit(
				DexInstruction_MoveException dexInstruction_MoveException) {
			// TODO Auto-generated method stub
            analyzeMoveException(analyzedInstruction);

		}
		@Override
		public void visit(DexInstruction_ReturnVoid dexInstruction_ReturnVoid) {}
		
		@Override
		public void visit(DexInstruction_Return dexInstruction_Return) {}
		
		@Override
		public void visit(DexInstruction_ReturnWide dexInstruction_ReturnWide) {}
		
		@Override
		public void visit(DexInstruction_Const dexInstruction_Const) {
			// TODO Auto-generated method stub
            analyzeConst(analyzedInstruction);

		}
		@Override
		public void visit(DexInstruction_ConstWide dexInstruction_ConstWide) {
			// TODO Auto-generated method stub
            analyzeConst(analyzedInstruction);

		}
		@Override
		public void visit(DexInstruction_ConstString dexInstruction_ConstString) {
			// TODO Auto-generated method stub
            analyzeConstString(analyzedInstruction);

		}
		@Override
		public void visit(DexInstruction_ConstClass dexInstruction_ConstClass) {
			// TODO Auto-generated method stub
            analyzeConstClass(analyzedInstruction);

		}
		@Override
		public void visit(DexInstruction_Monitor dexInstruction_Monitor) {}
		
		@Override
		public void visit(DexInstruction_CheckCast dexInstruction_CheckCast) {
			// TODO Auto-generated method stub
            analyzeCheckCast(analyzedInstruction);

		}
		@Override
		public void visit(DexInstruction_InstanceOf dexInstruction_InstanceOf) {
			// TODO Auto-generated method stub
            analyzeInstanceOf(analyzedInstruction);

		}
		@Override
		public void visit(DexInstruction_ArrayLength dexInstruction_ArrayLength) {
			// TODO Auto-generated method stub
            analyzeArrayLength(analyzedInstruction);

		}
		@Override
		public void visit(DexInstruction_NewInstance dexInstruction_NewInstance) {
			// TODO Auto-generated method stub
            analyzeNewInstance(analyzedInstruction);

		}
		@Override
		public void visit(DexInstruction_NewArray dexInstruction_NewArray) {
			// TODO Auto-generated method stub
            analyzeNewArray(analyzedInstruction);

		}
		@Override
		public void visit(DexInstruction_FilledNewArray dexInstruction_FilledNewArray) {}
		
		@Override
		public void visit(DexInstruction_FillArray dexInstruction_FillArray) {
			// TODO Auto-generated method stub
            analyzeArrayDataOrSwitch(analyzedInstruction);
			
		}
		
		@Override
		public void visit(DexInstruction_FillArrayData dexInstruction_FillArrayData) {}
		
		@Override
		public void visit(DexInstruction_Throw dexInstruction_Throw) {}
		
		@Override
		public void visit(DexInstruction_Goto dexInstruction_Goto) {}
		
		@Override
		public void visit(DexInstruction_Switch dexInstruction_Switch) {
			// TODO Auto-generated method stub
            analyzeArrayDataOrSwitch(analyzedInstruction);

		}
		@Override
		public void visit(DexInstruction_PackedSwitchData dexInstruction_PackedSwitchData) {}
		
		@Override
		public void visit(DexInstruction_SparseSwitchData dexInstruction_SparseSwitchData) {}
		
		@Override
		public void visit(
				DexInstruction_CompareFloat dexInstruction_CompareFloat) {
			// TODO Auto-generated method stub
            analyzeFloatWideCmp(analyzedInstruction);

		}
		@Override
		public void visit(DexInstruction_CompareWide dexInstruction_CompareWide) {
			// TODO Auto-generated method stub
            analyzeFloatWideCmp(analyzedInstruction);

		}
		@Override
		public void visit(DexInstruction_IfTest dexInstruction_IfTest) {}
		
		@Override
		public void visit(DexInstruction_IfTestZero dexInstruction_IfTestZero) {}
		
		@Override
		public void visit(DexInstruction_ArrayGet inst) {
	    	if (inst.getOpcode() == Opcode_GetPut.Object) {

	            RegisterType arrayRegisterType = instruction.getPreRegisterType(inst.getRegArray());
	            assert arrayRegisterType != null;

	            if (arrayRegisterType.category != RegisterType.Category.Null) {
	                assert arrayRegisterType.type != null;
	                if (arrayRegisterType.type.getClassType().charAt(0) != '[') {
	                    throw new ValidationException(String.format("Cannot use aget-object with non-array type %s",
	                            arrayRegisterType.type.getClassType()));
	                }

	                assert arrayRegisterType.type instanceof ClassPath.ArrayClassDef;
	                ClassPath.ArrayClassDef arrayClassDef = (ClassPath.ArrayClassDef)arrayRegisterType.type;

	                ClassPath.ClassDef elementClassDef = arrayClassDef.getImmediateElementClass();
	                char elementTypePrefix = elementClassDef.getClassType().charAt(0);
	                if (elementTypePrefix != 'L' && elementTypePrefix != '[') {
	                    throw new ValidationException(String.format("Cannot use aget-object with array type %s. Incorrect " +
	                            "array type for the instruction.", arrayRegisterType.type.getClassType()));
	                }

	                setDestinationRegisterTypeAndPropagateChanges(instruction,
	                        RegisterType.getRegisterType(RegisterType.Category.Reference, elementClassDef));
	            } else {
	                setDestinationRegisterTypeAndPropagateChanges(instruction,
	                        RegisterType.getRegisterType(RegisterType.Category.Null, null));
	            }
	    		
	    	} else {
	    		
		    	RegisterType.Category category;
		    	switch (inst.getOpcode()) {
				case Boolean:
					category = RegisterType.Category.Boolean;
					break;
				case Byte:
					category = RegisterType.Category.Byte;
					break;
				case Char:
					category = RegisterType.Category.Char;
					break;
				case IntFloat:
					category = RegisterType.Category.Integer; // Ambiguity here does not matter, type merging will deal with it.
					break;
				case Short:
					category = RegisterType.Category.Short;
					break;
				default:
					throw new ValidationException("wrong type AGET");
		    	}
		    	setDestinationRegisterTypeAndPropagateChanges(instruction, RegisterType.getRegisterType(category, null));
	    	}
		}		
		@Override
		public void visit(
				DexInstruction_ArrayGetWide dexInstruction_ArrayGetWide) {
			// TODO Auto-generated method stub
            analyzeAgetWide(analyzedInstruction);

		}
		@Override
		public void visit(DexInstruction_ArrayPut dexInstruction_ArrayPut) {}
		
		@Override
		public void visit(DexInstruction_ArrayPutWide dexInstruction_ArrayPutWide) {}
		
		@Override
		public void visit(DexInstruction_InstanceGet dexInstruction_InstanceGet) {
			// TODO Auto-generated method stub
            analyze32BitPrimitiveIget(analyzedInstruction, RegisterType.Category.Integer);
            analyzeIgetWideObject(analyzedInstruction);

		}
		@Override
		public void visit(
				DexInstruction_InstanceGetWide dexInstruction_InstanceGetWide) {
			// TODO Auto-generated method stub
            analyze32BitPrimitiveIget(analyzedInstruction, RegisterType.Category.Integer);
            analyzeIgetWideObject(analyzedInstruction);
			
		}
		@Override
		public void visit(DexInstruction_InstancePut dexInstruction_InstancePut) {}
		
		@Override
		public void visit(DexInstruction_InstancePutWide dexInstruction_InstancePutWide) {}
		
		@Override
		public void visit(DexInstruction_StaticGet dexInstruction_StaticGet) {
			// TODO Auto-generated method stub
            analyze32BitPrimitiveSget(analyzedInstruction, RegisterType.Category.Integer);
            analyzeSgetWideObject(analyzedInstruction);

		}
		@Override
		public void visit(
				DexInstruction_StaticGetWide dexInstruction_StaticGetWide) {
			// TODO Auto-generated method stub
            analyze32BitPrimitiveSget(analyzedInstruction, RegisterType.Category.Integer);
            analyzeSgetWideObject(analyzedInstruction);

		}
		@Override
		public void visit(DexInstruction_StaticPut dexInstruction_StaticPut) {}
		
		@Override
		public void visit(DexInstruction_StaticPutWide dexInstruction_StaticPutWide) {}
		
		@Override
		public void visit(DexInstruction_Invoke dexInstruction_Invoke) {
			// TODO Auto-generated method stub
			//INVOKE_DIRECT:
            analyzeInvokeDirect(analyzedInstruction);

		}
		@Override
		public void visit(DexInstruction_UnaryOp dexInstruction_UnaryOp) {
			// TODO Auto-generated method stub
            analyzeUnaryOp(analyzedInstruction, RegisterType.Category.Integer);
            analyzeUnaryOp(analyzedInstruction, RegisterType.Category.LongLo);

		}
		@Override
		public void visit(DexInstruction_UnaryOpWide dexInstruction_UnaryOpWide) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void visit(DexInstruction_Convert dexInstruction_Convert) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void visit(DexInstruction_ConvertWide dexInstruction_ConvertWide) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void visit(
				DexInstruction_ConvertFromWide dexInstruction_ConvertFromWide) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void visit(
				DexInstruction_ConvertToWide dexInstruction_ConvertToWide) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void visit(DexInstruction_BinaryOp dexInstruction_BinaryOp) {
			// TODO Auto-generated method stub
            analyzeBinaryOp(analyzedInstruction, RegisterType.Category.Integer, false);
            analyzeBinaryOp(analyzedInstruction, RegisterType.Category.Integer, true);

		}
		@Override
		public void visit(
				DexInstruction_BinaryOpLiteral dexInstruction_BinaryOpLiteral) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void visit(
				DexInstruction_BinaryOpWide dexInstruction_BinaryOpWide) {
			// TODO Auto-generated method stub
            analyzeBinaryOp(analyzedInstruction, RegisterType.Category.LongLo, false);

		}
		@Override
		public void visit(DexInstruction_Unknown dexInstruction_Unknown) {
			assert false;
		}
		
    };
    
    private void analyzeInstruction(AnalyzedDexInstruction instruction) {
    	DexInstruction inst = instruction.instruction;
    	
    	instructionAnalyzer.setAnalyzedInstruction(instruction);
    	inst.accept(instructionAnalyzer);
    }



    private static final EnumSet<RegisterType.Category> Primitive32BitCategories = EnumSet.of(
            RegisterType.Category.Null,
            RegisterType.Category.One,
            RegisterType.Category.Boolean,
            RegisterType.Category.Byte,
            RegisterType.Category.PosByte,
            RegisterType.Category.Short,
            RegisterType.Category.PosShort,
            RegisterType.Category.Char,
            RegisterType.Category.Integer,
            RegisterType.Category.Float);

    private static final EnumSet<RegisterType.Category> WideLowCategories = EnumSet.of(
            RegisterType.Category.LongLo,
            RegisterType.Category.DoubleLo);

    private static final EnumSet<RegisterType.Category> WideHighCategories = EnumSet.of(
            RegisterType.Category.LongHi,
            RegisterType.Category.DoubleHi);

    private static final EnumSet<RegisterType.Category> ReferenceCategories = EnumSet.of(
            RegisterType.Category.Null,
            RegisterType.Category.Reference);

    private static final EnumSet<RegisterType.Category> ReferenceOrUninitThisCategories = EnumSet.of(
            RegisterType.Category.Null,
            RegisterType.Category.UninitThis,
            RegisterType.Category.Reference);

    private static final EnumSet<RegisterType.Category> ReferenceOrUninitCategories = EnumSet.of(
            RegisterType.Category.Null,
            RegisterType.Category.UninitRef,
            RegisterType.Category.UninitThis,
            RegisterType.Category.Reference);

    private static final EnumSet<RegisterType.Category> ReferenceAndPrimitive32BitCategories = EnumSet.of(
            RegisterType.Category.Null,
            RegisterType.Category.One,
            RegisterType.Category.Boolean,
            RegisterType.Category.Byte,
            RegisterType.Category.PosByte,
            RegisterType.Category.Short,
            RegisterType.Category.PosShort,
            RegisterType.Category.Char,
            RegisterType.Category.Integer,
            RegisterType.Category.Float,
            RegisterType.Category.Reference);

    private static final EnumSet<RegisterType.Category> BooleanCategories = EnumSet.of(
            RegisterType.Category.Null,
            RegisterType.Category.One,
            RegisterType.Category.Boolean);

    private void analyzeMove(AnalyzedInstruction analyzedInstruction) {
        TwoRegisterInstruction instruction = (TwoRegisterInstruction)analyzedInstruction.instruction;

        RegisterType sourceRegisterType = analyzedInstruction.getPreInstructionRegisterType(instruction.getRegisterB());
        setDestinationRegisterTypeAndPropagateChanges(analyzedInstruction, sourceRegisterType);
    }

    private void analyzeMoveResult(AnalyzedInstruction analyzedInstruction) {
        AnalyzedInstruction previousInstruction = instructions.valueAt(analyzedInstruction.instructionIndex-1);
        if (!previousInstruction.instruction.opcode.setsResult()) {
            throw new ValidationException(analyzedInstruction.instruction.opcode.name + " must occur after an " +
                    "invoke-*/fill-new-array instruction");
        }

        RegisterType resultRegisterType;
        InstructionWithReference invokeInstruction = (InstructionWithReference)previousInstruction.instruction;
        Item item = invokeInstruction.getReferencedItem();

        if (item.getItemType() == ItemType.TYPE_METHOD_ID_ITEM) {
            resultRegisterType = RegisterType.getRegisterTypeForTypeIdItem(
                    ((MethodIdItem)item).getPrototype().getReturnType());
        } else {
            assert item.getItemType() == ItemType.TYPE_TYPE_ID_ITEM;
            resultRegisterType = RegisterType.getRegisterTypeForTypeIdItem((TypeIdItem)item);
        }

        setDestinationRegisterTypeAndPropagateChanges(analyzedInstruction, resultRegisterType);
    }

    private void analyzeMoveException(AnalyzedInstruction analyzedInstruction) {
        CodeItem.TryItem[] tries = encodedMethod.codeItem.getTries();
        int instructionAddress = getInstructionAddress(analyzedInstruction);

        if (tries == null) {
            throw new ValidationException("move-exception must be the first instruction in an exception handler block");
        }

        RegisterType exceptionType = null;

        for (CodeItem.TryItem tryItem: encodedMethod.codeItem.getTries()) {
            if (tryItem.encodedCatchHandler.getCatchAllHandlerAddress() == instructionAddress) {
                exceptionType = RegisterType.getRegisterType(RegisterType.Category.Reference,
                        ClassPath.getClassDef("Ljava/lang/Throwable;"));
                break;
            }
            for (CodeItem.EncodedTypeAddrPair handler: tryItem.encodedCatchHandler.handlers) {
                if (handler.getHandlerAddress() == instructionAddress) {
                    exceptionType = RegisterType.getRegisterTypeForTypeIdItem(handler.exceptionType)
                            .merge(exceptionType);
                }
            }
        }

        if (exceptionType == null) {
            throw new ValidationException("move-exception must be the first instruction in an exception handler block");
        }

        setDestinationRegisterTypeAndPropagateChanges(analyzedInstruction, exceptionType);
    }

    private void analyzeReturnVoidBarrier(AnalyzedInstruction analyzedInstruction) {
        analyzeReturnVoidBarrier(analyzedInstruction, true);
    }

    private void analyzeReturnVoidBarrier(AnalyzedInstruction analyzedInstruction, boolean analyzeResult) {
        Instruction10x instruction = (Instruction10x)analyzedInstruction.instruction;

        Instruction10x deodexedInstruction = new Instruction10x(Opcode.RETURN_VOID);

        analyzedInstruction.setDeodexedInstruction(deodexedInstruction);

        if (analyzeResult) {
            analyzeInstruction(analyzedInstruction);
        }
    }

    private void analyzeConst(AnalyzedInstruction analyzedInstruction) {
        LiteralInstruction instruction = (LiteralInstruction)analyzedInstruction.instruction;

        RegisterType newDestinationRegisterType = RegisterType.getRegisterTypeForLiteral(instruction.getLiteral());

        //we assume that the literal value is a valid value for the given instruction type, because it's impossible
        //to store an invalid literal with the instruction. so we don't need to check the type of the literal
        setDestinationRegisterTypeAndPropagateChanges(analyzedInstruction, newDestinationRegisterType);
    }

    private void analyzeConstHigh16(AnalyzedInstruction analyzedInstruction) {
        //the literal value stored in the instruction is a 16-bit value. When shifted left by 16, it will always be an
        //integer
        setDestinationRegisterTypeAndPropagateChanges(analyzedInstruction,
                RegisterType.getRegisterType(RegisterType.Category.Integer, null));
    }

    private void analyzeWideConst(AnalyzedInstruction analyzedInstruction) {
        setDestinationRegisterTypeAndPropagateChanges(analyzedInstruction,
                RegisterType.getRegisterType(RegisterType.Category.LongLo, null));
    }

    private void analyzeConstString(AnalyzedInstruction analyzedInstruction) {
        ClassPath.ClassDef stringClassDef = ClassPath.getClassDef("Ljava/lang/String;");
        RegisterType stringType = RegisterType.getRegisterType(RegisterType.Category.Reference, stringClassDef);
        setDestinationRegisterTypeAndPropagateChanges(analyzedInstruction, stringType);
    }

    private void analyzeConstClass(AnalyzedInstruction analyzedInstruction) {
        ClassPath.ClassDef classClassDef = ClassPath.getClassDef("Ljava/lang/Class;");
        RegisterType classType = RegisterType.getRegisterType(RegisterType.Category.Reference, classClassDef);

        setDestinationRegisterTypeAndPropagateChanges(analyzedInstruction, classType);
    }


    private void analyzeCheckCast(AnalyzedInstruction analyzedInstruction) {
        InstructionWithReference instruction = (InstructionWithReference)analyzedInstruction.instruction;

        Item item = instruction.getReferencedItem();
        assert item.getItemType() == ItemType.TYPE_TYPE_ID_ITEM;

        RegisterType castRegisterType = RegisterType.getRegisterTypeForTypeIdItem((TypeIdItem)item);
        setDestinationRegisterTypeAndPropagateChanges(analyzedInstruction, castRegisterType);
    }

    private void analyzeInstanceOf(AnalyzedInstruction analyzedInstruction) {
        setDestinationRegisterTypeAndPropagateChanges(analyzedInstruction,
                RegisterType.getRegisterType(RegisterType.Category.Boolean, null));
    }

    private void analyzeArrayLength(AnalyzedInstruction analyzedInstruction) {
        setDestinationRegisterTypeAndPropagateChanges(analyzedInstruction,
                RegisterType.getRegisterType(RegisterType.Category.Integer, null));
    }

    private void analyzeNewInstance(AnalyzedInstruction analyzedInstruction) {
        InstructionWithReference instruction = (InstructionWithReference)analyzedInstruction.instruction;

        int register = ((SingleRegisterInstruction)analyzedInstruction.instruction).getRegisterA();
        RegisterType destRegisterType = analyzedInstruction.getPostInstructionRegisterType(register);
        if (destRegisterType.category != RegisterType.Category.Unknown) {
            assert destRegisterType.category == RegisterType.Category.UninitRef;

            //the post-instruction destination register will only be set if we have already analyzed this instruction
            //at least once. If this is the case, then the uninit reference has already been propagated to all
            //successors and nothing else needs to be done.
            return;
        }

        Item item = instruction.getReferencedItem();
        assert item.getItemType() == ItemType.TYPE_TYPE_ID_ITEM;

        RegisterType classType = RegisterType.getRegisterTypeForTypeIdItem((TypeIdItem)item);

        setDestinationRegisterTypeAndPropagateChanges(analyzedInstruction,
                RegisterType.getUnitializedReference(classType.type));
    }

    private void analyzeNewArray(AnalyzedInstruction analyzedInstruction) {
        InstructionWithReference instruction = (InstructionWithReference)analyzedInstruction.instruction;

        Item item = instruction.getReferencedItem();
        assert item.getItemType() == ItemType.TYPE_TYPE_ID_ITEM;

        RegisterType arrayType = RegisterType.getRegisterTypeForTypeIdItem((TypeIdItem)item);
        assert arrayType.type instanceof ClassPath.ArrayClassDef;

        setDestinationRegisterTypeAndPropagateChanges(analyzedInstruction, arrayType);
    }

    private void analyzeArrayDataOrSwitch(AnalyzedInstruction analyzedInstruction) {
        int dataAddressOffset = ((OffsetInstruction)analyzedInstruction.instruction).getTargetAddressOffset();

        int dataCodeAddress = this.getInstructionAddress(analyzedInstruction) + dataAddressOffset;
        AnalyzedInstruction dataAnalyzedInstruction = instructions.get(dataCodeAddress);

        if (dataAnalyzedInstruction != null) {
            dataAnalyzedInstruction.dead = false;

            //if there is a preceding nop, it's deadness should be the same
            AnalyzedInstruction priorInstruction =
                    instructions.valueAt(dataAnalyzedInstruction.getInstructionIndex()-1);
            if (priorInstruction.getInstruction().opcode == Opcode.NOP &&
                    !priorInstruction.getInstruction().getFormat().variableSizeFormat) {

                priorInstruction.dead = false;
            }
        }
    }

    private void analyzeFloatWideCmp(AnalyzedInstruction analyzedInstruction) {
        setDestinationRegisterTypeAndPropagateChanges(analyzedInstruction,
                RegisterType.getRegisterType(RegisterType.Category.Byte, null));
    }

    private void analyze32BitPrimitiveAget(AnalyzedInstruction analyzedInstruction,
                                             RegisterType.Category instructionCategory) {
        setDestinationRegisterTypeAndPropagateChanges(analyzedInstruction,
                RegisterType.getRegisterType(instructionCategory, null));
    }

    private void analyzeAgetWide(AnalyzedInstruction analyzedInstruction) {
        ThreeRegisterInstruction instruction = (ThreeRegisterInstruction)analyzedInstruction.instruction;

        RegisterType arrayRegisterType = analyzedInstruction.getPreInstructionRegisterType(instruction.getRegisterB());
        assert arrayRegisterType != null;

        if (arrayRegisterType.category != RegisterType.Category.Null) {
            assert arrayRegisterType.type != null;
            if (arrayRegisterType.type.getClassType().charAt(0) != '[') {
                throw new ValidationException(String.format("Cannot use aget-wide with non-array type %s",
                        arrayRegisterType.type.getClassType()));
            }

            assert arrayRegisterType.type instanceof ClassPath.ArrayClassDef;
            ClassPath.ArrayClassDef arrayClassDef = (ClassPath.ArrayClassDef)arrayRegisterType.type;

            char arrayBaseType = arrayClassDef.getBaseElementClass().getClassType().charAt(0);
            if (arrayBaseType == 'J') {
                setDestinationRegisterTypeAndPropagateChanges(analyzedInstruction,
                        RegisterType.getRegisterType(RegisterType.Category.LongLo, null));
            } else if (arrayBaseType == 'D') {
                setDestinationRegisterTypeAndPropagateChanges(analyzedInstruction,
                        RegisterType.getRegisterType(RegisterType.Category.DoubleLo, null));
            } else {
                throw new ValidationException(String.format("Cannot use aget-wide with array type %s. Incorrect " +
                        "array type for the instruction.", arrayRegisterType.type.getClassType()));
            }
        } else {
            setDestinationRegisterTypeAndPropagateChanges(analyzedInstruction,
                        RegisterType.getRegisterType(RegisterType.Category.LongLo, null));
        }
    }

    private void analyzeAgetObject(AnalyzedInstruction analyzedInstruction) {
        ThreeRegisterInstruction instruction = (ThreeRegisterInstruction)analyzedInstruction.instruction;

        RegisterType arrayRegisterType = analyzedInstruction.getPreInstructionRegisterType(instruction.getRegisterB());
        assert arrayRegisterType != null;

        if (arrayRegisterType.category != RegisterType.Category.Null) {
            assert arrayRegisterType.type != null;
            if (arrayRegisterType.type.getClassType().charAt(0) != '[') {
                throw new ValidationException(String.format("Cannot use aget-object with non-array type %s",
                        arrayRegisterType.type.getClassType()));
            }

            assert arrayRegisterType.type instanceof ClassPath.ArrayClassDef;
            ClassPath.ArrayClassDef arrayClassDef = (ClassPath.ArrayClassDef)arrayRegisterType.type;

            ClassPath.ClassDef elementClassDef = arrayClassDef.getImmediateElementClass();
            char elementTypePrefix = elementClassDef.getClassType().charAt(0);
            if (elementTypePrefix != 'L' && elementTypePrefix != '[') {
                throw new ValidationException(String.format("Cannot use aget-object with array type %s. Incorrect " +
                        "array type for the instruction.", arrayRegisterType.type.getClassType()));
            }

            setDestinationRegisterTypeAndPropagateChanges(analyzedInstruction,
                    RegisterType.getRegisterType(RegisterType.Category.Reference, elementClassDef));
        } else {
            setDestinationRegisterTypeAndPropagateChanges(analyzedInstruction,
                    RegisterType.getRegisterType(RegisterType.Category.Null, null));
        }
    }

    private void analyze32BitPrimitiveIget(AnalyzedInstruction analyzedInstruction,
                                             RegisterType.Category instructionCategory) {
        setDestinationRegisterTypeAndPropagateChanges(analyzedInstruction,
                RegisterType.getRegisterType(instructionCategory, null));
    }

    private void analyzeIgetWideObject(AnalyzedInstruction analyzedInstruction) {
        TwoRegisterInstruction instruction = (TwoRegisterInstruction)analyzedInstruction.instruction;

        Item referencedItem = ((InstructionWithReference)analyzedInstruction.instruction).getReferencedItem();
        assert referencedItem instanceof FieldIdItem;
        FieldIdItem field = (FieldIdItem)referencedItem;

        RegisterType fieldType = RegisterType.getRegisterTypeForTypeIdItem(field.getFieldType());
        setDestinationRegisterTypeAndPropagateChanges(analyzedInstruction, fieldType);
    }

    private void analyze32BitPrimitiveSget(AnalyzedInstruction analyzedInstruction,
                                             RegisterType.Category instructionCategory) {
        setDestinationRegisterTypeAndPropagateChanges(analyzedInstruction,
                RegisterType.getRegisterType(instructionCategory, null));
    }

    private void analyzeSgetWideObject(AnalyzedInstruction analyzedInstruction) {
        Item referencedItem = ((InstructionWithReference)analyzedInstruction.instruction).getReferencedItem();
        assert referencedItem instanceof FieldIdItem;
        FieldIdItem field = (FieldIdItem)referencedItem;

        RegisterType fieldType = RegisterType.getRegisterTypeForTypeIdItem(field.getFieldType());
        setDestinationRegisterTypeAndPropagateChanges(analyzedInstruction, fieldType);
    }

    private void analyzeInvokeDirect(AnalyzedInstruction analyzedInstruction) {
        FiveRegisterInstruction instruction = (FiveRegisterInstruction)analyzedInstruction.instruction;
        analyzeInvokeDirectCommon(analyzedInstruction, new Format35cRegisterIterator(instruction));
    }

    private void analyzeInvokeDirectRange(AnalyzedInstruction analyzedInstruction) {
        RegisterRangeInstruction instruction = (RegisterRangeInstruction)analyzedInstruction.instruction;
        analyzeInvokeDirectCommon(analyzedInstruction, new Format3rcRegisterIterator(instruction));
    }

    private static final int INVOKE_VIRTUAL = 0x01;
    private static final int INVOKE_SUPER = 0x02;
    private static final int INVOKE_DIRECT = 0x04;
    private static final int INVOKE_INTERFACE = 0x08;
    private static final int INVOKE_STATIC = 0x10;

    private void analyzeInvokeDirectCommon(AnalyzedInstruction analyzedInstruction, RegisterIterator registers) {
        //the only time that an invoke instruction changes a register type is when using invoke-direct on a
        //constructor (<init>) method, which changes the uninitialized reference (and any register that the same
        //uninit reference has been copied to) to an initialized reference

        InstructionWithReference instruction = (InstructionWithReference)analyzedInstruction.instruction;

        Item item = instruction.getReferencedItem();
        assert item.getItemType() == ItemType.TYPE_METHOD_ID_ITEM;
        MethodIdItem methodIdItem = (MethodIdItem)item;

        if (!methodIdItem.getMethodName().getStringValue().equals("<init>")) {
            return;
        }

        RegisterType objectRegisterType;
        //the object register is always the first register
        int objectRegister = registers.getRegister();

        objectRegisterType = analyzedInstruction.getPreInstructionRegisterType(objectRegister);
        assert objectRegisterType != null;

        if (objectRegisterType.category != RegisterType.Category.UninitRef &&
                objectRegisterType.category != RegisterType.Category.UninitThis) {
            return;
        }

        setPostRegisterTypeAndPropagateChanges(analyzedInstruction, objectRegister,
                RegisterType.getRegisterType(RegisterType.Category.Reference, objectRegisterType.type));

        for (int i=0; i<analyzedInstruction.postRegisterMap.length; i++) {
            RegisterType postInstructionRegisterType = analyzedInstruction.postRegisterMap[i];
            if (postInstructionRegisterType.category == RegisterType.Category.Unknown) {
                RegisterType preInstructionRegisterType =
                        analyzedInstruction.getPreInstructionRegisterType(i);

                if (preInstructionRegisterType.category == RegisterType.Category.UninitRef ||
                    preInstructionRegisterType.category == RegisterType.Category.UninitThis) {

                    RegisterType registerType;
                    if (preInstructionRegisterType == objectRegisterType) {
                        registerType = analyzedInstruction.postRegisterMap[objectRegister];
                    } else {
                        registerType = preInstructionRegisterType;
                    }

                    setPostRegisterTypeAndPropagateChanges(analyzedInstruction, i, registerType);
                }
            }
        }
    }

    private void analyzeUnaryOp(AnalyzedInstruction analyzedInstruction, RegisterType.Category destRegisterCategory) {
        setDestinationRegisterTypeAndPropagateChanges(analyzedInstruction,
                RegisterType.getRegisterType(destRegisterCategory, null));
    }

    private void analyzeBinaryOp(AnalyzedInstruction analyzedInstruction, RegisterType.Category destRegisterCategory,
                                boolean checkForBoolean) {
        if (checkForBoolean) {
            ThreeRegisterInstruction instruction = (ThreeRegisterInstruction)analyzedInstruction.instruction;

            RegisterType source1RegisterType =
                    analyzedInstruction.getPreInstructionRegisterType(instruction.getRegisterB());
            RegisterType source2RegisterType =
                    analyzedInstruction.getPreInstructionRegisterType(instruction.getRegisterC());

            if (BooleanCategories.contains(source1RegisterType.category) &&
                BooleanCategories.contains(source2RegisterType.category)) {

                destRegisterCategory = RegisterType.Category.Boolean;
            }
        }

        setDestinationRegisterTypeAndPropagateChanges(analyzedInstruction,
                RegisterType.getRegisterType(destRegisterCategory, null));
    }

    private void analyzeBinary2AddrOp(AnalyzedInstruction analyzedInstruction,
                                      RegisterType.Category destRegisterCategory, boolean checkForBoolean) {
        if (checkForBoolean) {
            TwoRegisterInstruction instruction = (TwoRegisterInstruction)analyzedInstruction.instruction;

            RegisterType source1RegisterType =
                    analyzedInstruction.getPreInstructionRegisterType(instruction.getRegisterA());
            RegisterType source2RegisterType =
                    analyzedInstruction.getPreInstructionRegisterType(instruction.getRegisterB());

            if (BooleanCategories.contains(source1RegisterType.category) &&
                BooleanCategories.contains(source2RegisterType.category)) {

                destRegisterCategory = RegisterType.Category.Boolean;
            }
        }

        setDestinationRegisterTypeAndPropagateChanges(analyzedInstruction,
                RegisterType.getRegisterType(destRegisterCategory, null));
    }

    private void analyzeLiteralBinaryOp(AnalyzedInstruction analyzedInstruction,
                                        RegisterType.Category destRegisterCategory, boolean checkForBoolean) {
        if (checkForBoolean) {
            TwoRegisterInstruction instruction = (TwoRegisterInstruction)analyzedInstruction.instruction;

            RegisterType sourceRegisterType =
                    analyzedInstruction.getPreInstructionRegisterType(instruction.getRegisterB());

            if (BooleanCategories.contains(sourceRegisterType.category)) {
                long literal = ((LiteralInstruction)analyzedInstruction.instruction).getLiteral();
                if (literal == 0 || literal == 1) {
                    destRegisterCategory = RegisterType.Category.Boolean;
                }
            }
        }

        setDestinationRegisterTypeAndPropagateChanges(analyzedInstruction,
                RegisterType.getRegisterType(destRegisterCategory, null));
    }

    private RegisterType.Category getDestTypeForLiteralShiftRight(AnalyzedInstruction analyzedInstruction,
                                                                  boolean signedShift) {
        TwoRegisterInstruction instruction = (TwoRegisterInstruction)analyzedInstruction.instruction;

        RegisterType sourceRegisterType = getAndCheckSourceRegister(analyzedInstruction, instruction.getRegisterB(),
                Primitive32BitCategories);
        long literalShift = ((LiteralInstruction)analyzedInstruction.instruction).getLiteral();

        if (literalShift == 0) {
            return sourceRegisterType.category;
        }

        RegisterType.Category destRegisterCategory;
        if (!signedShift) {
            destRegisterCategory = RegisterType.Category.Integer;
        } else {
            destRegisterCategory = sourceRegisterType.category;
        }

        if (literalShift >= 32) {
            //TODO: add warning
            return destRegisterCategory;
        }

        switch (sourceRegisterType.category) {
            case Integer:
            case Float:
                if (!signedShift) {
                    if (literalShift > 24) {
                        return RegisterType.Category.PosByte;
                    }
                    if (literalShift >= 16) {
                        return RegisterType.Category.Char;
                    }
                } else {
                    if (literalShift >= 24) {
                        return RegisterType.Category.Byte;
                    }
                    if (literalShift >= 16) {
                        return RegisterType.Category.Short;
                    }
                }
                break;
            case Short:
                if (signedShift && literalShift >= 8) {
                    return RegisterType.Category.Byte;
                }
                break;
            case PosShort:
                if (literalShift >= 8) {
                    return RegisterType.Category.PosByte;
                }
                break;
            case Char:
                if (literalShift > 8) {
                    return RegisterType.Category.PosByte;
                }
                break;
            case Byte:
                break;
            case PosByte:
                return RegisterType.Category.PosByte;
            case Null:
            case One:
            case Boolean:
                return RegisterType.Category.Null;
            default:
                assert false;
        }

        return destRegisterCategory;
    }


    private void analyzeExecuteInline(AnalyzedInstruction analyzedInstruction) {
        if (deodexUtil == null) {
            throw new ValidationException("Cannot analyze an odexed instruction unless we are deodexing");
        }

        Instruction35mi instruction = (Instruction35mi)analyzedInstruction.instruction;

        DeodexUtil.InlineMethod inlineMethod = deodexUtil.lookupInlineMethod(analyzedInstruction);
        MethodIdItem inlineMethodIdItem = inlineMethod.getMethodIdItem(deodexUtil);
        if (inlineMethodIdItem == null) {
            throw new ValidationException(String.format("Cannot load inline method with index %d",
                    instruction.getInlineIndex()));
        }

        Opcode deodexedOpcode = null;
        switch (inlineMethod.methodType) {
            case DeodexUtil.Direct:
                deodexedOpcode = Opcode.INVOKE_DIRECT;
                break;
            case DeodexUtil.Static:
                deodexedOpcode = Opcode.INVOKE_STATIC;
                break;
            case DeodexUtil.Virtual:
                deodexedOpcode = Opcode.INVOKE_VIRTUAL;
                break;
            default:
                assert false;
        }

        Instruction35c deodexedInstruction = new Instruction35c(deodexedOpcode, instruction.getRegCount(),
                instruction.getRegisterD(), instruction.getRegisterE(), instruction.getRegisterF(),
                instruction.getRegisterG(), instruction.getRegisterA(), inlineMethodIdItem);

        analyzedInstruction.setDeodexedInstruction(deodexedInstruction);

        analyzeInstruction(analyzedInstruction);
    }

    private void analyzeExecuteInlineRange(AnalyzedInstruction analyzedInstruction) {
        if (deodexUtil == null) {
            throw new ValidationException("Cannot analyze an odexed instruction unless we are deodexing");
        }

        Instruction3rmi instruction = (Instruction3rmi)analyzedInstruction.instruction;

        DeodexUtil.InlineMethod inlineMethod = deodexUtil.lookupInlineMethod(analyzedInstruction);
        MethodIdItem inlineMethodIdItem = inlineMethod.getMethodIdItem(deodexUtil);
        if (inlineMethodIdItem == null) {
            throw new ValidationException(String.format("Cannot load inline method with index %d",
                    instruction.getInlineIndex()));
        }

        Opcode deodexedOpcode = null;
        switch (inlineMethod.methodType) {
            case DeodexUtil.Direct:
                deodexedOpcode = Opcode.INVOKE_DIRECT_RANGE;
                break;
            case DeodexUtil.Static:
                deodexedOpcode = Opcode.INVOKE_STATIC_RANGE;
                break;
            case DeodexUtil.Virtual:
                deodexedOpcode = Opcode.INVOKE_VIRTUAL_RANGE;
                break;
            default:
                assert false;
        }

        Instruction3rc deodexedInstruction = new Instruction3rc(deodexedOpcode, (short)instruction.getRegCount(),
                instruction.getStartRegister(), inlineMethodIdItem);

        analyzedInstruction.setDeodexedInstruction(deodexedInstruction);

        analyzeInstruction(analyzedInstruction);
    }

    private void analyzeInvokeDirectEmpty(AnalyzedInstruction analyzedInstruction) {
        analyzeInvokeDirectEmpty(analyzedInstruction, true);
    }

    private void analyzeInvokeDirectEmpty(AnalyzedInstruction analyzedInstruction, boolean analyzeResult) {
        Instruction35c instruction = (Instruction35c)analyzedInstruction.instruction;

        Instruction35c deodexedInstruction = new Instruction35c(Opcode.INVOKE_DIRECT, instruction.getRegCount(),
                instruction.getRegisterD(), instruction.getRegisterE(), instruction.getRegisterF(),
                instruction.getRegisterG(), instruction.getRegisterA(), instruction.getReferencedItem());

        analyzedInstruction.setDeodexedInstruction(deodexedInstruction);

        if (analyzeResult) {
            analyzeInstruction(analyzedInstruction);
        }
    }

    private void analyzeInvokeObjectInitRange(AnalyzedInstruction analyzedInstruction) {
        analyzeInvokeObjectInitRange(analyzedInstruction, true);
    }

    private void analyzeInvokeObjectInitRange(AnalyzedInstruction analyzedInstruction, boolean analyzeResult) {
        Instruction3rc instruction = (Instruction3rc)analyzedInstruction.instruction;

        Instruction3rc deodexedInstruction = new Instruction3rc(Opcode.INVOKE_DIRECT_RANGE,
                (short)instruction.getRegCount(), instruction.getStartRegister(), instruction.getReferencedItem());

        analyzedInstruction.setDeodexedInstruction(deodexedInstruction);

        if (analyzeResult) {
            analyzeInstruction(analyzedInstruction);
        }
    }

    private boolean analyzeIputIgetQuick(AnalyzedInstruction analyzedInstruction) {
        Instruction22cs instruction = (Instruction22cs)analyzedInstruction.instruction;

        int fieldOffset = instruction.getFieldOffset();
        RegisterType objectRegisterType = getAndCheckSourceRegister(analyzedInstruction, instruction.getRegisterB(),
                ReferenceOrUninitCategories);

        if (objectRegisterType.category == RegisterType.Category.Null) {
            return false;
        }

        FieldIdItem fieldIdItem = deodexUtil.lookupField(objectRegisterType.type, fieldOffset);
        if (fieldIdItem == null) {
            throw new ValidationException(String.format("Could not resolve the field in class %s at offset %d",
                    objectRegisterType.type.getClassType(), fieldOffset));
        }

        String fieldType = fieldIdItem.getFieldType().getTypeDescriptor();

        Opcode opcode = OdexedFieldInstructionMapper.getAndCheckDeodexedOpcodeForOdexedOpcode(fieldType, instruction.opcode);

        Instruction22c deodexedInstruction = new Instruction22c(opcode, (byte)instruction.getRegisterA(),
                (byte)instruction.getRegisterB(), fieldIdItem);
        analyzedInstruction.setDeodexedInstruction(deodexedInstruction);

        analyzeInstruction(analyzedInstruction);

        return true;
    }

    private boolean analyzeInvokeVirtualQuick(AnalyzedInstruction analyzedInstruction, boolean isSuper,
                                              boolean isRange) {
        int methodIndex;
        int objectRegister;


        if (isRange) {
            Instruction3rms instruction = (Instruction3rms)analyzedInstruction.instruction;
            methodIndex = instruction.getVtableIndex();
            objectRegister = instruction.getStartRegister();
        } else {
            Instruction35ms instruction = (Instruction35ms)analyzedInstruction.instruction;
            methodIndex = instruction.getVtableIndex();
            objectRegister = instruction.getRegisterD();
        }

        RegisterType objectRegisterType = getAndCheckSourceRegister(analyzedInstruction, objectRegister,
                ReferenceOrUninitCategories);

        if (objectRegisterType.category == RegisterType.Category.Null) {
            return false;
        }

        MethodIdItem methodIdItem = null;
        ClassPath.ClassDef accessingClass =
                ClassPath.getClassDef(this.encodedMethod.method.getContainingClass(), false);
        if (accessingClass == null) {
            throw new ExceptionWithContext(String.format("Could not find ClassDef for current class: %s",
                    this.encodedMethod.method.getContainingClass()));
        }
        if (isSuper) {
            if (accessingClass.getSuperclass() != null) {
                methodIdItem = deodexUtil.lookupVirtualMethod(accessingClass, accessingClass.getSuperclass(),
                        methodIndex);
            }

            if (methodIdItem == null) {
                //it's possible that the pre-odexed instruction had used the method from the current class instead
                //of from the superclass (although the superclass method is still what would actually be called).
                //And so the MethodIdItem for the superclass method may not be in the dex file. Let's try to get the
                //MethodIdItem for the method in the current class instead
                methodIdItem = deodexUtil.lookupVirtualMethod(accessingClass, accessingClass, methodIndex);
            }
        } else{
            methodIdItem = deodexUtil.lookupVirtualMethod(accessingClass, objectRegisterType.type, methodIndex);
        }

        if (methodIdItem == null) {
            throw new ValidationException(String.format("Could not resolve the method in class %s at index %d",
                    objectRegisterType.type.getClassType(), methodIndex));
        }


        Instruction deodexedInstruction;
        if (isRange) {
            Instruction3rms instruction = (Instruction3rms)analyzedInstruction.instruction;
            Opcode opcode;
            if (isSuper) {
                opcode = Opcode.INVOKE_SUPER_RANGE;
            } else {
                opcode = Opcode.INVOKE_VIRTUAL_RANGE;
            }

            deodexedInstruction = new Instruction3rc(opcode, (short)instruction.getRegCount(),
                    instruction.getStartRegister(), methodIdItem);
        } else {
            Instruction35ms instruction = (Instruction35ms)analyzedInstruction.instruction;
            Opcode opcode;
            if (isSuper) {
                opcode = Opcode.INVOKE_SUPER;
            } else {
                opcode = Opcode.INVOKE_VIRTUAL;
            }

            deodexedInstruction = new Instruction35c(opcode, instruction.getRegCount(),
                    instruction.getRegisterD(), instruction.getRegisterE(), instruction.getRegisterF(),
                    instruction.getRegisterG(), instruction.getRegisterA(), methodIdItem);
        }

        analyzedInstruction.setDeodexedInstruction(deodexedInstruction);
        analyzeInstruction(analyzedInstruction);

        return true;
    }

    private boolean analyzePutGetVolatile(AnalyzedInstruction analyzedInstruction) {
        return analyzePutGetVolatile(analyzedInstruction, true);
    }

    private boolean analyzePutGetVolatile(AnalyzedInstruction analyzedInstruction, boolean analyzeResult) {
        FieldIdItem fieldIdItem =
                (FieldIdItem)(((InstructionWithReference)analyzedInstruction.instruction).getReferencedItem());

        String fieldType = fieldIdItem.getFieldType().getTypeDescriptor();

        Opcode opcode = OdexedFieldInstructionMapper.getAndCheckDeodexedOpcodeForOdexedOpcode(fieldType,
                analyzedInstruction.instruction.opcode);

        Instruction deodexedInstruction;

        if (analyzedInstruction.instruction.opcode.isOdexedStaticVolatile()) {
            SingleRegisterInstruction instruction = (SingleRegisterInstruction)analyzedInstruction.instruction;
            if (analyzedInstruction.instruction.opcode.format == Format.Format21c) {
                deodexedInstruction = new Instruction21c(opcode, (byte)instruction.getRegisterA(), fieldIdItem);
            } else {
                assert(analyzedInstruction.instruction.opcode.format == Format.Format41c);
                deodexedInstruction = new Instruction41c(opcode, (byte)instruction.getRegisterA(), fieldIdItem);
            }
        } else {
            TwoRegisterInstruction instruction = (TwoRegisterInstruction)analyzedInstruction.instruction;

            if (analyzedInstruction.instruction.opcode.format == Format.Format22c) {
                deodexedInstruction = new Instruction22c(opcode, (byte)instruction.getRegisterA(),
                    (byte)instruction.getRegisterB(), fieldIdItem);
            } else {
                assert(analyzedInstruction.instruction.opcode.format == Format.Format52c);
                deodexedInstruction = new Instruction52c(opcode, (byte)instruction.getRegisterA(),
                    (byte)instruction.getRegisterB(), fieldIdItem);
            }
        }

        analyzedInstruction.setDeodexedInstruction(deodexedInstruction);

        if (analyzeResult) {
            analyzeInstruction(analyzedInstruction);
        }
        return true;
    }

    private void analyzeInvokeObjectInitJumbo(AnalyzedInstruction analyzedInstruction) {
        Instruction5rc instruction = (Instruction5rc)analyzedInstruction.instruction;

        Instruction5rc deodexedInstruction = new Instruction5rc(Opcode.INVOKE_DIRECT_JUMBO,
                instruction.getRegCount(), instruction.getStartRegister(), instruction.getReferencedItem());

        analyzedInstruction.setDeodexedInstruction(deodexedInstruction);

        analyzeInstruction(analyzedInstruction);
    }

    private static boolean checkArrayFieldAssignment(RegisterType.Category arrayFieldCategory,
                                                  RegisterType.Category instructionCategory) {
        if (arrayFieldCategory == instructionCategory) {
            return true;
        }

        if ((arrayFieldCategory == RegisterType.Category.Integer &&
             instructionCategory == RegisterType.Category.Float) ||
            (arrayFieldCategory == RegisterType.Category.Float &&
             instructionCategory == RegisterType.Category.Integer)) {
            return true;
        }
        return false;
    }

    private static RegisterType getAndCheckSourceRegister(AnalyzedInstruction analyzedInstruction, int registerNumber,
                                            EnumSet validCategories) {
        assert registerNumber >= 0 && registerNumber < analyzedInstruction.postRegisterMap.length;

        RegisterType registerType = analyzedInstruction.getPreInstructionRegisterType(registerNumber);
        assert registerType != null;

        checkRegister(registerType, registerNumber, validCategories);

        if (validCategories == WideLowCategories) {
            checkRegister(registerType, registerNumber, WideLowCategories);
            checkWidePair(registerNumber, analyzedInstruction);

            RegisterType secondRegisterType = analyzedInstruction.getPreInstructionRegisterType(registerNumber + 1);
            assert secondRegisterType != null;
            checkRegister(secondRegisterType, registerNumber+1, WideHighCategories);
        }

        return registerType;
    }

    private static void checkRegister(RegisterType registerType, int registerNumber, EnumSet validCategories) {
        if (!validCategories.contains(registerType.category)) {
            throw new ValidationException(String.format("Invalid register type %s for register v%d.",
                    registerType.toString(), registerNumber));
        }
    }

    private static void checkWidePair(int registerNumber, AnalyzedInstruction analyzedInstruction) {
        if (registerNumber + 1 >= analyzedInstruction.postRegisterMap.length) {
            throw new ValidationException(String.format("v%d cannot be used as the first register in a wide register" +
                    "pair because it is the last register.", registerNumber));
        }
    }

    private static interface RegisterIterator {
        int getRegister();
        boolean moveNext();
        int getCount();
        boolean pastEnd();
    }

    private static class Format35cRegisterIterator implements RegisterIterator {
        private final int registerCount;
        private final int[] registers;
        private int currentRegister = 0;

        public Format35cRegisterIterator(FiveRegisterInstruction instruction) {
            registerCount = instruction.getRegCount();
            registers = new int[]{instruction.getRegisterD(), instruction.getRegisterE(),
                                  instruction.getRegisterF(), instruction.getRegisterG(),
                                  instruction.getRegisterA()};
        }

        public int getRegister() {
            return registers[currentRegister];
        }

        public boolean moveNext() {
            currentRegister++;
            return !pastEnd();
        }

        public int getCount() {
            return registerCount;
        }

        public boolean pastEnd() {
            return currentRegister >= registerCount;
        }
    }

    private static class Format3rcRegisterIterator implements RegisterIterator {
        private final int startRegister;
        private final int registerCount;
        private int currentRegister = 0;

        public Format3rcRegisterIterator(RegisterRangeInstruction instruction) {
            startRegister = instruction.getStartRegister();
            registerCount = instruction.getRegCount();
        }

        public int getRegister() {
            return startRegister + currentRegister;
        }

        public boolean moveNext() {
            currentRegister++;
            return !pastEnd();
        }

        public int getCount() {
            return registerCount;
        }

        public boolean pastEnd() {
            return currentRegister >= registerCount;
        }
    }
}
