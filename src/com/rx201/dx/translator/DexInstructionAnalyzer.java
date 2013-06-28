package com.rx201.dx.translator;

import java.util.EnumSet;
import java.util.List;

import org.jf.dexlib.Code.Analysis.ClassPath;
import org.jf.dexlib.Code.Analysis.RegisterType;
import org.jf.dexlib.Code.Analysis.ValidationException;

import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCatch;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCatchAll;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
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
import uk.ac.cam.db538.dexter.dex.code.insn.macro.DexMacro_FilledNewArray;
import uk.ac.cam.db538.dexter.dex.code.insn.macro.DexMacro_GetInternalClassAnnotation;
import uk.ac.cam.db538.dexter.dex.code.insn.macro.DexMacro_GetInternalMethodAnnotation;
import uk.ac.cam.db538.dexter.dex.code.insn.macro.DexMacro_GetMethodCaller;
import uk.ac.cam.db538.dexter.dex.code.insn.macro.DexMacro_GetObjectTaint;
import uk.ac.cam.db538.dexter.dex.code.insn.macro.DexMacro_GetQueryTaint;
import uk.ac.cam.db538.dexter.dex.code.insn.macro.DexMacro_GetServiceTaint;
import uk.ac.cam.db538.dexter.dex.code.insn.macro.DexMacro_PrintInteger;
import uk.ac.cam.db538.dexter.dex.code.insn.macro.DexMacro_PrintIntegerConst;
import uk.ac.cam.db538.dexter.dex.code.insn.macro.DexMacro_PrintString;
import uk.ac.cam.db538.dexter.dex.code.insn.macro.DexMacro_PrintStringConst;
import uk.ac.cam.db538.dexter.dex.code.insn.macro.DexMacro_SetObjectTaint;
import uk.ac.cam.db538.dexter.dex.code.insn.invoke.DexPseudoinstruction_Invoke;
import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;

import com.rx201.dx.translator.RopType.Category;
import com.rx201.dx.translator.util.DexRegisterHelper;

public class DexInstructionAnalyzer implements DexInstructionVisitor{

	private AnalyzedDexInstruction analyzedInst;
	private DexCodeAnalyzer analyzer;
	private UseDefTypeAnalyzer typeAnalyzer;
	
	public DexInstructionAnalyzer(DexCodeAnalyzer analyzer) {
		this.analyzer = analyzer;
		this.typeAnalyzer = new UseDefTypeAnalyzer();
	}
	
	public void setAnalyzedInstruction(AnalyzedDexInstruction i) {
		this.analyzedInst = i;
	}
	
	private void setPostRopType(AnalyzedDexInstruction inst, DexRegister registerNumber, 
			RopType registerType) {
		analyzer.setPostRegisterTypeAndPropagateChanges(inst, registerNumber, registerType);
	}

    private void setDestinationRopType(AnalyzedDexInstruction analyzedInstruction, 
    		RopType registerType) {
    	setPostRopType(analyzedInstruction, analyzedInstruction.getDestinationRegister(), registerType);
	}
	
    private void setDestinationRopType(AnalyzedDexInstruction analyzedInstruction,
            DexRegisterType dexType) {
    	setDestinationRopType(analyzedInstruction, dexType.getDescriptor());
    }
    
    private void setDestinationRopType(AnalyzedDexInstruction analyzedInstruction,
            String typeDescriptor) {
    	setDestinationRopType(analyzedInstruction, RopType.getRopType(typeDescriptor));
    }
    
    
	public void defineRegister(DexRegister regTo, RopType registerType) {
		analyzedInst.defineRegister(regTo, registerType);
	}
	public void useRegister(DexRegister regFrom, RopType registerType) {
		analyzedInst.useRegister(regFrom, registerType);
	}
	public void moveRegister(DexRegister regFrom, DexRegister regTo, RopType registerType) {
		analyzedInst.useRegister(regFrom, registerType);
		analyzedInst.defineRegister(regTo, registerType);
		analyzedInst.moveRegister(regFrom, regTo);

		RopType valueType = analyzedInst.getPreRegisterType(regFrom);
		setDestinationRopType(analyzedInst, valueType);
	}
	    
    
	@Override
	public void visit(DexInstruction_Nop instruction) {}
	
	@Override
	public void visit(DexInstruction_Move instruction) {
		moveRegister(instruction.getRegFrom(), instruction.getRegTo(), instruction.isObjectMoving()? RopType.Reference : RopType.Primitive);
	}
	@Override
	public void visit(DexInstruction_MoveWide instruction) {
		assert DexRegisterHelper.isPair(instruction.getRegFrom1(),  instruction.getRegFrom2());
		assert DexRegisterHelper.isPair(instruction.getRegTo1(),  instruction.getRegTo2());

		moveRegister(instruction.getRegFrom1(), instruction.getRegTo1(), RopType.Wide);
		moveRegister(instruction.getRegFrom2(), instruction.getRegTo2(), RopType.Wide);

	}
	
