package com.rx201.dx.translator;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Analysis.AnalyzedInstruction;
import org.jf.dexlib.Code.Analysis.RegisterType;

import com.android.dx.rop.code.FillArrayDataInsn;
import com.android.dx.rop.code.Insn;
import com.android.dx.rop.code.PlainCstInsn;
import com.android.dx.rop.code.PlainInsn;
import com.android.dx.rop.code.RegOps;
import com.android.dx.rop.code.RegisterSpec;
import com.android.dx.rop.code.RegisterSpecList;
import com.android.dx.rop.code.Rop;
import com.android.dx.rop.code.Rops;
import com.android.dx.rop.code.SourcePosition;
import com.android.dx.rop.code.SwitchInsn;
import com.android.dx.rop.code.ThrowingCstInsn;
import com.android.dx.rop.code.ThrowingInsn;
import com.android.dx.rop.cst.Constant;
import com.android.dx.rop.cst.CstBaseMethodRef;
import com.android.dx.rop.cst.CstBoolean;
import com.android.dx.rop.cst.CstByte;
import com.android.dx.rop.cst.CstChar;
import com.android.dx.rop.cst.CstDouble;
import com.android.dx.rop.cst.CstFloat;
import com.android.dx.rop.cst.CstInteger;
import com.android.dx.rop.cst.CstInterfaceMethodRef;
import com.android.dx.rop.cst.CstLiteralBits;
import com.android.dx.rop.cst.CstLong;
import com.android.dx.rop.cst.CstMethodRef;
import com.android.dx.rop.cst.CstShort;
import com.android.dx.rop.cst.CstString;
import com.android.dx.rop.cst.CstType;
import com.android.dx.rop.cst.CstFieldRef;
import com.android.dx.rop.cst.CstNat;
import com.android.dx.rop.type.StdTypeList;
import com.android.dx.rop.type.Type;
import com.android.dx.rop.type.TypeList;
import com.android.dx.util.IntList;
import com.rx201.dx.translator.util.DexRegisterHelper;

import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCatch;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCatchAll;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.elem.DexLabel;
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
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_BinaryOpLiteral;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_Invoke;
import uk.ac.cam.db538.dexter.dex.code.insn.pseudo.DexPseudoinstruction_FilledNewArray;
import uk.ac.cam.db538.dexter.dex.code.insn.pseudo.DexPseudoinstruction_GetInternalClassAnnotation;
import uk.ac.cam.db538.dexter.dex.code.insn.pseudo.DexPseudoinstruction_GetInternalMethodAnnotation;
import uk.ac.cam.db538.dexter.dex.code.insn.pseudo.DexPseudoinstruction_GetMethodCaller;
import uk.ac.cam.db538.dexter.dex.code.insn.pseudo.DexPseudoinstruction_GetObjectTaint;
import uk.ac.cam.db538.dexter.dex.code.insn.pseudo.DexPseudoinstruction_GetQueryTaint;
import uk.ac.cam.db538.dexter.dex.code.insn.pseudo.DexPseudoinstruction_GetServiceTaint;
import uk.ac.cam.db538.dexter.dex.code.insn.pseudo.DexPseudoinstruction_PrintInteger;
import uk.ac.cam.db538.dexter.dex.code.insn.pseudo.DexPseudoinstruction_PrintIntegerConst;
import uk.ac.cam.db538.dexter.dex.code.insn.pseudo.DexPseudoinstruction_PrintString;
import uk.ac.cam.db538.dexter.dex.code.insn.pseudo.DexPseudoinstruction_PrintStringConst;
import uk.ac.cam.db538.dexter.dex.code.insn.pseudo.DexPseudoinstruction_SetObjectTaint;
import uk.ac.cam.db538.dexter.dex.code.insn.pseudo.invoke.DexPseudoinstruction_Invoke;
import uk.ac.cam.db538.dexter.dex.method.DexPrototype;
import uk.ac.cam.db538.dexter.dex.type.DexArrayType;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexReferenceType;
import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;
import uk.ac.cam.db538.dexter.dex.type.DexType;
import uk.ac.cam.db538.dexter.utils.Pair;


class DexConvertedResult {
	public ArrayList<Insn> insns;
	public ArrayList<Insn> auxInsns; // Insns that needs to be propagated to successor basic blocks.
	public AnalyzedDexInstruction primarySuccessor;
	public ArrayList<AnalyzedDexInstruction> successors;
	
	public DexConvertedResult() {
		insns = new ArrayList<Insn>();
		auxInsns = new ArrayList<Insn>();
		primarySuccessor = null;
		successors = new ArrayList<AnalyzedDexInstruction>();
	}

	public DexConvertedResult setPrimarySuccessor(AnalyzedDexInstruction successor){
		primarySuccessor = successor;
		return this;
	}
	
