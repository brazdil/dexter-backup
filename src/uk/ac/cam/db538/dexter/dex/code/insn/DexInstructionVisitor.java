package uk.ac.cam.db538.dexter.dex.code.insn;

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

public interface DexInstructionVisitor {

	void visit(DexInstruction_Nop instruction);

	void visit(DexInstruction_Move instruction);

	void visit(DexInstruction_MoveWide instruction);

	void visit(DexInstruction_MoveResult instruction);

	void visit(DexInstruction_MoveResultWide instruction);

	void visit(DexInstruction_MoveException instruction);

	void visit(DexInstruction_ReturnVoid instruction);

	void visit(DexInstruction_Return instruction);

	void visit(DexInstruction_ReturnWide instruction);

	void visit(DexInstruction_Const instruction);

	void visit(DexInstruction_ConstWide instruction);

	void visit(DexInstruction_ConstString instruction);

	void visit(DexInstruction_ConstClass instruction);

	void visit(DexInstruction_Monitor instruction);

	void visit(DexInstruction_CheckCast instruction);

	void visit(DexInstruction_InstanceOf instruction);

	void visit(DexInstruction_ArrayLength instruction);

	void visit(DexInstruction_NewInstance instruction);

	void visit(DexInstruction_NewArray instruction);

	void visit(DexInstruction_FilledNewArray instruction);

	void visit(DexInstruction_FillArrayData instruction);

	void visit(DexInstruction_FillArrayData instruction);

	void visit(DexInstruction_Throw instruction);

	void visit(DexInstruction_Goto instruction);

	void visit(DexInstruction_Switch instruction);

	void visit(DexInstruction_PackedSwitchData instruction);

	void visit(DexInstruction_SparseSwitchData instruction);

	void visit(DexInstruction_Compare instruction);

	void visit(DexInstruction_CompareWide instruction);

	void visit(DexInstruction_IfTest instruction);

	void visit(DexInstruction_IfTestZero instruction);

	void visit(DexInstruction_ArrayGet instruction);

	void visit(DexInstruction_ArrayGetWide instruction);

	void visit(DexInstruction_ArrayPut instruction);

	void visit(DexInstruction_ArrayPutWide instruction);

	void visit(DexInstruction_InstanceGet instruction);

	void visit(DexInstruction_InstanceGetWide instruction);

	void visit(DexInstruction_InstancePut instruction);

	void visit(DexInstruction_InstancePutWide instruction);

	void visit(DexInstruction_StaticGet instruction);

	void visit(DexInstruction_StaticGetWide instruction);

	void visit(DexInstruction_StaticPut instruction);

	void visit(DexInstruction_StaticPutWide instruction);

	void visit(DexInstruction_Invoke instruction);

	void visit(DexInstruction_UnaryOp instruction);

	void visit(DexInstruction_UnaryOpWide instruction);

	void visit(DexInstruction_Convert instruction);

	void visit(DexInstruction_ConvertWide instruction);

	void visit(DexInstruction_ConvertFromWide instruction);

	void visit(DexInstruction_ConvertToWide instruction);

	void visit(DexInstruction_BinaryOp instruction);

	void visit(DexInstruction_BinaryOpLiteral instruction);

	void visit(DexInstruction_BinaryOpWide instruction);

	void visit(DexInstruction_Unknown instruction);

	void visit(DexMacro_FilledNewArray instruction);

	void visit(DexMacro_GetInternalClassAnnotation instruction);

	void visit(DexMacro_GetInternalMethodAnnotation instruction);

	void visit(DexMacro_GetMethodCaller instruction);

	void visit(DexMacro_GetObjectTaint instruction);

	void visit(DexMacro_GetQueryTaint instruction);

	void visit(DexMacro_GetServiceTaint instruction);

	void visit(DexMacro_PrintInteger instruction);

	void visit(DexMacro_PrintIntegerConst instruction);

	void visit(DexMacro_PrintString instruction);

	void visit(DexMacro_PrintStringConst instruction);

	void visit(DexMacro_SetObjectTaint instruction);

    void visit(DexPseudoinstruction_Invoke instruction);

}