	private RopType analyzeMoveResult(DexRegister srcReg) {
		
        AnalyzedDexInstruction prevAnalyzedInst = analyzedInst;
        //Skip auxillary blocks like TryBlockEnd etc.
        do {
    		assert prevAnalyzedInst.getPredecessorCount() == 1;
    		prevAnalyzedInst = prevAnalyzedInst.getPredecessors().get(0);
        } while (prevAnalyzedInst.instruction == null);
        
        RopType resultRopType;
        if (prevAnalyzedInst.instruction instanceof DexInstruction_Invoke) {
        	DexInstruction_Invoke i = (DexInstruction_Invoke) prevAnalyzedInst.instruction;
        	resultRopType = RopType.getRopType(i.getMethodPrototype().getReturnType().getDescriptor());
        } else if (prevAnalyzedInst.instruction instanceof DexInstruction_FilledNewArray) {
        	DexInstruction_FilledNewArray i = (DexInstruction_FilledNewArray)prevAnalyzedInst.instruction;
        	resultRopType = RopType.getRopType(i.getArrayType().getDescriptor());
        } else {
            throw new ValidationException(analyzedInst.instruction.getOriginalAssembly() + " must occur after an " +
                    "invoke-*/fill-new-array instruction");
        }
        
        setDestinationRopType(analyzedInst, resultRopType);	
        return resultRopType;
	}
	@Override
	public void visit(DexInstruction_MoveResult instruction) {
		defineRegister(instruction.getRegTo(), 
				analyzeMoveResult(instruction.getRegTo()));
	}
	@Override
	public void visit(DexInstruction_MoveResultWide instruction) {
		assert DexRegisterHelper.isPair(instruction.getRegTo1(),  instruction.getRegTo2());
		RopType type = analyzeMoveResult(instruction.getRegTo1());
		defineRegister(instruction.getRegTo1(), type);
		defineRegister(instruction.getRegTo2(), type.lowToHigh());
	}
	@Override
	public void visit(DexInstruction_MoveException instruction) {
		assert analyzedInst.getPredecessorCount() == 1;
		DexCodeElement catchElement = analyzedInst.getPredecessors().get(0).auxillaryElement;
		assert catchElement != null && (catchElement instanceof DexCatch || catchElement instanceof DexCatchAll);
		String exception = null;
		if (catchElement instanceof DexCatch)
			exception = ((DexCatch)catchElement).getExceptionType().getDescriptor();
		else 
			exception = "Ljava/lang/Throwable;";
				
		RopType type = RopType.getRopType(exception);
		setDestinationRopType(analyzedInst, type);
		defineRegister(instruction.getRegTo(), type);
	}
	@Override
	public void visit(DexInstruction_ReturnVoid instruction) {}
	
	@Override
	public void visit(DexInstruction_Return instruction) {
		String returnType = instruction.getParentMethod().getPrototype().getReturnType().getDescriptor();
		useRegister(instruction.getRegFrom(), RopType.getRopType(returnType));
	}
	
	@Override
	public void visit(DexInstruction_ReturnWide instruction) {
		if (instruction.getParentMethod().getPrototype().getReturnType().getDescriptor().equals("J")) {
			useRegister(instruction.getRegFrom1(), RopType.LongLo);
			useRegister(instruction.getRegFrom2(), RopType.LongHi);
		} else {
			useRegister(instruction.getRegFrom1(), RopType.DoubleLo);
			useRegister(instruction.getRegFrom2(), RopType.DoubleHi);
		}
	}
	
	@Override
	public void visit(DexInstruction_Const instruction) {
		long value = instruction.getValue();
		RopType type;
		if (value == 0)
			type = RopType.Zero;
		else if (value == 1)
			type = RopType.One;
		else
			type = RopType.Integer;
		//??
		setDestinationRopType(analyzedInst, type);
		defineRegister(instruction.getRegTo(), type);
	}

	@Override
	public void visit(DexInstruction_ConstWide instruction) {
		//??
		setDestinationRopType(analyzedInst, RopType.Wide);
		
		defineRegister(instruction.getRegTo1(), RopType.Wide);
		defineRegister(instruction.getRegTo2(), RopType.Wide);
	}
	