	public DexConvertedResult addSuccessor(AnalyzedDexInstruction s) {
		successors.add(s);
		return this;
	}
	
	public DexConvertedResult addInstruction(Insn insn) {
		insns.add(insn);
		return this;
	}
	
	public DexConvertedResult addAuxInstruction(Insn insn) {
		auxInsns.add(insn);
		return this;
	}	
}


public class DexInstructionTranslator implements DexInstructionVisitor {

	private DexCodeAnalyzer analyzer;
	private DexConvertedResult result;
	private AnalyzedDexInstruction curInst;
	
	public DexInstructionTranslator(DexCodeAnalyzer analyzer) {
		this.analyzer = analyzer;
	}
	
	
	public DexConvertedResult translate(AnalyzedDexInstruction inst) {
		result = new DexConvertedResult();
		curInst = inst;
		
		List<AnalyzedDexInstruction> successors = inst.getSuccesors();
		if (successors.size() > 0) {
			result.setPrimarySuccessor(successors.get(0));
			for(int i=0; i<successors.size(); i++)
				result.addSuccessor(successors.get(i));
		}
		
		inst.getInstruction().accept(this);
		
		return result;
	}

	private RegisterSpec toRegSpec(DexRegister reg, DexRegisterType type) {
		return RegisterSpec.make(reg.getOriginalIndex(), Type.intern(type.getDescriptor()));
	}

	private RegisterSpec getPreRegSpec(DexRegister reg) {
		return toRegSpec(reg, curInst.getPreInstructionRegisterType(reg));
	}
	
	private RegisterSpec getPostRegSpec(DexRegister reg) {
		return toRegSpec(reg, curInst.getPostInstructionRegisterType(reg));
	}

	private TypeList getCatches() {
		TypeList result = StdTypeList.EMPTY;
		for (AnalyzedDexInstruction i : curInst.getSuccesors()) {
			DexCodeElement aux = i.auxillaryElement;
			if (aux == null) continue;
			if (aux instanceof DexCatch) {
				result = result.withAddedType(Type.intern(((DexCatch)aux).getExceptionType().getDescriptor()));
			} else if (aux instanceof DexCatchAll) {
				//result = result.withAddedType(Type.intern("Ljava/lang/Exception;"));
				return StdTypeList.THROWABLE;
			}
		}
		return result;
	}
	
	private RegisterSpecList makeOperands(DexRegister ... operands) {
		if (operands.length == 0)
			return RegisterSpecList.EMPTY;
		RegisterSpecList result = new RegisterSpecList(operands.length);
		for(int i=0;i <operands.length; i++)
			result.set(i, getPreRegSpec(operands[i]));
		return result;
	}
	

	private CstLiteralBits makeCstLiteral(long value) {
		if (value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE)
			return  CstInteger.make((int)value);
		else
			return CstLong.make(value);
	}

	private CstString makeCstString(String stringConstant) {
		return new CstString(stringConstant);
	}

	private CstType makeCstType(DexReferenceType value) {
		return new CstType(Type.intern(value.getDescriptor()));
	}

	private CstNat makeCstNat(String name, String type) {
		return new CstNat(makeCstString(name), makeCstString(type));
	}
	
	private CstFieldRef makeFieldRef(DexClassType fieldClass, DexRegisterType fieldType, String fieldName) {
		return new CstFieldRef(
				makeCstType(fieldClass),
				makeCstNat(fieldName, fieldType.getDescriptor())
				);
	}
	
	private CstBaseMethodRef makeMethodRef(DexReferenceType classType, String methodName, DexPrototype methodPrototype, boolean isInterface) {
		CstType clazz = makeCstType(classType);
		CstNat method = makeCstNat(methodName, methodPrototype.getDescriptor());
		if (isInterface)
			return new CstInterfaceMethodRef(clazz, method);
		else
			return new CstMethodRef(clazz, method);
	}
	
////////////////////////////////////////////////////////////////////////////////
	private void doPlainInsn(Rop opcode, RegisterSpec dst, DexRegister ... srcs) {
		result.addInstruction(new PlainInsn(opcode, SourcePosition.NO_INFO, dst, makeOperands(srcs)));
	}
	
	private void doPlainCstInsn(Rop opcode, RegisterSpec dst, Constant constant, DexRegister ... srcs) {
		result.addInstruction(new PlainCstInsn(opcode, SourcePosition.NO_INFO, dst, makeOperands(srcs), constant));
	}
	
	private void doThrowingInsn(Rop opcode, DexRegister ... srcs) {
		result.addInstruction(new ThrowingInsn(opcode, SourcePosition.NO_INFO, makeOperands(srcs), getCatches()));
	}
	
	private void doThrowingCstInsn(Rop opcode, Constant constant, DexRegister ... srcs) {
		result.addInstruction(new ThrowingCstInsn(opcode, SourcePosition.NO_INFO, makeOperands(srcs), getCatches(), constant));
	}
	
