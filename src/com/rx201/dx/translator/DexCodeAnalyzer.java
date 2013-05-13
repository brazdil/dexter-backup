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
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ArrayGet;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Nop;
import uk.ac.cam.db538.dexter.dex.method.DexPrototype;
import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;

public class DexCodeAnalyzer {
	private DexCode code;

    private HashMap<DexCodeElement, AnalyzedDexInstruction> instructionMap;
    private ArrayList<AnalyzedDexInstruction> instructions;

    private static final int NOT_ANALYZED = 0;
    private static final int ANALYZED = 1;
    private static final int VERIFIED = 2;
    
    private int analyzerState = NOT_ANALYZED;

//    private BitSet analyzedInstructions;


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
	        	pendingSetPostRegisterType(startOfMethod, DexRegisterHelper.normalize(paramId), regType);
	        	break;
	        case WIDE:
	        	RegisterType regTypeHi = null;
	        	
	        	if (regType.category == RegisterType.Category.DoubleLo)
	        		regTypeHi = RegisterType.getRegisterType(RegisterType.Category.DoubleHi, null);
	        	else if (regType.category == RegisterType.Category.LongLo)
	        		regTypeHi = RegisterType.getRegisterType(RegisterType.Category.LongHi, null);
	        	else
	        		throw new ValidationException("Bad register type.");
	        	
	        	pendingSetPostRegisterType(startOfMethod, DexRegisterHelper.normalize(paramId), regType);
	        	pendingSetPostRegisterType(startOfMethod, DexRegisterHelper.normalize(paramId + 1), regTypeHi);
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
        for(PendingRegType pending : pendingRegisterTypes) {
        	setPostRegisterTypeAndPropagateChanges(pending.instruction, pending.regNum, pending.regType);
        }