	@Override
	public void visit(DexInstruction_ConstString instruction) {
		RopType type = RopType.getRopType("Ljava/lang/String;");
		setDestinationRopType(analyzedInst, type);
		defineRegister(instruction.getRegTo(), type);
	}
	@Override
	public void visit(DexInstruction_ConstClass instruction) {
		RopType type = RopType.getRopType(instruction.getValue().getDescriptor());
		setDestinationRopType(analyzedInst, type);
		defineRegister(instruction.getRegTo(), type);
	}
	@Override
	public void visit(DexInstruction_Monitor instruction) {
		useRegister(instruction.getRegMonitor(), RopType.Reference);
	}
	@Override
	public void visit(DexInstruction_CheckCast instruction) {
		RopType type = RopType.getRopType(instruction.getValue().getDescriptor());
		setDestinationRopType(analyzedInst, type);
		useRegister(instruction.getRegObject(), type);
		defineRegister(instruction.getRegObject(), type);
	}
	@Override
	public void visit(DexInstruction_InstanceOf instruction) {
		defineRegister(instruction.getRegTo(), RopType.Boolean);
        setDestinationRopType(analyzedInst,RopType.Boolean);
        //??
		useRegister(instruction.getRegObject(), RopType.getRopType(instruction.getValue().getDescriptor()));
	}
	@Override
	public void visit(DexInstruction_ArrayLength instruction) {
        setDestinationRopType(analyzedInst, RopType.Integer);
        //??
		useRegister(instruction.getRegArray(), RopType.Array);
		defineRegister(instruction.getRegTo(), RopType.Integer);
	}
	@Override
	public void visit(DexInstruction_NewInstance instruction) {
		RopType type = RopType.getRopType(instruction.getValue().getDescriptor());
		setDestinationRopType(analyzedInst, type);

		defineRegister(instruction.getRegTo(), type);
	}
	@Override
	public void visit(DexInstruction_NewArray instruction) {
		RopType type = RopType.getRopType(instruction.getValue().getDescriptor());
		setDestinationRopType(analyzedInst, type);
		useRegister(instruction.getRegSize(), RopType.Integer);
		defineRegister(instruction.getRegTo(), type);
	}
	
	@Override
	public void visit(DexInstruction_FilledNewArray instruction) {
		RopType elementType = RopType.getRopType(instruction.getArrayType().getElementType().getDescriptor());
		for(DexRegister argument : instruction.getArgumentRegisters()) {
			useRegister(argument, elementType);
		}
	}
	
	@Override
	public void visit(DexInstruction_FillArray instruction) {
		useRegister(instruction.getRegArray(), RopType.Array);
	}
	
	@Override
	public void visit(DexInstruction_FillArrayData instruction) {}
	
	@Override
	public void visit(DexInstruction_Throw instruction) {
		useRegister(instruction.getRegFrom(), RopType.Reference);
	}
	
	@Override
	public void visit(DexInstruction_Goto instruction) {}
	
	@Override
	public void visit(DexInstruction_Switch instruction) {
		useRegister(instruction.getRegTest(), RopType.Integer);
	}
	
	@Override
	public void visit(DexInstruction_PackedSwitchData instruction) {}
	
	@Override
	public void visit(DexInstruction_SparseSwitchData instruction) {}
	
	@Override
	public void visit(DexInstruction_CompareFloat instruction) {
		setDestinationRopType(analyzedInst, RopType.Byte);

		useRegister(instruction.getRegSourceA(), RopType.Float);
		useRegister(instruction.getRegSourceB(), RopType.Float);
		defineRegister(instruction.getRegTo(), RopType.Byte);
	}
	@Override
	public void visit(DexInstruction_CompareWide instruction) {
		setDestinationRopType(analyzedInst, RopType.Byte);

		switch(instruction.getInsnOpcode()) {
		case CmpLong:
			useRegister(instruction.getRegSourceA1(), RopType.LongLo);
			useRegister(instruction.getRegSourceA2(), RopType.LongHi);
			useRegister(instruction.getRegSourceB1(), RopType.LongLo);
			useRegister(instruction.getRegSourceB2(), RopType.LongHi);
			break;
		case CmpgDouble:
		case CmplDouble:
			useRegister(instruction.getRegSourceA1(), RopType.DoubleLo);
			useRegister(instruction.getRegSourceA2(), RopType.DoubleHi);
			useRegister(instruction.getRegSourceB1(), RopType.DoubleLo);
			useRegister(instruction.getRegSourceB2(), RopType.DoubleHi);
			break;
		default:
			assert false;
			break;
		}
		defineRegister(instruction.getRegTo(), RopType.Byte);
	}
	@Override
	public void visit(DexInstruction_IfTest instruction) {}
	