	// instructions like INSTANCE_OF, ARRAY_LENGTH needs a pseudo move-result Insn, which (I guess) only
	// helps with flow analysis and does not contribute to the actual assembled code.
	private void doPseudoMoveResult(DexRegister to) {
		RegisterSpec dst = getPostRegSpec(to);
		result.addAuxInstruction(new PlainInsn(Rops.opMoveResultPseudo(dst), SourcePosition.NO_INFO, dst, RegisterSpecList.EMPTY));
	}
////////////////////////////////////////////////////////////////////////////////		
	@Override
	public void visit(DexInstruction_Nop instruction) {
		doPlainInsn(Rops.NOP, null);
	}


	private void doMove(DexRegister to, DexRegister from) {
		RegisterSpec dst = getPostRegSpec(to);
		doPlainInsn(Rops.opMove(dst), dst, from);
	}
	
	@Override
	public void visit(DexInstruction_Move instruction) {
		doMove(instruction.getRegTo(), instruction.getRegFrom());
	}


	@Override
	public void visit(DexInstruction_MoveWide instruction) {
		doMove(instruction.getRegTo1(), instruction.getRegFrom1());
	}


	private void doMoveResult(DexRegister to) {
		// opMoveResultPseudo if afterNonInvokeThrowingInsn
		//TODO: Could it be a TryStart or other stuff?
		boolean hasInvoke = curInst.getPredecessors().get(0).getInstruction() instanceof DexInstruction_Invoke;
		for(AnalyzedDexInstruction i : curInst.getPredecessors()) {
			assert hasInvoke == (i.getInstruction() instanceof DexInstruction_Invoke);
		}

		RegisterSpec dst = getPostRegSpec(to);
		if (hasInvoke)
			doPlainInsn(Rops.opMoveResult(dst), dst);
		else 
			doPlainInsn(Rops.opMoveResultPseudo(dst), dst);
	}
	
	@Override
	public void visit(DexInstruction_MoveResult instruction) {
		doMoveResult(instruction.getRegTo());
	}


	@Override
	public void visit(DexInstruction_MoveResultWide instruction) {
		doMoveResult(instruction.getRegTo1());
	}


	@Override
	public void visit(DexInstruction_MoveException instruction) {
		RegisterSpec dst = getPostRegSpec(instruction.getRegTo());
		doPlainInsn(Rops.opMoveException(dst), dst);
	}


	@Override
	public void visit(DexInstruction_ReturnVoid instruction) {
		doPlainInsn(Rops.RETURN_VOID, null);
	}


	private void doReturn(DexRegister from) {
		doPlainInsn(Rops.opReturn(getPreRegSpec(from)), null, from);
	}
	
	@Override
	public void visit(DexInstruction_Return instruction) {
		doReturn(instruction.getRegFrom());		
	}


	@Override
	public void visit(DexInstruction_ReturnWide instruction) {
		doReturn(instruction.getRegFrom1());		
	}

	
	private void doConst(DexRegister to, Constant constant) {
		RegisterSpec dst = getPostRegSpec(to);
		doPlainCstInsn(Rops.opConst(dst), dst, constant);
		
	}
	
	@Override
	public void visit(DexInstruction_Const instruction) {
		doConst(instruction.getRegTo(), makeCstLiteral(instruction.getValue()));
	}


	@Override
	public void visit(DexInstruction_ConstWide instruction) {
		doConst(instruction.getRegTo1(), makeCstLiteral(instruction.getValue()));
	}


	@Override
	public void visit(DexInstruction_ConstString instruction) {
		doConst(instruction.getRegTo(), makeCstString(instruction.getStringConstant()));
	}


	@Override
	public void visit(DexInstruction_ConstClass instruction) {
		doConst(instruction.getRegTo(), makeCstType(instruction.getValue()));
	}


	@Override
	public void visit(DexInstruction_Monitor instruction) {
		doThrowingInsn(instruction.isEnter() ? Rops.MONITOR_ENTER : Rops.MONITOR_EXIT, instruction.getRegMonitor());
	}


	@Override
	public void visit(DexInstruction_CheckCast instruction) {
		doThrowingCstInsn(Rops.CHECK_CAST, makeCstType(instruction.getValue()), instruction.getRegObject());
	}


	@Override
	public void visit(DexInstruction_InstanceOf instruction) {
		doThrowingCstInsn(Rops.INSTANCE_OF, makeCstType(instruction.getValue()), instruction.getRegObject());
		doPseudoMoveResult(instruction.getRegTo());
	}


	@Override
	public void visit(DexInstruction_ArrayLength instruction) {
		doThrowingInsn(Rops.ARRAY_LENGTH, instruction.getRegArray());
		doPseudoMoveResult(instruction.getRegTo());
	}


