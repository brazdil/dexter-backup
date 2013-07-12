package uk.ac.cam.db538.dexter.dex.code;

import java.util.LinkedList;
import java.util.List;

import lombok.val;

import org.jf.dexlib.CodeItem;
import org.jf.dexlib.Code.Instruction;

import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ArrayLength;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_CheckCast;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Const;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ConstClass;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ConstString;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_InstanceOf;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Monitor;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Move;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_MoveException;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_MoveResult;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_NewArray;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_NewInstance;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Return;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ReturnVoid;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Unknown;
import uk.ac.cam.db538.dexter.dex.code.insn.InstructionParseError;
import uk.ac.cam.db538.dexter.hierarchy.RuntimeHierarchy;
import uk.ac.cam.db538.dexter.utils.InstructionList;
import uk.ac.cam.db538.dexter.utils.Pair;

public abstract class CodeParser {

	public static InstructionList parse(CodeItem codeItem, RuntimeHierarchy hierarchy) {
		val parserCache = new CodeParserState(hierarchy);
		
		val dexlibInstructions = codeItem.getInstructions();
		val parsedCode = new LinkedList<Pair<? extends DexCodeElement, Long>>();
		
		long insnOffset = 0L;
		for (val insn : dexlibInstructions) {
			parsedCode.add(
				Pair.create(parseInstruction(insn, parserCache), insnOffset));
			insnOffset += insn.getSize(0); // param ignored
		}
		
		return finalizeCode(parsedCode);
	}
	
	private static InstructionList finalizeCode(List<Pair<? extends DexCodeElement, Long>> insnsData) {
		val insnList = new InstructionList(insnsData.size());
		for (val insnData : insnsData)
			insnList.add(insnData.getValA());
		return insnList;
	}