	@Override
	public void visit(DexInstruction_IfTestZero instruction) {}
	
	@Override
	public void visit(DexInstruction_ArrayGet inst) {
		useRegister(inst.getRegIndex(), RopType.Integer);
		useRegister(inst.getRegArray(), RopType.Array);

    	if (inst.getOpcode() == Opcode_GetPut.Object) {
    		defineRegister(inst.getRegTo(), RopType.Reference);
    		
            RopType arrayRopType = analyzedInst.getPreRegisterType(inst.getRegArray());
            assert arrayRopType != null;

            if (arrayRopType.category != RopType.Category.Null) {
                assert arrayRopType.type != null;
                if (arrayRopType.type.getClassType().charAt(0) != '[') {
                    throw new ValidationException(String.format("Cannot use aget-object with non-array type %s",
                            arrayRopType.type.getClassType()));
                }

                assert arrayRopType.type instanceof ClassPath.ArrayClassDef;
                ClassPath.ArrayClassDef arrayClassDef = (ClassPath.ArrayClassDef)arrayRopType.type;

                ClassPath.ClassDef elementClassDef = arrayClassDef.getImmediateElementClass();
                char elementTypePrefix = elementClassDef.getClassType().charAt(0);
                if (elementTypePrefix != 'L' && elementTypePrefix != '[') {
                    throw new ValidationException(String.format("Cannot use aget-object with array type %s. Incorrect " +
                            "array type for the instruction.", arrayRopType.type.getClassType()));
                }

                RopType type = RopType.getRopType(elementClassDef.getClassType()); 
                setDestinationRopType(analyzedInst, type);
            } else {
                setDestinationRopType(analyzedInst, RopType.Null);
            }
    		
    	} else {
    		
	    	RopType.Category category;
	    	switch (inst.getOpcode()) {
			case Boolean:
				category = RopType.Category.Boolean;
				break;
			case Byte:
				category = RopType.Category.Byte;
				break;
			case Char:
				category = RopType.Category.Char;
				break;
			case IntFloat:
				//??
				category = RopType.Category.IntFloat; 
			case Short:
				category = RopType.Category.Short;
				break;
			default:
				throw new ValidationException("wrong type AGET");
	    	}
	    	RopType type = RopType.getRopType(category);
	    	setDestinationRopType(analyzedInst, type);
	    	defineRegister(inst.getRegTo(), type);
    	}
	}		
	@Override
	public void visit(DexInstruction_ArrayGetWide instruction) {
		useRegister(instruction.getRegArray(), RopType.Array);
		useRegister(instruction.getRegIndex(), RopType.Integer);

        RopType arrayRopType = analyzedInst.getPreRegisterType(instruction.getRegArray());
        assert arrayRopType != null;

        if (arrayRopType.category != RopType.Category.Null) {
            assert arrayRopType.type != null;
            if (arrayRopType.type.getClassType().charAt(0) != '[') {
                throw new ValidationException(String.format("Cannot use aget-wide with non-array type %s",
                        arrayRopType.type.getClassType()));
            }

            assert arrayRopType.type instanceof ClassPath.ArrayClassDef;
            ClassPath.ArrayClassDef arrayClassDef = (ClassPath.ArrayClassDef)arrayRopType.type;

            char arrayBaseType = arrayClassDef.getBaseElementClass().getClassType().charAt(0);
            if (arrayBaseType == 'J') {
                setDestinationRopType(analyzedInst, RopType.LongLo);
        		defineRegister(instruction.getRegTo1(), RopType.LongLo);
        		defineRegister(instruction.getRegTo2(), RopType.LongHi);
            } else if (arrayBaseType == 'D') {
                setDestinationRopType(analyzedInst,RopType.DoubleLo);
        		defineRegister(instruction.getRegTo1(), RopType.DoubleLo);
        		defineRegister(instruction.getRegTo2(), RopType.DoubleHi);
            } else {
                throw new ValidationException(String.format("Cannot use aget-wide with array type %s. Incorrect " +
                        "array type for the instruction.", arrayRopType.type.getClassType()));
            }
        } else {
    		defineRegister(instruction.getRegTo1(), RopType.Wide);
    		defineRegister(instruction.getRegTo2(), RopType.Wide);
            setDestinationRopType(analyzedInst, RopType.Wide);
        }
	}
	@Override
	public void visit(DexInstruction_ArrayPut instruction) {
		useRegister(instruction.getRegArray(), RopType.Array);
		useRegister(instruction.getRegIndex(), RopType.Integer);
		switch(instruction.getOpcode()) {
		case Boolean:
			useRegister(instruction.getRegFrom(), RopType.Boolean);
			break;
		case Byte:
			useRegister(instruction.getRegFrom(), RopType.Byte);
			break;
		case Char:
			useRegister(instruction.getRegFrom(), RopType.Char);
			break;
		case IntFloat:
			useRegister(instruction.getRegFrom(), RopType.IntFloat);
			break;
		case Object:
			useRegister(instruction.getRegFrom(), RopType.Reference);
			break;
		case Short:
			useRegister(instruction.getRegFrom(), RopType.Short);
			break;
		default:
			assert false;
			break;
		}
	}
	