	@Override
	public void visit(DexInstruction_NewInstance instruction) {
		doThrowingCstInsn(Rops.NEW_INSTANCE, makeCstType(instruction.getValue()));
		doPseudoMoveResult(instruction.getRegTo());
	}


	@Override
	public void visit(DexInstruction_NewArray instruction) {
		DexArrayType arrayType = instruction.getValue();
		doThrowingCstInsn(Rops.opNewArray(Type.intern(arrayType.getElementType().getDescriptor())), 
				makeCstType(arrayType), instruction.getRegSize());
		doPseudoMoveResult(instruction.getRegTo());
	}


	@Override
	public void visit(DexInstruction_FilledNewArray instruction) {
		DexArrayType arrayType = instruction.getArrayType();
		Type elementType = Type.intern(arrayType.getElementType().getDescriptor());
		int arrayLen = instruction.getArgumentRegisters().size();
		
		DexRegister tmp0 = DexRegisterHelper.getTempRegister(0);
		RegisterSpec tmp0Spec = RegisterSpec.make(tmp0.getOriginalIndex(), Type.INT);
		
		// add Instruction manually because type information is not available in analyzer
		result.addInstruction(new PlainCstInsn(Rops.CONST_INT,
				SourcePosition.NO_INFO, 
				tmp0Spec,
				RegisterSpecList.EMPTY, 
				makeCstLiteral(arrayLen)));
		result.addInstruction(new ThrowingCstInsn(Rops.opNewArray(Type.intern(arrayType.getElementType().getDescriptor())),
				SourcePosition.NO_INFO, 
				RegisterSpecList.make(tmp0Spec), 
				getCatches(), 
				makeCstType(arrayType)));
		
		// add array assignments to primary successor basic blocks
		AnalyzedDexInstruction primSuccessor = curInst.getSuccesors().get(0); 
		DexRegister dstReg = primSuccessor.getDestinationRegister();
		RegisterSpec dstRegSpec = RegisterSpec.make(dstReg.getOriginalIndex(), Type.intern(arrayType.getDescriptor()));
		Rop opcode = Rops.opAput(Type.intern(elementType.getDescriptor()));
		
		for(int i=0; i<arrayLen; i++) {
			result.addAuxInstruction(new PlainCstInsn(Rops.CONST_INT,
					SourcePosition.NO_INFO, 
					tmp0Spec,
					RegisterSpecList.EMPTY, 
					makeCstLiteral(i)));
			result.addAuxInstruction(new ThrowingInsn(opcode, 
					SourcePosition.NO_INFO, 
					RegisterSpecList.make(
							RegisterSpec.make(instruction.getArgumentRegisters().get(i).getOriginalIndex(), elementType), 
							dstRegSpec, 
							tmp0Spec),
					getCatches()));
		}
	}


	@Override
	public void visit(DexInstruction_FillArray instruction) {
		AnalyzedDexInstruction arrayDataPtr = analyzer.reverseLookup(instruction.getArrayTable());
		assert arrayDataPtr.getSuccessorCount() == 1;
		DexInstruction_FillArrayData arrayData = (DexInstruction_FillArrayData) arrayDataPtr.getSuccesors().get(0).getInstruction();
		
		Type arrayElementType = getPostRegSpec(instruction.getRegArray()).getType().getComponentType();
		ArrayList<Constant> values = new ArrayList<Constant>();
		for(byte[] element : arrayData.getElementData()) {
			long v = 0;
			for(int i=0; i<element.length; i++)
				v |= (((long)element[i]) << i);

			if (arrayElementType == Type.BYTE) {
	        	assert element.length == 1;
	        	values.add(CstByte.make((int)v));
	        } else if (arrayElementType == Type.BOOLEAN) {
	        	assert element.length == 1;
	        	values.add(CstBoolean.make((int)v));
	        } else if (arrayElementType == Type.SHORT) {
	        	assert element.length == 2;
	        	values.add(CstShort.make((int)v));
	        } else if (arrayElementType == Type.CHAR) {
	        	assert element.length == 2;
	        	values.add(CstChar.make((int)v));
	        } else if (arrayElementType == Type.INT) {
	        	assert element.length == 4;
	        	values.add(CstInteger.make((int)v));
	        } else if (arrayElementType == Type.FLOAT) {
	        	assert element.length == 4;
	        	values.add(CstFloat.make((int)v));
	        } else if (arrayElementType == Type.LONG) {
	        	assert element.length == 8;
	        	values.add(CstLong.make(v));
	        } else if (arrayElementType == Type.DOUBLE) {
	        	assert element.length == 8;
	        	values.add(CstDouble.make(v));
	        } else {
	            throw new IllegalArgumentException("Unexpected constant type");
	        }
		}
		
		result.addInstruction(new FillArrayDataInsn(Rops.FILL_ARRAY_DATA, SourcePosition.NO_INFO,
				RegisterSpecList.EMPTY, values, CstType.intern(arrayElementType)));
	}