        BitSet instructionsToAnalyze = new BitSet(instructions.size());

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
            if (successor.mergeRegister(registerNumber, postRegisterType)) {
                changedInstructions.set(successor.getInstructionIndex());
            }
        }
    }

    
    private void buildInstructionList() {
    	instructions = new ArrayList<AnalyzedDexInstruction>();
    	instructionMap = new HashMap<DexCodeElement, AnalyzedDexInstruction>();
    	for(DexCodeElement inst : code.getInstructionList()) {
    		AnalyzedDexInstruction analyzedInst = analyzeDexCodeElement(instructions.size(), inst);
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

    private AnalyzedDexInstruction analyzeDexCodeElement(int index, DexCodeElement inst) {
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
    		return analyzeDexInstruction(index, (DexInstruction) inst);
    	} else /* DexCatch, DexCatchAll, DexLabel, DexTryBlockStart, DexTryBlockEnd */ {
    		return new AnalyzedDexInstruction(index, new DexInstruction_Nop(code), cache);
    	}
	}

    private  AnalyzedDexInstruction analyzeSetsResultInst(int index, DexInstruction inst, DexRegister dstReg, RegisterType dstType) {
    	AnalyzedDexInstruction result = new AnalyzedDexInstruction(index, inst, dstReg, false, cache);
    	pendingSetPostRegisterType(result, dstReg, dstType);
    	return result;
    }
    
    private AnalyzedDexInstruction analyzeArrayGet(int index, DexInstruction_ArrayGet inst) {
    	
    	if (inst.getOpcode() == Opcode_GetPut.Object) {

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
				category = RegisterType.Category.Boolean;
				break;
			default:
				throw new ValidationException("wrong type AGET");
	    	}
	    	return analyzeSetsResultInst(index, inst, inst.getRegTo(), RegisterType.getRegisterType(category, null));
    	}
    }
    
    private AnalyzedDexInstruction analyzeDexInstruction(int index, DexInstruction inst) {
        if (inst instanceof DexInstruction_ArrayGet) {
        	DexInstruction_ArrayGet i = (DexInstruction_ArrayGet)inst;
        	
        }
    }
    
	/**
     * @return false if analyzedInstruction is an odex instruction that couldn't be deodexed, due to its
     * object register being null
     */
    private boolean analyzeInstruction(AnalyzedInstruction analyzedInstruction) {
        Instruction instruction = analyzedInstruction.instruction;

        switch (instruction.opcode) {
            case NOP:
                return true;
            case MOVE:
            case MOVE_FROM16:
            case MOVE_16:
            case MOVE_WIDE:
            case MOVE_WIDE_FROM16:
            case MOVE_WIDE_16:
            case MOVE_OBJECT:
            case MOVE_OBJECT_FROM16:
            case MOVE_OBJECT_16:
                analyzeMove(analyzedInstruction);
                return true;
            case MOVE_RESULT:
            case MOVE_RESULT_WIDE:
            case MOVE_RESULT_OBJECT:
                analyzeMoveResult(analyzedInstruction);
                return true;
            case MOVE_EXCEPTION:
                analyzeMoveException(analyzedInstruction);
                return true;
            case RETURN_VOID:
            case RETURN:
            case RETURN_WIDE:
            case RETURN_OBJECT:
                return true;
            case RETURN_VOID_BARRIER:
                analyzeReturnVoidBarrier(analyzedInstruction);
                return true;
            case CONST_4:
            case CONST_16:
            case CONST:
                analyzeConst(analyzedInstruction);
                return true;
            case CONST_HIGH16:
                analyzeConstHigh16(analyzedInstruction);
                return true;
            case CONST_WIDE_16:
            case CONST_WIDE_32:
            case CONST_WIDE:
            case CONST_WIDE_HIGH16:
                analyzeWideConst(analyzedInstruction);
                return true;
            case CONST_STRING:
            case CONST_STRING_JUMBO:
                analyzeConstString(analyzedInstruction);
                return true;
            case CONST_CLASS:
            case CONST_CLASS_JUMBO:
                analyzeConstClass(analyzedInstruction);
                return true;
            case MONITOR_ENTER:
            case MONITOR_EXIT:
                return true;
            case CHECK_CAST:
            case CHECK_CAST_JUMBO:
                analyzeCheckCast(analyzedInstruction);
                return true;
            case INSTANCE_OF:
            case INSTANCE_OF_JUMBO:
                analyzeInstanceOf(analyzedInstruction);
                return true;
            case ARRAY_LENGTH:
                analyzeArrayLength(analyzedInstruction);
                return true;
            case NEW_INSTANCE:
            case NEW_INSTANCE_JUMBO:
                analyzeNewInstance(analyzedInstruction);
                return true;
            case NEW_ARRAY:
            case NEW_ARRAY_JUMBO:
                analyzeNewArray(analyzedInstruction);
                return true;
            case FILLED_NEW_ARRAY:
            case FILLED_NEW_ARRAY_RANGE:
            case FILLED_NEW_ARRAY_JUMBO:
                return true;
            case FILL_ARRAY_DATA:
                analyzeArrayDataOrSwitch(analyzedInstruction);
            case THROW:
            case GOTO:
            case GOTO_16:
            case GOTO_32:
                return true;
            case PACKED_SWITCH:
            case SPARSE_SWITCH:
                analyzeArrayDataOrSwitch(analyzedInstruction);
                return true;
            case CMPL_FLOAT:
            case CMPG_FLOAT:
            case CMPL_DOUBLE:
            case CMPG_DOUBLE:
            case CMP_LONG:
                analyzeFloatWideCmp(analyzedInstruction);
                return true;
            case IF_EQ:
            case IF_NE:
            case IF_LT:
            case IF_GE:
            case IF_GT:
            case IF_LE:
            case IF_EQZ:
            case IF_NEZ:
            case IF_LTZ:
            case IF_GEZ:
            case IF_GTZ:
            case IF_LEZ:
                return true;
            case AGET:
                analyze32BitPrimitiveAget(analyzedInstruction, RegisterType.Category.Integer);
                return true;
            case AGET_BOOLEAN:
                analyze32BitPrimitiveAget(analyzedInstruction, RegisterType.Category.Boolean);
                return true;
            case AGET_BYTE:
                analyze32BitPrimitiveAget(analyzedInstruction, RegisterType.Category.Byte);
                return true;
            case AGET_CHAR:
                analyze32BitPrimitiveAget(analyzedInstruction, RegisterType.Category.Char);
                return true;
            case AGET_SHORT:
                analyze32BitPrimitiveAget(analyzedInstruction, RegisterType.Category.Short);
                return true;
            case AGET_WIDE:
                analyzeAgetWide(analyzedInstruction);
                return true;
            case AGET_OBJECT:
                analyzeAgetObject(analyzedInstruction);
                return true;
            case APUT:
            case APUT_BOOLEAN:
            case APUT_BYTE:
            case APUT_CHAR:
            case APUT_SHORT:
            case APUT_WIDE:
            case APUT_OBJECT:
                return true;
            case IGET:
            case IGET_JUMBO:
                analyze32BitPrimitiveIget(analyzedInstruction, RegisterType.Category.Integer);
                return true;
            case IGET_BOOLEAN:
            case IGET_BOOLEAN_JUMBO:
                analyze32BitPrimitiveIget(analyzedInstruction, RegisterType.Category.Boolean);
                return true;
            case IGET_BYTE:
            case IGET_BYTE_JUMBO:
                analyze32BitPrimitiveIget(analyzedInstruction, RegisterType.Category.Byte);
                return true;
            case IGET_CHAR:
            case IGET_CHAR_JUMBO:
                analyze32BitPrimitiveIget(analyzedInstruction, RegisterType.Category.Char);
                return true;
            case IGET_SHORT:
            case IGET_SHORT_JUMBO:
                analyze32BitPrimitiveIget(analyzedInstruction, RegisterType.Category.Short);
                return true;
            case IGET_WIDE:
            case IGET_WIDE_JUMBO:
            case IGET_OBJECT:
            case IGET_OBJECT_JUMBO:
                analyzeIgetWideObject(analyzedInstruction);
                return true;
            case IPUT:
            case IPUT_JUMBO:
            case IPUT_BOOLEAN:
            case IPUT_BOOLEAN_JUMBO:
            case IPUT_BYTE:
            case IPUT_BYTE_JUMBO:
            case IPUT_CHAR:
            case IPUT_CHAR_JUMBO:
            case IPUT_SHORT:
            case IPUT_SHORT_JUMBO:
            case IPUT_WIDE:
            case IPUT_WIDE_JUMBO:
            case IPUT_OBJECT:
            case IPUT_OBJECT_JUMBO:
                return true;
            case SGET:
            case SGET_JUMBO:
                analyze32BitPrimitiveSget(analyzedInstruction, RegisterType.Category.Integer);
                return true;
            case SGET_BOOLEAN:
            case SGET_BOOLEAN_JUMBO:
                analyze32BitPrimitiveSget(analyzedInstruction, RegisterType.Category.Boolean);
                return true;
            case SGET_BYTE:
            case SGET_BYTE_JUMBO:
                analyze32BitPrimitiveSget(analyzedInstruction, RegisterType.Category.Byte);
                return true;
            case SGET_CHAR:
            case SGET_CHAR_JUMBO:
                analyze32BitPrimitiveSget(analyzedInstruction, RegisterType.Category.Char);
                return true;
            case SGET_SHORT:
            case SGET_SHORT_JUMBO:
                analyze32BitPrimitiveSget(analyzedInstruction, RegisterType.Category.Short);
                return true;
            case SGET_WIDE:
            case SGET_WIDE_JUMBO:
            case SGET_OBJECT:
            case SGET_OBJECT_JUMBO:
                analyzeSgetWideObject(analyzedInstruction);
                return true;
            case SPUT:
            case SPUT_JUMBO:
            case SPUT_BOOLEAN:
            case SPUT_BOOLEAN_JUMBO:
            case SPUT_BYTE:
            case SPUT_BYTE_JUMBO:
            case SPUT_CHAR:
            case SPUT_CHAR_JUMBO:
            case SPUT_SHORT:
            case SPUT_SHORT_JUMBO:
            case SPUT_WIDE:
            case SPUT_WIDE_JUMBO:
            case SPUT_OBJECT:
            case SPUT_OBJECT_JUMBO:
                return true;
            case INVOKE_VIRTUAL:
            case INVOKE_SUPER:
                return true;
            case INVOKE_DIRECT:
                analyzeInvokeDirect(analyzedInstruction);
                return true;
            case INVOKE_STATIC:
            case INVOKE_INTERFACE:
            case INVOKE_VIRTUAL_RANGE:
            case INVOKE_VIRTUAL_JUMBO:
            case INVOKE_SUPER_RANGE:
            case INVOKE_SUPER_JUMBO:
                return true;
            case INVOKE_DIRECT_RANGE:
            case INVOKE_DIRECT_JUMBO:
                analyzeInvokeDirectRange(analyzedInstruction);
                return true;
            case INVOKE_STATIC_RANGE:
            case INVOKE_STATIC_JUMBO:
            case INVOKE_INTERFACE_RANGE:
            case INVOKE_INTERFACE_JUMBO:
                return true;
            case NEG_INT:
            case NOT_INT:
                analyzeUnaryOp(analyzedInstruction, RegisterType.Category.Integer);
                return true;
            case NEG_LONG:
            case NOT_LONG:
                analyzeUnaryOp(analyzedInstruction, RegisterType.Category.LongLo);
                return true;
            case NEG_FLOAT:
                analyzeUnaryOp(analyzedInstruction, RegisterType.Category.Float);
                return true;
            case NEG_DOUBLE:
                analyzeUnaryOp(analyzedInstruction, RegisterType.Category.DoubleLo);
                return true;
            case INT_TO_LONG:
                analyzeUnaryOp(analyzedInstruction, RegisterType.Category.LongLo);
                return true;
            case INT_TO_FLOAT:
                analyzeUnaryOp(analyzedInstruction, RegisterType.Category.Float);
                return true;
            case INT_TO_DOUBLE:
                analyzeUnaryOp(analyzedInstruction, RegisterType.Category.DoubleLo);
                return true;
            case LONG_TO_INT:
            case DOUBLE_TO_INT:
                analyzeUnaryOp(analyzedInstruction, RegisterType.Category.Integer);
                return true;
            case LONG_TO_FLOAT:
            case DOUBLE_TO_FLOAT:
                analyzeUnaryOp(analyzedInstruction, RegisterType.Category.Float);
                return true;
            case LONG_TO_DOUBLE:
                analyzeUnaryOp(analyzedInstruction, RegisterType.Category.DoubleLo);
                return true;
            case FLOAT_TO_INT:
                analyzeUnaryOp(analyzedInstruction, RegisterType.Category.Integer);
                return true;
            case FLOAT_TO_LONG:
                analyzeUnaryOp(analyzedInstruction, RegisterType.Category.LongLo);
                return true;
            case FLOAT_TO_DOUBLE:
                analyzeUnaryOp(analyzedInstruction, RegisterType.Category.DoubleLo);
                return true;
            case DOUBLE_TO_LONG:
                analyzeUnaryOp(analyzedInstruction, RegisterType.Category.LongLo);
                return true;
            case INT_TO_BYTE:
                analyzeUnaryOp(analyzedInstruction, RegisterType.Category.Byte);
                return true;
            case INT_TO_CHAR:
                analyzeUnaryOp(analyzedInstruction, RegisterType.Category.Char);
                return true;
            case INT_TO_SHORT:
                analyzeUnaryOp(analyzedInstruction, RegisterType.Category.Short);
                return true;
            case ADD_INT:
            case SUB_INT:
            case MUL_INT:
            case DIV_INT:
            case REM_INT:
            case SHL_INT:
            case SHR_INT:
            case USHR_INT:
                analyzeBinaryOp(analyzedInstruction, RegisterType.Category.Integer, false);
                return true;
            case AND_INT:
            case OR_INT:
            case XOR_INT:
                analyzeBinaryOp(analyzedInstruction, RegisterType.Category.Integer, true);
                return true;
            case ADD_LONG:
            case SUB_LONG:
            case MUL_LONG:
            case DIV_LONG:
            case REM_LONG:
            case AND_LONG:
            case OR_LONG:
            case XOR_LONG:
            case SHL_LONG:
            case SHR_LONG:
            case USHR_LONG:
                analyzeBinaryOp(analyzedInstruction, RegisterType.Category.LongLo, false);
                return true;
            case ADD_FLOAT:
            case SUB_FLOAT:
            case MUL_FLOAT:
            case DIV_FLOAT:
            case REM_FLOAT:
                analyzeBinaryOp(analyzedInstruction, RegisterType.Category.Float, false);
                return true;
            case ADD_DOUBLE:
            case SUB_DOUBLE:
            case MUL_DOUBLE:
            case DIV_DOUBLE:
            case REM_DOUBLE:
                analyzeBinaryOp(analyzedInstruction, RegisterType.Category.DoubleLo, false);
                return true;
            case ADD_INT_2ADDR:
            case SUB_INT_2ADDR:
            case MUL_INT_2ADDR:
            case DIV_INT_2ADDR:
            case REM_INT_2ADDR:
            case SHL_INT_2ADDR:
            case SHR_INT_2ADDR:
            case USHR_INT_2ADDR:
                analyzeBinary2AddrOp(analyzedInstruction, RegisterType.Category.Integer, false);
                return true;
            case AND_INT_2ADDR:
            case OR_INT_2ADDR:
            case XOR_INT_2ADDR:
                analyzeBinary2AddrOp(analyzedInstruction, RegisterType.Category.Integer, true);
                return true;
            case ADD_LONG_2ADDR:
            case SUB_LONG_2ADDR:
            case MUL_LONG_2ADDR:
            case DIV_LONG_2ADDR:
            case REM_LONG_2ADDR:
            case AND_LONG_2ADDR:
            case OR_LONG_2ADDR:
            case XOR_LONG_2ADDR:
            case SHL_LONG_2ADDR:
            case SHR_LONG_2ADDR:
            case USHR_LONG_2ADDR:
                analyzeBinary2AddrOp(analyzedInstruction, RegisterType.Category.LongLo, false);
                return true;
            case ADD_FLOAT_2ADDR:
            case SUB_FLOAT_2ADDR:
            case MUL_FLOAT_2ADDR:
            case DIV_FLOAT_2ADDR:
            case REM_FLOAT_2ADDR:
                analyzeBinary2AddrOp(analyzedInstruction, RegisterType.Category.Float, false);
                return true;
            case ADD_DOUBLE_2ADDR:
            case SUB_DOUBLE_2ADDR:
            case MUL_DOUBLE_2ADDR:
            case DIV_DOUBLE_2ADDR:
            case REM_DOUBLE_2ADDR:
                analyzeBinary2AddrOp(analyzedInstruction, RegisterType.Category.DoubleLo, false);
                return true;
            case ADD_INT_LIT16:
            case RSUB_INT:
            case MUL_INT_LIT16:
            case DIV_INT_LIT16:
            case REM_INT_LIT16:
                analyzeLiteralBinaryOp(analyzedInstruction, RegisterType.Category.Integer, false);
                return true;
            case AND_INT_LIT16:
            case OR_INT_LIT16:
            case XOR_INT_LIT16:
                analyzeLiteralBinaryOp(analyzedInstruction, RegisterType.Category.Integer, true);
                return true;
            case ADD_INT_LIT8:
            case RSUB_INT_LIT8:
            case MUL_INT_LIT8:
            case DIV_INT_LIT8:
            case REM_INT_LIT8:
            case SHL_INT_LIT8:
                analyzeLiteralBinaryOp(analyzedInstruction, RegisterType.Category.Integer, false);
                return true;
            case AND_INT_LIT8:
            case OR_INT_LIT8:
            case XOR_INT_LIT8:
                analyzeLiteralBinaryOp(analyzedInstruction, RegisterType.Category.Integer, true);
                return true;
            case SHR_INT_LIT8:
                analyzeLiteralBinaryOp(analyzedInstruction, getDestTypeForLiteralShiftRight(analyzedInstruction, true),
                        false);
                return true;
            case USHR_INT_LIT8:
                analyzeLiteralBinaryOp(analyzedInstruction, getDestTypeForLiteralShiftRight(analyzedInstruction, false),
                        false);
                return true;

            /*odexed instructions*/
            case IGET_VOLATILE:
            case IPUT_VOLATILE:
            case SGET_VOLATILE:
            case SPUT_VOLATILE:
            case IGET_OBJECT_VOLATILE:
            case IGET_WIDE_VOLATILE:
            case IPUT_WIDE_VOLATILE:
            case SGET_WIDE_VOLATILE:
            case SPUT_WIDE_VOLATILE:
                analyzePutGetVolatile(analyzedInstruction);
                return true;
            case THROW_VERIFICATION_ERROR:
                return true;
            case EXECUTE_INLINE:
                analyzeExecuteInline(analyzedInstruction);
                return true;
            case EXECUTE_INLINE_RANGE:
                analyzeExecuteInlineRange(analyzedInstruction);
                return true;
            case INVOKE_DIRECT_EMPTY:
                analyzeInvokeDirectEmpty(analyzedInstruction);
                return true;
            case INVOKE_OBJECT_INIT_RANGE:
                analyzeInvokeObjectInitRange(analyzedInstruction);
                return true;
            case IGET_QUICK:
            case IGET_WIDE_QUICK:
            case IGET_OBJECT_QUICK:
            case IPUT_QUICK:
            case IPUT_WIDE_QUICK:
            case IPUT_OBJECT_QUICK:
                return analyzeIputIgetQuick(analyzedInstruction);
            case INVOKE_VIRTUAL_QUICK:
                return analyzeInvokeVirtualQuick(analyzedInstruction, false, false);
            case INVOKE_SUPER_QUICK:
                return analyzeInvokeVirtualQuick(analyzedInstruction, true, false);
            case INVOKE_VIRTUAL_QUICK_RANGE:
                return analyzeInvokeVirtualQuick(analyzedInstruction, false, true);
            case INVOKE_SUPER_QUICK_RANGE:
                return analyzeInvokeVirtualQuick(analyzedInstruction, true, true);
            case IPUT_OBJECT_VOLATILE:
            case SGET_OBJECT_VOLATILE:
            case SPUT_OBJECT_VOLATILE:
                analyzePutGetVolatile(analyzedInstruction);
                return true;
            case INVOKE_OBJECT_INIT_JUMBO:
                analyzeInvokeObjectInitJumbo(analyzedInstruction);
                return true;
            case IGET_VOLATILE_JUMBO:
            case IGET_WIDE_VOLATILE_JUMBO:
            case IGET_OBJECT_VOLATILE_JUMBO:
            case IPUT_VOLATILE_JUMBO:
            case IPUT_WIDE_VOLATILE_JUMBO:
            case IPUT_OBJECT_VOLATILE_JUMBO:
            case SGET_VOLATILE_JUMBO:
            case SGET_WIDE_VOLATILE_JUMBO:
            case SGET_OBJECT_VOLATILE_JUMBO:
            case SPUT_VOLATILE_JUMBO:
            case SPUT_WIDE_VOLATILE_JUMBO:
            case SPUT_OBJECT_VOLATILE_JUMBO:
                analyzePutGetVolatile(analyzedInstruction);
                return true;
            default:
                assert false;
                return true;
        }
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