	@Override
	public void visit(DexInstruction_ArrayPutWide instruction) {
		useRegister(instruction.getRegArray(), RopType.Array);
		useRegister(instruction.getRegIndex(), RopType.Integer);
		useRegister(instruction.getRegFrom1(), RopType.Wide);
		useRegister(instruction.getRegFrom2(), RopType.Wide);
	}
	
	@Override
	public void visit(DexInstruction_InstanceGet instruction) {
		useRegister(instruction.getRegObject(), RopType.getRopType(instruction.getFieldClass().getDescriptor()));
		defineRegister(instruction.getRegTo(), RopType.getRopType(instruction.getFieldType().getDescriptor()));
		
		setDestinationRopType(analyzedInst, instruction.getFieldType());

	}
	@Override
	public void visit(DexInstruction_InstanceGetWide instruction) {
		setDestinationRopType(analyzedInst, instruction.getFieldType());
		
		useRegister(instruction.getRegObject(), RopType.getRopType(instruction.getFieldClass().getDescriptor()));
		RopType type = RopType.getRopType(instruction.getFieldType().getDescriptor());
		defineRegister(instruction.getRegTo1(), type);
		defineRegister(instruction.getRegTo2(), type.lowToHigh());
		
	}
	@Override
	public void visit(DexInstruction_InstancePut instruction) {
		useRegister(instruction.getRegObject(), RopType.getRopType(instruction.getFieldClass().getDescriptor()));
		useRegister(instruction.getRegFrom(), RopType.getRopType(instruction.getFieldType().getDescriptor()));
	}
	
	@Override
	public void visit(DexInstruction_InstancePutWide instruction) {
		useRegister(instruction.getRegObject(), RopType.getRopType(instruction.getFieldClass().getDescriptor()));
		RopType type = RopType.getRopType(instruction.getFieldType().getDescriptor());
		useRegister(instruction.getRegFrom1(), type);
		useRegister(instruction.getRegFrom2(), type.lowToHigh());
	}
	
	@Override
	public void visit(DexInstruction_StaticGet instruction) {
		RopType type = RopType.getRopType(instruction.getFieldType().getDescriptor());
		
		setDestinationRopType(analyzedInst, type);
		defineRegister(instruction.getRegTo(), type);
	}
	@Override
	public void visit(DexInstruction_StaticGetWide instruction) {
		RopType type = RopType.getRopType(instruction.getFieldType().getDescriptor());
		
		setDestinationRopType(analyzedInst, type);
		defineRegister(instruction.getRegTo1(), type);
		defineRegister(instruction.getRegTo2(), type.lowToHigh());

	}
	@Override
	public void visit(DexInstruction_StaticPut instruction) {
		useRegister(instruction.getRegFrom(), RopType.getRopType(instruction.getFieldType().getDescriptor()));
	}
	
	@Override
	public void visit(DexInstruction_StaticPutWide instruction) {
		RopType type = RopType.getRopType(instruction.getFieldType().getDescriptor());
		
		useRegister(instruction.getRegFrom1(), type);
		useRegister(instruction.getRegFrom2(), type.lowToHigh());
	}
	
	
	@Override
	public void visit(DexInstruction_Invoke instruction) {
		List<DexRegister> arguments = instruction.getArgumentRegisters();
		List<DexRegisterType> parameterTypes = instruction.getMethodPrototype().getParameterTypes();
		
		int regIndex = 0;
		if (!instruction.isStaticCall()) {
			useRegister(arguments.get(regIndex++), RopType.getRopType(instruction.getClassType().getDescriptor()));
		}
		
		for(int i=0 ;i<parameterTypes.size(); i++) {
			DexRegisterType paramType = parameterTypes.get(i);
			useRegister(arguments.get(regIndex), RopType.getRopType(paramType.getDescriptor()));
			regIndex += paramType.getRegisters();
		}
	}