	@Override
	public void visit(DexInstruction_FillArrayData instruction) {
		assert false;
	}


	@Override
	public void visit(DexInstruction_Throw instruction) {
		doThrowingInsn(Rops.THROW, instruction.getRegFrom());
	}


	@Override
	public void visit(DexInstruction_Goto instruction) {
		doPlainInsn(Rops.GOTO, null);
	}


	@Override
	public void visit(DexInstruction_Switch instruction) {
		IntList caseList = new IntList();
		List<DexLabel> targetList;
		
		AnalyzedDexInstruction switchDataPtr = analyzer.reverseLookup(instruction.getSwitchTable());
		assert switchDataPtr.getSuccessorCount() == 1;
		DexInstruction switchDataRaw = switchDataPtr.getSuccesors().get(0).getInstruction();
		
		if (instruction.isPacked()) {
			DexInstruction_PackedSwitchData switchData = (DexInstruction_PackedSwitchData) switchDataRaw;
			for(int i=0; i < switchData.getTargets().size(); i++)
				caseList.add(switchData.getFirstKey() + i);
			targetList = switchData.getTargets();
		} else {
			targetList = new ArrayList<DexLabel>();
			DexInstruction_SparseSwitchData switchData = (DexInstruction_SparseSwitchData) switchDataRaw;
			for(Pair<Integer, DexLabel> pair : switchData.getKeyTargetPairs()) {
				caseList.add(pair.getValA());
				targetList.add(pair.getValB());
			}
		}
		
		// Sanity check
		assert targetList.size() + 1== curInst.getSuccessorCount();
		for(int i=0; i < targetList.size(); i++)
			assert targetList.get(i) == curInst.getSuccesors().get(i + 1).auxillaryElement;
		
		result.addInstruction(new SwitchInsn(Rops.SWITCH, SourcePosition.NO_INFO, null, makeOperands(instruction.getRegTest()), caseList));
	}


	@Override
	public void visit(DexInstruction_PackedSwitchData instruction) {
		assert false;
	}


	@Override
	public void visit(DexInstruction_SparseSwitchData instruction) {
		assert false;
	}


	@Override
	public void visit(DexInstruction_CompareFloat instruction) {
		doPlainInsn(instruction.isLtBias() ? Rops.CMPL_FLOAT : Rops.CMPG_FLOAT, 
				getPostRegSpec(instruction.getRegTo()), instruction.getRegSourceA(), instruction.getRegSourceB());
	}


	@Override
	public void visit(DexInstruction_CompareWide instruction) {
		Rop opcode = null;;
		switch (instruction.getInsnOpcode()) {
		case CmpLong:
			opcode = Rops.CMPL_LONG;
			break;
		case CmpgDouble:
			opcode = Rops.CMPG_DOUBLE;
			break;
		case CmplDouble:
			opcode = Rops.CMPL_DOUBLE;
			break;
		
		}
		doPlainInsn(opcode, getPostRegSpec(instruction.getRegTo()), instruction.getRegSourceA1(), instruction.getRegSourceB1());
	}


	@Override
	public void visit(DexInstruction_IfTest instruction) {
		RegisterSpecList operands = makeOperands(instruction.getRegA(), instruction.getRegB());
		Rop opcode = null;
		switch(instruction.getInsnOpcode()) {
		case eq:
			opcode = Rops.opIfEq(operands);
			break;
		case ge:
			opcode = Rops.opIfGe(operands);
			break;
		case gt:
			opcode = Rops.opIfGt(operands);
			break;
		case le:
			opcode = Rops.opIfLe(operands);
			break;
		case lt:
			opcode = Rops.opIfLt(operands);
			break;
		case ne:
			opcode = Rops.opIfNe(operands);
			break;
		}
		doPlainInsn(opcode, null, instruction.getRegA(), instruction.getRegB());
	}


	@Override
	public void visit(DexInstruction_IfTestZero instruction) {
		RegisterSpecList operands = makeOperands(instruction.getReg());
		Rop opcode = null;
		switch(instruction.getInsnOpcode()) {
		case eqz:
			opcode = Rops.opIfEq(operands);
			break;
		case gez:
			opcode = Rops.opIfGe(operands);
			break;
		case gtz:
			opcode = Rops.opIfGt(operands);
			break;
		case lez:
			opcode = Rops.opIfLe(operands);
			break;
		case ltz:
			opcode = Rops.opIfLt(operands);
			break;
		case nez:
			opcode = Rops.opIfNe(operands);
			break;
		}
		doPlainInsn(opcode, null, instruction.getReg());
	}


