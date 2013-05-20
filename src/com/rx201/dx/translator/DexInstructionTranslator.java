package com.rx201.dx.translator;

import java.util.ArrayList;

import org.jf.dexlib.Code.Analysis.AnalyzedInstruction;

import com.android.dx.rop.code.Insn;

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
		
		inst.getInstruction().accept(this);
		
		return result;
	}


	@Override
	public void visit(DexInstruction_Nop dexInstruction_Nop) {
		return;
	}


	@Override
	public void visit(DexInstruction_Move dexInstruction_Move) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(DexInstruction_MoveWide dexInstruction_MoveWide) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(DexInstruction_MoveResult dexInstruction_MoveResult) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(
			DexInstruction_MoveResultWide dexInstruction_MoveResultWide) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(DexInstruction_MoveException dexInstruction_MoveException) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(DexInstruction_ReturnVoid dexInstruction_ReturnVoid) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(DexInstruction_Return dexInstruction_Return) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(DexInstruction_ReturnWide dexInstruction_ReturnWide) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(DexInstruction_Const dexInstruction_Const) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(DexInstruction_ConstWide dexInstruction_ConstWide) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(DexInstruction_ConstString dexInstruction_ConstString) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(DexInstruction_ConstClass dexInstruction_ConstClass) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(DexInstruction_Monitor dexInstruction_Monitor) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(DexInstruction_CheckCast dexInstruction_CheckCast) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(DexInstruction_InstanceOf dexInstruction_InstanceOf) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(DexInstruction_ArrayLength dexInstruction_ArrayLength) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(DexInstruction_NewInstance dexInstruction_NewInstance) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(DexInstruction_NewArray dexInstruction_NewArray) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(
			DexInstruction_FilledNewArray dexInstruction_FilledNewArray) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(DexInstruction_FillArray dexInstruction_FillArray) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(DexInstruction_FillArrayData dexInstruction_FillArrayData) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(DexInstruction_Throw dexInstruction_Throw) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(DexInstruction_Goto dexInstruction_Goto) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(DexInstruction_Switch dexInstruction_Switch) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(
			DexInstruction_PackedSwitchData dexInstruction_PackedSwitchData) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(
			DexInstruction_SparseSwitchData dexInstruction_SparseSwitchData) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(DexInstruction_CompareFloat dexInstruction_CompareFloat) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(DexInstruction_CompareWide dexInstruction_CompareWide) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(DexInstruction_IfTest dexInstruction_IfTest) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(DexInstruction_IfTestZero dexInstruction_IfTestZero) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(DexInstruction_ArrayGet dexInstruction_ArrayGet) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(DexInstruction_ArrayGetWide dexInstruction_ArrayGetWide) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(DexInstruction_ArrayPut dexInstruction_ArrayPut) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(DexInstruction_ArrayPutWide dexInstruction_ArrayPutWide) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(DexInstruction_InstanceGet dexInstruction_InstanceGet) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(
			DexInstruction_InstanceGetWide dexInstruction_InstanceGetWide) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(DexInstruction_InstancePut dexInstruction_InstancePut) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(
			DexInstruction_InstancePutWide dexInstruction_InstancePutWide) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(DexInstruction_StaticGet dexInstruction_StaticGet) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(DexInstruction_StaticGetWide dexInstruction_StaticGetWide) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(DexInstruction_StaticPut dexInstruction_StaticPut) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(DexInstruction_StaticPutWide dexInstruction_StaticPutWide) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(DexInstruction_Invoke dexInstruction_Invoke) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(DexInstruction_UnaryOp dexInstruction_UnaryOp) {
		// TODO Auto-generated method stub
		
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
	public void visit(DexInstruction_ConvertToWide dexInstruction_ConvertToWide) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(DexInstruction_BinaryOp dexInstruction_BinaryOp) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(
			DexInstruction_BinaryOpLiteral dexInstruction_BinaryOpLiteral) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(DexInstruction_BinaryOpWide dexInstruction_BinaryOpWide) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(DexInstruction_Unknown dexInstruction_Unknown) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(
			DexPseudoinstruction_FilledNewArray dexPseudoinstruction_FilledNewArray) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(
			DexPseudoinstruction_GetInternalClassAnnotation dexPseudoinstruction_GetInternalClassAnnotation) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(
			DexPseudoinstruction_GetInternalMethodAnnotation dexPseudoinstruction_GetInternalMethodAnnotation) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(
			DexPseudoinstruction_GetMethodCaller dexPseudoinstruction_GetMethodCaller) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(
			DexPseudoinstruction_GetObjectTaint dexPseudoinstruction_GetObjectTaint) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(
			DexPseudoinstruction_GetQueryTaint dexPseudoinstruction_GetQueryTaint) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(
			DexPseudoinstruction_GetServiceTaint dexPseudoinstruction_GetServiceTaint) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(
			DexPseudoinstruction_PrintInteger dexPseudoinstruction_PrintInteger) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(
			DexPseudoinstruction_PrintIntegerConst dexPseudoinstruction_PrintIntegerConst) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(
			DexPseudoinstruction_PrintString dexPseudoinstruction_PrintString) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(
			DexPseudoinstruction_PrintStringConst dexPseudoinstruction_PrintStringConst) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(
			DexPseudoinstruction_SetObjectTaint dexPseudoinstruction_SetObjectTaint) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void visit(DexPseudoinstruction_Invoke dexPseudoinstruction_Invoke) {
		// TODO Auto-generated method stub
		
	}
}