	@Override
	public void visit(DexInstruction_UnaryOp instruction) {
		RopType type;
		switch (instruction.getInsnOpcode()) {
			case NegFloat:
				type = RopType.Float;
				break;
			case NegInt:
			case NotInt:
				type = RopType.Integer;
				break;
		    default:
		    	throw new ValidationException("Unknown opcode for DexInstruction_UnaryOp");
		}
		setDestinationRopType(analyzedInst, type);

		useRegister(instruction.getRegFrom(), type);
		defineRegister(instruction.getRegTo(), type);
	}
	@Override
	public void visit(DexInstruction_UnaryOpWide instruction) {
		assert DexRegisterHelper.isPair(instruction.getRegFrom1(), instruction.getRegFrom2());
		assert DexRegisterHelper.isPair(instruction.getRegTo1(), instruction.getRegTo2());

		switch (instruction.getInsnOpcode()) {
	    case NegDouble:
			setDestinationRopType(analyzedInst, RopType.DoubleLo);
			useRegister(instruction.getRegFrom1(), RopType.DoubleLo);
			useRegister(instruction.getRegFrom2(), RopType.DoubleHi);
			defineRegister(instruction.getRegTo1(), RopType.DoubleLo);
			defineRegister(instruction.getRegTo2(), RopType.DoubleHi);
	    	break;
	    case NegLong:
	    case NotLong:
			setDestinationRopType(analyzedInst, RopType.LongLo);
			useRegister(instruction.getRegFrom1(), RopType.LongLo);
			useRegister(instruction.getRegFrom2(), RopType.LongHi);
			defineRegister(instruction.getRegTo1(), RopType.LongLo);
			defineRegister(instruction.getRegTo2(), RopType.LongHi);
	    	break;
	    default:
	    	throw new ValidationException("Unknown opcode for DexInstruction_UnaryOpWide");
	    }
	}
	@Override
	public void visit(DexInstruction_Convert instruction) {
	    switch (instruction.getInsnOpcode()) {
		case FloatToInt:
			setDestinationRopType(analyzedInst, RopType.Integer);
			useRegister(instruction.getRegFrom(), RopType.Float);
			defineRegister(instruction.getRegTo(), RopType.Integer);
			break;
		case IntToByte:
			setDestinationRopType(analyzedInst, RopType.Byte);
			useRegister(instruction.getRegFrom(), RopType.Integer);
			defineRegister(instruction.getRegTo(), RopType.Byte);
			break;
		case IntToChar:
			setDestinationRopType(analyzedInst, RopType.Char);
			useRegister(instruction.getRegFrom(), RopType.Integer);
			defineRegister(instruction.getRegTo(), RopType.Char);
			break;
		case IntToFloat:
			setDestinationRopType(analyzedInst, RopType.Float);
			useRegister(instruction.getRegFrom(), RopType.Integer);
			defineRegister(instruction.getRegTo(), RopType.Float);
			break;
		case IntToShort:
			setDestinationRopType(analyzedInst, RopType.Short);
			useRegister(instruction.getRegFrom(), RopType.Integer);
			defineRegister(instruction.getRegTo(), RopType.Short);
			break;
		default:
			throw new ValidationException("Unknown opcode for DexInstruction_Convert");
	    
	    }
	}
	@Override
	public void visit(DexInstruction_ConvertWide instruction) {
		assert DexRegisterHelper.isPair(instruction.getRegFrom1(), instruction.getRegFrom2());
		assert DexRegisterHelper.isPair(instruction.getRegTo1(), instruction.getRegTo2());

		if (instruction.getInsnOpcode() == Opcode_ConvertWide.DoubleToLong) {
			setDestinationRopType(analyzedInst, RopType.LongLo);
			useRegister(instruction.getRegFrom1(), RopType.DoubleLo);
			useRegister(instruction.getRegFrom2(), RopType.DoubleHi);
			defineRegister(instruction.getRegTo1(), RopType.LongLo);
			defineRegister(instruction.getRegTo2(), RopType.LongHi);
	    } else {
			setDestinationRopType(analyzedInst, RopType.DoubleLo);
			useRegister(instruction.getRegFrom1(), RopType.LongLo);
			useRegister(instruction.getRegFrom2(), RopType.LongHi);
			defineRegister(instruction.getRegTo1(), RopType.DoubleLo);
			defineRegister(instruction.getRegTo2(), RopType.DoubleHi);
	    }
	}
	@Override
	public void visit(DexInstruction_ConvertFromWide instruction) {
		assert DexRegisterHelper.isPair(instruction.getRegFrom1(), instruction.getRegFrom2());

		RopType srcType, dstType;
		
		switch (instruction.getInsnOpcode()) {
		case DoubleToFloat:
			srcType = RopType.DoubleLo;
			dstType = RopType.Float;
			break;
		case LongToFloat:
			srcType = RopType.LongLo;
			dstType = RopType.Float;
			break;
		case DoubleToInt:
			srcType = RopType.DoubleLo;
			dstType = RopType.Integer;
			break;
		case LongToInt:
			srcType = RopType.LongLo;
			dstType = RopType.Integer;
			break;
		default:
			throw new ValidationException("Unknown opcode for DexInstruction_ConvertFromWide");
	    }
		setDestinationRopType(analyzedInst, dstType);
		useRegister(instruction.getRegFrom1(), srcType);
		useRegister(instruction.getRegFrom2(), srcType.lowToHigh());
		defineRegister(instruction.getRegTo(), dstType);
	}
	@Override
	public void visit(DexInstruction_ConvertToWide instruction) {
		assert DexRegisterHelper.isPair(instruction.getRegTo1(), instruction.getRegTo2());

		RopType srcType, dstType;
	    switch (instruction.getInsnOpcode()) {
	    case FloatToDouble:
			srcType = RopType.Float;
			dstType = RopType.DoubleLo;
			break;
		case IntToDouble:
			srcType = RopType.Integer;
			dstType = RopType.DoubleLo;
			break;
		case FloatToLong:
			srcType = RopType.Float;
			dstType = RopType.LongLo;
			break;
		case IntToLong:
			srcType = RopType.Integer;
			dstType = RopType.LongLo;
			break;
		default:
	    	throw new ValidationException("Unknown opcode for DexInstruction_UnaryOpWide");
	    }
		setDestinationRopType(analyzedInst, dstType);
		useRegister(instruction.getRegFrom(), srcType);
		defineRegister(instruction.getRegTo1(), dstType);
		defineRegister(instruction.getRegTo2(), dstType.lowToHigh());
	}
	