	private void doAget(DexRegister to, DexRegister array, DexRegister index) {
		doThrowingInsn(Rops.opAget(getPostRegSpec(to)), array, index);
		doPseudoMoveResult(to);
	}
	@Override
	public void visit(DexInstruction_ArrayGet instruction) {
		doAget(instruction.getRegTo(), instruction.getRegArray(), instruction.getRegIndex());
	}


	@Override
	public void visit(DexInstruction_ArrayGetWide instruction) {
		doAget(instruction.getRegTo1(), instruction.getRegArray(), instruction.getRegIndex());
	}


	private void doAput(DexRegister src, DexRegister array, DexRegister index) {
		doThrowingInsn(Rops.opAput(getPreRegSpec(src)), src, array, index);
	}
	@Override
	public void visit(DexInstruction_ArrayPut instruction) {
		doAput(instruction.getRegFrom(), instruction.getRegArray(), instruction.getRegIndex());
	}


	@Override
	public void visit(DexInstruction_ArrayPutWide instruction) {
		doAput(instruction.getRegFrom1(), instruction.getRegArray(), instruction.getRegIndex());
	}

	private void doIget(DexRegister to, DexRegister object, DexClassType fieldClass, DexRegisterType fieldType, String fieldName) {
		RegisterSpec dst = getPostRegSpec(to);
		Constant fieldRef = makeFieldRef(fieldClass, fieldType, fieldName);
		doThrowingCstInsn(Rops.opGetField(dst), fieldRef, object);
		doPseudoMoveResult(to);
	}
	
	@Override
	public void visit(DexInstruction_InstanceGet instruction) {
		doIget(instruction.getRegTo(), instruction.getRegObject(), 
				instruction.getFieldClass(), instruction.getFieldType(), instruction.getFieldName());
	}


	@Override
	public void visit(DexInstruction_InstanceGetWide instruction) {
		doIget(instruction.getRegTo1(), instruction.getRegObject(), 
				instruction.getFieldClass(), instruction.getFieldType(), instruction.getFieldName());
	}


	private void doIput(DexRegister from, DexRegister object, DexClassType fieldClass, DexRegisterType fieldType, String fieldName) {
		RegisterSpec src = getPreRegSpec(from);
		Constant fieldRef = makeFieldRef(fieldClass, fieldType, fieldName);
		doThrowingCstInsn(Rops.opPutField(src), fieldRef, from, object);
	}
	@Override
	public void visit(DexInstruction_InstancePut instruction) {
		doIput(instruction.getRegFrom(), instruction.getRegObject(), 
				instruction.getFieldClass(), instruction.getFieldType(), instruction.getFieldName());
	}


	@Override
	public void visit(DexInstruction_InstancePutWide instruction) {
		doIput(instruction.getRegFrom1(), instruction.getRegObject(), 
				instruction.getFieldClass(), instruction.getFieldType(), instruction.getFieldName());
	}


	private void doSget(DexRegister to,  DexClassType fieldClass, DexRegisterType fieldType, String fieldName) {
		RegisterSpec dst = getPostRegSpec(to);
		Constant fieldRef = makeFieldRef(fieldClass, fieldType, fieldName);
		doThrowingCstInsn(Rops.opGetStatic(dst), fieldRef);
		doPseudoMoveResult(to);
	}
	@Override
	public void visit(DexInstruction_StaticGet instruction) {
		doSget(instruction.getRegTo(), 
				instruction.getFieldClass(), instruction.getFieldType(), instruction.getFieldName());
	}


	@Override
	public void visit(DexInstruction_StaticGetWide instruction) {
		doSget(instruction.getRegTo1(), 
				instruction.getFieldClass(), instruction.getFieldType(), instruction.getFieldName());
	}

	
	private void doSput(DexRegister from, DexClassType fieldClass, DexRegisterType fieldType, String fieldName) {
		RegisterSpec src = getPreRegSpec(from);
		Constant fieldRef = makeFieldRef(fieldClass, fieldType, fieldName);
		doThrowingCstInsn(Rops.opPutStatic(src), fieldRef, from);
	}
	@Override
	public void visit(DexInstruction_StaticPut instruction) {
		doSput(instruction.getRegFrom(), 
				instruction.getFieldClass(), instruction.getFieldType(), instruction.getFieldName());
	}


	@Override
	public void visit(DexInstruction_StaticPutWide instruction) {
		doSput(instruction.getRegFrom1(), 
				instruction.getFieldClass(), instruction.getFieldType(), instruction.getFieldName());
	}


