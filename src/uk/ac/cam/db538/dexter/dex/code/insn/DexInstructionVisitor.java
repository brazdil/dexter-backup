package uk.ac.cam.db538.dexter.dex.code.insn;

public interface DexInstructionVisitor {

	void visit(DexInstruction_Nop dexInstruction_Nop);

	void visit(DexInstruction_Move dexInstruction_Move);

	void visit(DexInstruction_MoveWide dexInstruction_MoveWide);

	void visit(DexInstruction_MoveResult dexInstruction_MoveResult);

	void visit(DexInstruction_MoveResultWide dexInstruction_MoveResultWide);

	void visit(DexInstruction_MoveException dexInstruction_MoveException);

	void visit(DexInstruction_ReturnVoid dexInstruction_ReturnVoid);

	void visit(DexInstruction_Return dexInstruction_Return);

	void visit(DexInstruction_ReturnWide dexInstruction_ReturnWide);

	void visit(DexInstruction_Const dexInstruction_Const);

	void visit(DexInstruction_ConstWide dexInstruction_ConstWide);

	void visit(DexInstruction_ConstString dexInstruction_ConstString);

	void visit(DexInstruction_ConstClass dexInstruction_ConstClass);

	void visit(DexInstruction_Monitor dexInstruction_Monitor);

	void visit(DexInstruction_CheckCast dexInstruction_CheckCast);

	void visit(DexInstruction_InstanceOf dexInstruction_InstanceOf);

	void visit(DexInstruction_ArrayLength dexInstruction_ArrayLength);

	void visit(DexInstruction_NewInstance dexInstruction_NewInstance);

	void visit(DexInstruction_NewArray dexInstruction_NewArray);

	void visit(DexInstruction_FilledNewArray dexInstruction_FilledNewArray);

	void visit(DexInstruction_FillArray dexInstruction_FillArray);

	void visit(DexInstruction_FillArrayData dexInstruction_FillArrayData);

	void visit(DexInstruction_Throw dexInstruction_Throw);

	void visit(DexInstruction_Goto dexInstruction_Goto);

	void visit(DexInstruction_Switch dexInstruction_Switch);

	void visit(DexInstruction_PackedSwitchData dexInstruction_PackedSwitchData);

	void visit(DexInstruction_SparseSwitchData dexInstruction_SparseSwitchData);

	void visit(DexInstruction_CompareFloat dexInstruction_CompareFloat);

	void visit(DexInstruction_CompareWide dexInstruction_CompareWide);

	void visit(DexInstruction_IfTest dexInstruction_IfTest);

	void visit(DexInstruction_IfTestZero dexInstruction_IfTestZero);

	void visit(DexInstruction_ArrayGet dexInstruction_ArrayGet);

	void visit(DexInstruction_ArrayGetWide dexInstruction_ArrayGetWide);

	void visit(DexInstruction_ArrayPut dexInstruction_ArrayPut);

	void visit(DexInstruction_ArrayPutWide dexInstruction_ArrayPutWide);

	void visit(DexInstruction_InstanceGet dexInstruction_InstanceGet);

	void visit(DexInstruction_InstanceGetWide dexInstruction_InstanceGetWide);

	void visit(DexInstruction_InstancePut dexInstruction_InstancePut);

	void visit(DexInstruction_InstancePutWide dexInstruction_InstancePutWide);

	void visit(DexInstruction_StaticGet dexInstruction_StaticGet);

	void visit(DexInstruction_StaticGetWide dexInstruction_StaticGetWide);

	void visit(DexInstruction_StaticPut dexInstruction_StaticPut);

	void visit(DexInstruction_StaticPutWide dexInstruction_StaticPutWide);

	void visit(DexInstruction_Invoke dexInstruction_Invoke);

	void visit(DexInstruction_UnaryOp dexInstruction_UnaryOp);

	void visit(DexInstruction_UnaryOpWide dexInstruction_UnaryOpWide);

	void visit(DexInstruction_Convert dexInstruction_Convert);

	void visit(DexInstruction_ConvertWide dexInstruction_ConvertWide);

	void visit(DexInstruction_ConvertFromWide dexInstruction_ConvertFromWide);

	void visit(DexInstruction_ConvertToWide dexInstruction_ConvertToWide);

	void visit(DexInstruction_BinaryOp dexInstruction_BinaryOp);

	void visit(DexInstruction_BinaryOpLiteral dexInstruction_BinaryOpLiteral);

	void visit(DexInstruction_BinaryOpWide dexInstruction_BinaryOpWide);

	void visit(DexInstruction_Unknown dexInstruction_Unknown);

}