    private void analyzeBinaryOp(DexRegister srcReg1, DexRegister srcReg2, 
    		Category destRegisterCategory, boolean checkForBoolean) {
    	//TODO
//		if (checkForBoolean) {
//			RopType source1RopType =
//			    analyzedInst.getPreRopType(srcReg1);
//			RopType source2RopType =
//			    analyzedInst.getPreRopType(srcReg2);
//			
//			if (BooleanCategories.contains(source1RopType.category) &&
//			BooleanCategories.contains(source2RopType.category)) {
//				destRegisterCategory = RopType.Category.Boolean;
//			}
//		}
//		
//		setDestinationRopType(analyzedInst,
//		RopType.getRopType(destRegisterCategory, null));
	}		
    
	@Override
	public void visit(DexInstruction_BinaryOp instruction) {
		RopType type;
		boolean checkForBoolean = false; // Is this int operation actually representing boolean?
		switch(instruction.getInsnOpcode()) {
		case AddFloat:
		case SubFloat:
		case DivFloat:
		case MulFloat:
		case RemFloat:
			type = RopType.Float;
			break;
		case AddInt:
		case SubInt:
		case MulInt:
		case DivInt:
		case RemInt:
		case ShlInt:
		case ShrInt:
		case UshrInt:
			type = RopType.Integer;
			break;
		case AndInt:
		case OrInt:
		case XorInt:
			type = RopType.Integer;
			checkForBoolean = true;
			break;
		default:
			throw new ValidationException("Unknown opcode for DexInstruction_BinaryOp");
		}
		setDestinationRopType(analyzedInst, type);
		useRegister(instruction.getRegSourceA(), type);
		useRegister(instruction.getRegSourceB(), type);
		defineRegister(instruction.getRegTarget(), type);
	}
	
	private Category getDestTypeForLiteralShiftRight(RopType sourceRopType, long literalShift, boolean signedShift) {
//		if (literalShift == 0) {
//			return sourceRopType.category;
//		}
//
//		RopType.Category destRegisterCategory;
//		if (!signedShift) {
//			destRegisterCategory = RopType.Category.Integer;
//		} else {
//			destRegisterCategory = sourceRopType.category;
//		}
//
//		if (literalShift >= 32) {
//			// TODO: add warning
//			return destRegisterCategory;
//		}
//
//		switch (sourceRopType.category) {
//		case Integer:
//		case Float:
//			if (!signedShift) {
//				if (literalShift > 24) {
//					return RopType.Category.PosByte;
//				}
//				if (literalShift >= 16) {
//					return RopType.Category.Char;
//				}
//			} else {
//				if (literalShift >= 24) {
//					return RopType.Category.Byte;
//				}
//				if (literalShift >= 16) {
//					return RopType.Category.Short;
//				}
//			}
//			break;
//		case Short:
//			if (signedShift && literalShift >= 8) {
//				return RopType.Category.Byte;
//			}
//			break;
//		case PosShort:
//			if (literalShift >= 8) {
//				return RopType.Category.PosByte;
//			}
//			break;
//		case Char:
//			if (literalShift > 8) {
//				return RopType.Category.PosByte;
//			}
//			break;
//		case Byte:
//			break;
//		case PosByte:
//			return RopType.Category.PosByte;
//		case Null:
//		case One:
//		case Boolean:
//			return RopType.Category.Null;
//		default:
//			throw new ValidationException("Unknown opcode for DexInstruction_BinaryOp");
//		}
//
//		return destRegisterCategory;
		return null;
	}
    		