	@Override
	public void visit(DexInstruction_Invoke instruction) {
		Rop opcode = null;
		DexRegister[] operands_array = (DexRegister[]) instruction.getArgumentRegisters().toArray();
		RegisterSpecList operands = makeOperands(operands_array);
		switch(instruction.getCallType()) {
		case Direct:
			opcode = new Rop(RegOps.INVOKE_DIRECT, operands, StdTypeList.THROWABLE);
			break;
		case Interface:
			opcode = new Rop(RegOps.INVOKE_INTERFACE, operands, StdTypeList.THROWABLE);
			break;
		case Static:
			opcode = new Rop(RegOps.INVOKE_STATIC, operands, StdTypeList.THROWABLE);
			break;
		case Super:
			opcode = new Rop(RegOps.INVOKE_SUPER, operands, StdTypeList.THROWABLE);
			break;
		case Virtual:
			opcode = new Rop(RegOps.INVOKE_VIRTUAL, operands, StdTypeList.THROWABLE);
			break;
		}
		doThrowingCstInsn(opcode, 
				makeMethodRef(instruction.getClassType(), instruction.getMethodName(), instruction.getMethodPrototype(), instruction.getCallType() == Opcode_Invoke.Interface), 
				operands_array);
	}


	@Override
	public void visit(DexInstruction_UnaryOp instruction) {
		Rop opcode = null;
		switch( instruction.getInsnOpcode()) {
		case NegFloat:
			opcode = Rops.NEG_FLOAT;
			break;
		case NegInt:
			opcode = Rops.NEG_INT;
			break;
		case NotInt:
			opcode = Rops.NOT_INT;
			break;
		}
		doPlainInsn(opcode, getPostRegSpec(instruction.getRegTo()), instruction.getRegFrom());
	}


	@Override
	public void visit(DexInstruction_UnaryOpWide instruction) {
		Rop opcode = null;
		switch( instruction.getInsnOpcode()) {
		case NegDouble:
			opcode = Rops.NEG_DOUBLE;
			break;
		case NegLong:
			opcode = Rops.NEG_LONG;
			break;
		case NotLong:
			opcode = Rops.NOT_LONG;
			break;
		}
		doPlainInsn(opcode, getPostRegSpec(instruction.getRegTo1()), instruction.getRegFrom1());
	}


	private void doConvert(DexRegister dst, DexRegister src) {
		doPlainInsn(Rops.opConv(getPostRegSpec(dst), getPreRegSpec(src)), getPostRegSpec(dst), src);
	}
	
	@Override
	public void visit(DexInstruction_Convert instruction) {
		doConvert(instruction.getRegTo(), instruction.getRegFrom());
	}


	@Override
	public void visit(DexInstruction_ConvertWide instruction) {
		doConvert(instruction.getRegTo1(), instruction.getRegFrom1());
	}


	@Override
	public void visit(DexInstruction_ConvertFromWide instruction) {
		doConvert(instruction.getRegTo(), instruction.getRegFrom1());
	}


	@Override
	public void visit(DexInstruction_ConvertToWide instruction) {
		doConvert(instruction.getRegTo1(), instruction.getRegFrom());
	}


	@Override
	public void visit(DexInstruction_BinaryOp instruction) {
		Rop opcode = null;
		switch(instruction.getInsnOpcode()) {
		case AddFloat:
			opcode = Rops.ADD_FLOAT;
			break;
		case AddInt:
			opcode = Rops.ADD_INT;
			break;
		case AndInt:
			opcode = Rops.AND_INT;
			break;
		case DivFloat:
			opcode = Rops.DIV_FLOAT;
			break;
		case DivInt:
			opcode = Rops.DIV_INT;
			break;
		case MulFloat:
			opcode = Rops.MUL_FLOAT;
			break;
		case MulInt:
			opcode = Rops.MUL_INT;
			break;
		case OrInt:
			opcode = Rops.OR_INT;
			break;
		case RemFloat:
			opcode = Rops.REM_FLOAT;
			break;
		case RemInt:
			opcode = Rops.REM_INT;
			break;
		case ShlInt:
			opcode = Rops.SHL_INT;
			break;
		case ShrInt:
			opcode = Rops.SHR_INT;
			break;
		case SubFloat:
			opcode = Rops.SUB_FLOAT;
			break;
		case SubInt:
			opcode = Rops.SUB_INT;
			break;
		case UshrInt:
			opcode = Rops.USHR_INT;
			break;
		case XorInt:
			opcode = Rops.XOR_INT;
			break;
		}
		doPlainInsn(opcode, getPostRegSpec(instruction.getRegTarget()), instruction.getRegSourceA(), instruction.getRegSourceB());
	}