	  private static DexInstruction parseInstruction(Instruction insn, CodeParserState parsingCache) throws InstructionParseError {
		    switch (insn.opcode) {

//		    case NOP:
//		      if (insn instanceof PackedSwitchDataPseudoInstruction)
//		        return new DexInstruction_PackedSwitchData(this, insn, parsingCache);
//		      else if (insn instanceof SparseSwitchDataPseudoInstruction)
//		        return new DexInstruction_SparseSwitchData(this, insn, parsingCache);
//		      else if (insn instanceof ArrayDataPseudoInstruction)
//		        return new DexInstruction_FillArrayData(this, insn, parsingCache);
//		      else
//		        return new DexInstruction_Nop(this, insn, parsingCache);

		    case MOVE:
		    case MOVE_OBJECT:
		    case MOVE_FROM16:
		    case MOVE_OBJECT_FROM16:
		    case MOVE_16:
		    case MOVE_OBJECT_16:
		    case MOVE_WIDE:
		    case MOVE_WIDE_FROM16:
		    case MOVE_WIDE_16:
		      return DexInstruction_Move.parse(insn, parsingCache);

		    case MOVE_RESULT:
		    case MOVE_RESULT_OBJECT:
		    case MOVE_RESULT_WIDE:
		      return DexInstruction_MoveResult.parse(insn, parsingCache);

		    case MOVE_EXCEPTION:
		      return DexInstruction_MoveException.parse(insn, parsingCache);

		    case RETURN_VOID:
		      return DexInstruction_ReturnVoid.parse(insn, parsingCache);

		    case RETURN:
		    case RETURN_OBJECT:
		    case RETURN_WIDE:
		      return DexInstruction_Return.parse(insn, parsingCache);

		    case CONST_4:
		    case CONST_16:
		    case CONST:
		    case CONST_HIGH16:
		    case CONST_WIDE_16:
		    case CONST_WIDE_32:
		    case CONST_WIDE:
		    case CONST_WIDE_HIGH16:
		      return DexInstruction_Const.parse(insn, parsingCache);

		    case CONST_STRING:
		    case CONST_STRING_JUMBO:
		      return DexInstruction_ConstString.parse(insn, parsingCache);

		    case CONST_CLASS:
		      return DexInstruction_ConstClass.parse(insn, parsingCache);

		    case MONITOR_ENTER:
		    case MONITOR_EXIT:
		      return DexInstruction_Monitor.parse(insn, parsingCache);

		    case CHECK_CAST:
		      return DexInstruction_CheckCast.parse(insn, parsingCache);

		    case INSTANCE_OF:
		      return DexInstruction_InstanceOf.parse(insn, parsingCache);

		    case NEW_INSTANCE:
		      return DexInstruction_NewInstance.parse(insn, parsingCache);

		    case NEW_ARRAY:
		      return DexInstruction_NewArray.parse(insn, parsingCache);

		    case ARRAY_LENGTH:
		      return DexInstruction_ArrayLength.parse(insn, parsingCache);

//		    case THROW:
//		      return new DexInstruction_Throw(this, insn, parsingCache);
//
//		    case GOTO:
//		    case GOTO_16:
//		    case GOTO_32:
//		      return new DexInstruction_Goto(this, insn, parsingCache);
//
//		    case IF_EQ:
//		    case IF_NE:
//		    case IF_LT:
//		    case IF_GE:
//		    case IF_GT:
//		    case IF_LE:
//		      return new DexInstruction_IfTest(this, insn, parsingCache);
//
//		    case IF_EQZ:
//		    case IF_NEZ:
//		    case IF_LTZ:
//		    case IF_GEZ:
//		    case IF_GTZ:
//		    case IF_LEZ:
//		      return new DexInstruction_IfTestZero(this, insn, parsingCache);
//
//		    case CMPL_FLOAT:
//		    case CMPG_FLOAT:
//		      return new DexInstruction_CompareFloat(this, insn, parsingCache);
//
//		    case CMPL_DOUBLE:
//		    case CMPG_DOUBLE:
//		    case CMP_LONG:
//		      return new DexInstruction_CompareWide(this, insn, parsingCache);
//
//		    case SGET:
//		    case SGET_OBJECT:
//		    case SGET_BOOLEAN:
//		    case SGET_BYTE:
//		    case SGET_CHAR:
//		    case SGET_SHORT:
//		      return new DexInstruction_StaticGet(this, insn, parsingCache);
//
//		    case SGET_WIDE:
//		      return new DexInstruction_StaticGetWide(this, insn, parsingCache);
//
//		    case SPUT:
//		    case SPUT_OBJECT:
//		    case SPUT_BOOLEAN:
//		    case SPUT_BYTE:
//		    case SPUT_CHAR:
//		    case SPUT_SHORT:
//		      return new DexInstruction_StaticPut(this, insn, parsingCache);
//
//		    case SPUT_WIDE:
//		      return new DexInstruction_StaticPutWide(this, insn, parsingCache);
//
//		    case IGET:
//		    case IGET_OBJECT:
//		    case IGET_BOOLEAN:
//		    case IGET_BYTE:
//		    case IGET_CHAR:
//		    case IGET_SHORT:
//		      return new DexInstruction_InstanceGet(this, insn, parsingCache);
//
//		    case IGET_WIDE:
//		      return new DexInstruction_InstanceGetWide(this, insn, parsingCache);
//
//		    case IPUT:
//		    case IPUT_OBJECT:
//		    case IPUT_BOOLEAN:
//		    case IPUT_BYTE:
//		    case IPUT_CHAR:
//		    case IPUT_SHORT:
//		      return new DexInstruction_InstancePut(this, insn, parsingCache);
//
//		    case IPUT_WIDE:
//		      return new DexInstruction_InstancePutWide(this, insn, parsingCache);
//
//		    case AGET:
//		    case AGET_OBJECT:
//		    case AGET_BOOLEAN:
//		    case AGET_BYTE:
//		    case AGET_CHAR:
//		    case AGET_SHORT:
//		      return new DexInstruction_ArrayGet(this, insn, parsingCache);
//
//		    case AGET_WIDE:
//		      return new DexInstruction_ArrayGetWide(this, insn, parsingCache);
//
//		    case APUT:
//		    case APUT_OBJECT:
//		    case APUT_BOOLEAN:
//		    case APUT_BYTE:
//		    case APUT_CHAR:
//		    case APUT_SHORT:
//		      return new DexInstruction_ArrayPut(this, insn, parsingCache);
//
//		    case APUT_WIDE:
//		      return new DexInstruction_ArrayPutWide(this, insn, parsingCache);
//
//		    case INVOKE_VIRTUAL:
//		    case INVOKE_SUPER:
//		    case INVOKE_DIRECT:
//		    case INVOKE_STATIC:
//		    case INVOKE_INTERFACE:
//		    case INVOKE_VIRTUAL_RANGE:
//		    case INVOKE_SUPER_RANGE:
//		    case INVOKE_DIRECT_RANGE:
//		    case INVOKE_STATIC_RANGE:
//		    case INVOKE_INTERFACE_RANGE:
//		      return new DexInstruction_Invoke(this, insn, parsingCache);
//
//		    case NEG_INT:
//		    case NOT_INT:
//		    case NEG_FLOAT:
//		      return new DexInstruction_UnaryOp(this, insn, parsingCache);
//
//		    case NEG_LONG:
//		    case NOT_LONG:
//		    case NEG_DOUBLE:
//		      return new DexInstruction_UnaryOpWide(this, insn, parsingCache);
//
//		    case INT_TO_FLOAT:
//		    case FLOAT_TO_INT:
//		    case INT_TO_BYTE:
//		    case INT_TO_CHAR:
//		    case INT_TO_SHORT:
//		      return new DexInstruction_Convert(this, insn, parsingCache);
//
//		    case INT_TO_LONG:
//		    case INT_TO_DOUBLE:
//		    case FLOAT_TO_LONG:
//		    case FLOAT_TO_DOUBLE:
//		      return new DexInstruction_ConvertToWide(this, insn, parsingCache);
//
//		    case LONG_TO_INT:
//		    case DOUBLE_TO_INT:
//		    case LONG_TO_FLOAT:
//		    case DOUBLE_TO_FLOAT:
//		      return new DexInstruction_ConvertFromWide(this, insn, parsingCache);
//
//		    case LONG_TO_DOUBLE:
//		    case DOUBLE_TO_LONG:
//		      return new DexInstruction_ConvertWide(this, insn, parsingCache);
//
//		    case ADD_INT:
//		    case SUB_INT:
//		    case MUL_INT:
//		    case DIV_INT:
//		    case REM_INT:
//		    case AND_INT:
//		    case OR_INT:
//		    case XOR_INT:
//		    case SHL_INT:
//		    case SHR_INT:
//		    case USHR_INT:
//		    case ADD_FLOAT:
//		    case SUB_FLOAT:
//		    case MUL_FLOAT:
//		    case DIV_FLOAT:
//		    case REM_FLOAT:
//		    case ADD_INT_2ADDR:
//		    case SUB_INT_2ADDR:
//		    case MUL_INT_2ADDR:
//		    case DIV_INT_2ADDR:
//		    case REM_INT_2ADDR:
//		    case AND_INT_2ADDR:
//		    case OR_INT_2ADDR:
//		    case XOR_INT_2ADDR:
//		    case SHL_INT_2ADDR:
//		    case SHR_INT_2ADDR:
//		    case USHR_INT_2ADDR:
//		    case ADD_FLOAT_2ADDR:
//		    case SUB_FLOAT_2ADDR:
//		    case MUL_FLOAT_2ADDR:
//		    case DIV_FLOAT_2ADDR:
//		    case REM_FLOAT_2ADDR:
//		      return new DexInstruction_BinaryOp(this, insn, parsingCache);
//
//		    case ADD_LONG:
//		    case SUB_LONG:
//		    case MUL_LONG:
//		    case DIV_LONG:
//		    case REM_LONG:
//		    case AND_LONG:
//		    case OR_LONG:
//		    case XOR_LONG:
//		    case SHL_LONG:
//		    case SHR_LONG:
//		    case USHR_LONG:
//		    case ADD_DOUBLE:
//		    case SUB_DOUBLE:
//		    case MUL_DOUBLE:
//		    case DIV_DOUBLE:
//		    case REM_DOUBLE:
//		    case ADD_LONG_2ADDR:
//		    case SUB_LONG_2ADDR:
//		    case MUL_LONG_2ADDR:
//		    case DIV_LONG_2ADDR:
//		    case REM_LONG_2ADDR:
//		    case AND_LONG_2ADDR:
//		    case OR_LONG_2ADDR:
//		    case XOR_LONG_2ADDR:
//		    case SHL_LONG_2ADDR:
//		    case SHR_LONG_2ADDR:
//		    case USHR_LONG_2ADDR:
//		    case ADD_DOUBLE_2ADDR:
//		    case SUB_DOUBLE_2ADDR:
//		    case MUL_DOUBLE_2ADDR:
//		    case DIV_DOUBLE_2ADDR:
//		    case REM_DOUBLE_2ADDR:
//		      return new DexInstruction_BinaryOpWide(this, insn, parsingCache);
//
//		    case ADD_INT_LIT16:
//		    case ADD_INT_LIT8:
//		    case RSUB_INT:
//		    case RSUB_INT_LIT8:
//		    case MUL_INT_LIT16:
//		    case MUL_INT_LIT8:
//		    case DIV_INT_LIT16:
//		    case DIV_INT_LIT8:
//		    case REM_INT_LIT16:
//		    case REM_INT_LIT8:
//		    case AND_INT_LIT16:
//		    case AND_INT_LIT8:
//		    case OR_INT_LIT16:
//		    case OR_INT_LIT8:
//		    case XOR_INT_LIT16:
//		    case XOR_INT_LIT8:
//		    case SHL_INT_LIT8:
//		    case SHR_INT_LIT8:
//		    case USHR_INT_LIT8:
//		      return new DexInstruction_BinaryOpLiteral(this, insn, parsingCache);
//
//		    case PACKED_SWITCH:
//		    case SPARSE_SWITCH:
//		      return new DexInstruction_Switch(this, insn, parsingCache);
//
//		    case FILL_ARRAY_DATA:
//		      return new DexInstruction_FillArray(this, insn, parsingCache);
//
//		    case FILLED_NEW_ARRAY:
//		    case FILLED_NEW_ARRAY_RANGE:
//		      return new DexInstruction_FilledNewArray(this, insn, parsingCache);

		    default:
		    	return new DexInstruction_Unknown(insn);
		    	// throw new InstructionParsingException("Unknown instruction " + insn.opcode.name());
		    }
		  }
}