	@Override
	public void visit(DexInstruction_BinaryOpLiteral instruction) {
		setDestinationRopType(analyzedInst, RopType.Integer);
		useRegister(instruction.getRegSource(), RopType.Integer);
		defineRegister(instruction.getRegTarget(), RopType.Integer);
	}
	@Override
	public void visit(DexInstruction_BinaryOpWide instruction) {
		assert DexRegisterHelper.isPair(instruction.getRegSourceA1(), instruction.getRegSourceA2());
		assert DexRegisterHelper.isPair(instruction.getRegSourceB1(), instruction.getRegSourceB2());
		assert DexRegisterHelper.isPair(instruction.getRegTarget1(), instruction.getRegTarget2());
		
		switch(instruction.getInsnOpcode()){
		case AddDouble:
		case SubDouble:
		case MulDouble:
		case DivDouble:
		case RemDouble:
			setDestinationRopType(analyzedInst, RopType.LongLo);
			useRegister(instruction.getRegSourceA1(), RopType.DoubleLo);
			useRegister(instruction.getRegSourceA2(), RopType.DoubleHi);
			useRegister(instruction.getRegSourceB1(), RopType.DoubleLo);
			useRegister(instruction.getRegSourceB2(), RopType.DoubleHi);
			defineRegister(instruction.getRegTarget1(), RopType.DoubleLo);
			defineRegister(instruction.getRegTarget2(), RopType.DoubleHi);
			break;
		case AddLong:
		case AndLong:
		case DivLong:
		case MulLong:
		case OrLong:
		case RemLong:
		case ShlLong:
		case ShrLong:
		case SubLong:
		case UshrLong:
		case XorLong:
			setDestinationRopType(analyzedInst, RopType.LongLo);
			useRegister(instruction.getRegSourceA1(), RopType.LongLo);
			useRegister(instruction.getRegSourceA2(), RopType.LongHi);
			useRegister(instruction.getRegSourceB1(), RopType.LongLo);
			useRegister(instruction.getRegSourceB2(), RopType.LongHi);
			defineRegister(instruction.getRegTarget1(), RopType.LongLo);
			defineRegister(instruction.getRegTarget2(), RopType.LongHi);
			break;
		default:
			throw new ValidationException("Unknown opcode for DexInstruction_BinaryOpWide");
		}
	}
	@Override
	public void visit(DexInstruction_Unknown instruction) {
		assert false;
	}

	@Override
	public void visit(DexMacro_FilledNewArray DexMacro_FilledNewArray) {}

	@Override
	public void visit(DexMacro_GetInternalClassAnnotation DexMacro_GetInternalClassAnnotation) {}

	@Override
	public void visit(DexMacro_GetInternalMethodAnnotation DexMacro_GetInternalMethodAnnotation) {}

	@Override
	public void visit(DexMacro_GetMethodCaller DexMacro_GetMethodCaller) {}

	@Override
	public void visit(DexMacro_GetObjectTaint DexMacro_GetObjectTaint) {}

	@Override
	public void visit(DexMacro_GetQueryTaint DexMacro_GetQueryTaint) {}

	@Override
	public void visit(DexMacro_GetServiceTaint DexMacro_GetServiceTaint) {}	
	
	@Override
	public void visit(DexMacro_PrintInteger DexMacro_PrintInteger) {}

	@Override
	public void visit(DexMacro_PrintIntegerConst DexMacro_PrintIntegerConst) {}

	@Override
	public void visit(DexMacro_PrintString DexMacro_PrintString) {}	
	
	@Override
	public void visit(DexMacro_PrintStringConst DexMacro_PrintStringConst) {}

	@Override
	public void visit(DexMacro_SetObjectTaint DexMacro_SetObjectTaint) {}
	
	@Override
	public void visit(DexPseudoinstruction_Invoke DexMacro_Invoke) {}
	
};