	@Override
	public void visit(DexInstruction_BinaryOpLiteral instruction) {
		Rop opcode = null;
		switch(instruction.getInsnOpcode()) {
		case Add:
			opcode = Rops.ADD_CONST_INT; // Rop allow CONST operation on long/double/float register, but DexInstruction does not.
			break;
		case And:
			opcode = Rops.AND_CONST_INT;
			break;
		case Div:
			opcode = Rops.DIV_CONST_INT;
			break;
		case Mul:
			opcode = Rops.MUL_CONST_INT;
			break;
		case Or:
			opcode = Rops.OR_CONST_INT;
			break;
		case Rem:
			opcode = Rops.REM_CONST_INT;
			break;
		case Rsub:
			opcode = Rops.SUB_CONST_INT; // Extra Insn to handle this
			break;
		case Shl:
			opcode = Rops.SHL_CONST_INT;
			break;
		case Shr:
			opcode = Rops.SHR_CONST_INT;
			break;
		case Ushr:
			opcode = Rops.USHR_CONST_INT;
			break;
		case Xor:
			opcode = Rops.XOR_CONST_INT;
			break;
		}
		doPlainCstInsn(opcode, getPostRegSpec(instruction.getRegTarget()), makeCstLiteral(instruction.getLiteral()), instruction.getRegSource());
		
		if (instruction.getInsnOpcode() == Opcode_BinaryOpLiteral.Rsub) // Patch up rsub
			doPlainInsn(Rops.NEG_INT, getPostRegSpec(instruction.getRegTarget()), instruction.getRegTarget());
	}


	@Override
	public void visit(DexInstruction_BinaryOpWide instruction) {
		Rop opcode = null;
		switch(instruction.getInsnOpcode()) {
		case AddDouble:
			opcode = Rops.ADD_DOUBLE;
			break;
		case AddLong:
			opcode = Rops.ADD_LONG;
			break;
		case AndLong:
			opcode = Rops.AND_LONG;
			break;
		case DivDouble:
			opcode = Rops.DIV_DOUBLE;
			break;
		case DivLong:
			opcode = Rops.DIV_LONG;
			break;
		case MulDouble:
			opcode = Rops.MUL_DOUBLE;
			break;
		case MulLong:
			opcode = Rops.MUL_LONG;
			break;
		case OrLong:
			opcode = Rops.OR_LONG;
			break;
		case RemDouble:
			opcode = Rops.REM_DOUBLE;
			break;
		case RemLong:
			opcode = Rops.REM_LONG;
			break;
		case ShlLong:
			opcode = Rops.SHL_LONG;
			break;
		case ShrLong:
			opcode = Rops.SHR_LONG;
			break;
		case SubDouble:
			opcode = Rops.SUB_DOUBLE;
			break;
		case SubLong:
			opcode = Rops.SUB_LONG;
			break;
		case UshrLong:
			opcode = Rops.USHR_LONG;
			break;
		case XorLong:
			opcode = Rops.XOR_LONG;
			break;
		}
		doPlainInsn(opcode, getPostRegSpec(instruction.getRegTarget1()), instruction.getRegSourceA1(), instruction.getRegSourceB1());
	}


	@Override
	public void visit(DexInstruction_Unknown instruction) {
		// should not happen
		assert false;
	}


	@Override
	public void visit(DexPseudoinstruction_FilledNewArray instruction) {
		// Pseudo instruction should have already been unwrapped.
		assert false;
	}


	@Override
	public void visit(DexPseudoinstruction_GetInternalClassAnnotation instruction) {
		// Pseudo instruction should have already been unwrapped.
		assert false;
	}


	@Override
	public void visit(DexPseudoinstruction_GetInternalMethodAnnotation instruction) {
		// Pseudo instruction should have already been unwrapped.
		assert false;
	}


	@Override
	public void visit(DexPseudoinstruction_GetMethodCaller instruction) {
		// Pseudo instruction should have already been unwrapped.
		assert false;
	}


	@Override
	public void visit(DexPseudoinstruction_GetObjectTaint instruction) {
		// Pseudo instruction should have already been unwrapped.
		assert false;
	}


	@Override
	public void visit(DexPseudoinstruction_GetQueryTaint instruction) {
		// Pseudo instruction should have already been unwrapped.
		assert false;
	}


	@Override
	public void visit(DexPseudoinstruction_GetServiceTaint instruction) {
		// Pseudo instruction should have already been unwrapped.
		assert false;
	}


	@Override
	public void visit(DexPseudoinstruction_PrintInteger instruction) {
		// Pseudo instruction should have already been unwrapped.
		assert false;
	}


	@Override
	public void visit(DexPseudoinstruction_PrintIntegerConst instruction) {
		// Pseudo instruction should have already been unwrapped.
		assert false;
	}


	@Override
	public void visit(DexPseudoinstruction_PrintString instruction) {
		// Pseudo instruction should have already been unwrapped.
		assert false;
	}


	@Override
	public void visit(DexPseudoinstruction_PrintStringConst instruction) {
		// Pseudo instruction should have already been unwrapped.
		assert false;
	}


	@Override
	public void visit(DexPseudoinstruction_SetObjectTaint instruction) {
		// Pseudo instruction should have already been unwrapped.
		assert false;
	}


	@Override
	public void visit(DexPseudoinstruction_Invoke instruction) {
		// Pseudo instruction should have already been unwrapped.
		assert false;
	}
}