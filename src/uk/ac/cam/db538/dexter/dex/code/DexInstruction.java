package uk.ac.cam.db538.dexter.dex.code;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.Code.Instruction;

import uk.ac.cam.db538.dexter.dex.DexParsingCache;
import uk.ac.cam.db538.dexter.dex.type.UnknownTypeException;

public abstract class DexInstruction extends DexCodeElement {

  // INSTRUCTION INSTRUMENTATION

  public static class TaintRegisterMap {
    private final Map<DexRegister, DexRegister> RegisterMap;
    private final int IdOffset;

    public TaintRegisterMap(DexCode code) {
      RegisterMap = new HashMap<DexRegister, DexRegister>();

      // find the maximal register id in the code
      // this is strictly for GUI purposes
      // actual register allocation happens later
      int maxId = -1;
      for (val elem : code)
        if (elem instanceof DexInstruction)
          for (val reg : ((DexInstruction) elem).getReferencedRegisters())
            if (maxId < reg.getId())
              maxId = reg.getId();
      IdOffset = maxId + 1;
    }

    public DexRegister getTaintRegister(DexRegister reg) {
      val taintReg = RegisterMap.get(reg);
      if (taintReg == null) {
        val newReg = new DexRegister(reg.getId() + IdOffset);
        RegisterMap.put(reg, newReg);
        return newReg;
      } else
        return taintReg;
    }
  }

  public DexCodeElement[] instrument(TaintRegisterMap mapping) {
    return new DexCodeElement[] { this };
  }

  protected DexRegister[] getReferencedRegisters() {
    return new DexRegister[] { };
  }

  // INSTRUCTION PARSING

  protected static class InstructionParsingState {
    private final Map<Integer, DexRegister> RegisterIdMap;
    private final Map<Long, DexLabel> LabelOffsetMap;
    @Getter private final DexParsingCache Cache;

    public InstructionParsingState(DexParsingCache cache) {
      RegisterIdMap = new HashMap<Integer, DexRegister>();
      LabelOffsetMap = new HashMap<Long, DexLabel>();
      Cache = cache;
    }

    public DexRegister getRegister(int id) {
      val objId = new Integer(id);
      val register = RegisterIdMap.get(objId);
      if (register == null) {
        val newRegister = new DexRegister(id);
        RegisterIdMap.put(objId, newRegister);
        return newRegister;
      } else
        return register;
    }

    public DexLabel getLabel(long offset) {
      val objOffset = new Long(offset);
      val label = LabelOffsetMap.get(objOffset);
      if (label == null) {
        val newLabel = new DexLabel(offset);
        LabelOffsetMap.put(objOffset, newLabel);
        return newLabel;
      } else
        return label;
    }

    public void placeLabels(DexCode code, Map<Long, DexInstruction> insnOffsetMap) throws DexInstructionParsingException {
      for (val entry : LabelOffsetMap.entrySet()) {
        val labelOffset = entry.getKey();
        val insnAtOffset = insnOffsetMap.get(labelOffset);
        if (insnAtOffset == null)
          throw new DexInstructionParsingException(
            "Label could not be placed (non-existent offset " + labelOffset + ")");
        else {
          val label = entry.getValue();
          code.insertBefore(label, insnAtOffset);
        }
      }
    }
  }

  public static DexCode parse(Instruction[] instructions, DexParsingCache cache) throws UnknownTypeException, DexInstructionParsingException {
    // What happens here:
    // - each instruction is parsed
    //   - offset of each instruction is stored
    //   - labels created in jumping instructions are stored
    //     separately, together with desired offsets
    // - labels are placed in the right position inside
    //   the instruction list

    val code = new DexCode();
    val parsingState = new InstructionParsingState(cache);
    val insnOffsetMap = new HashMap<Long, DexInstruction>();
    long offset = 0L;

    for (val insn : instructions) {
      DexInstruction parsedInsn = null;

      switch (insn.opcode) {

      case NOP:
        parsedInsn = new DexInstruction_Nop();
        break;

      case MOVE:
      case MOVE_OBJECT:
      case MOVE_FROM16:
      case MOVE_OBJECT_FROM16:
      case MOVE_16:
      case MOVE_OBJECT_16:
        parsedInsn = new DexInstruction_Move(insn, parsingState);
        break;

      case MOVE_WIDE:
      case MOVE_WIDE_FROM16:
      case MOVE_WIDE_16:
        parsedInsn = new DexInstruction_MoveWide(insn, parsingState);
        break;

      case MOVE_RESULT:
      case MOVE_RESULT_OBJECT:
        parsedInsn = new DexInstruction_MoveResult(insn, parsingState);
        break;

      case MOVE_RESULT_WIDE:
        parsedInsn = new DexInstruction_MoveResultWide(insn, parsingState);
        break;

      case MOVE_EXCEPTION:
        parsedInsn = new DexInstruction_MoveException(insn, parsingState);
        break;

      case RETURN_VOID:
        parsedInsn = new DexInstruction_ReturnVoid();
        break;

      case RETURN:
      case RETURN_OBJECT:
        parsedInsn = new DexInstruction_Return(insn, parsingState);
        break;

      case RETURN_WIDE:
        parsedInsn = new DexInstruction_ReturnWide(insn, parsingState);
        break;

      case CONST_4:
      case CONST_16:
      case CONST:
      case CONST_HIGH16:
        parsedInsn = new DexInstruction_Const(insn, parsingState);
        break;

      case CONST_WIDE_16:
      case CONST_WIDE_32:
      case CONST_WIDE:
      case CONST_WIDE_HIGH16:
        parsedInsn = new DexInstruction_ConstWide(insn, parsingState);
        break;

      case CONST_STRING:
      case CONST_STRING_JUMBO:
        parsedInsn = new DexInstruction_ConstString(insn, parsingState);
        break;

      case CONST_CLASS:
        parsedInsn = new DexInstruction_ConstClass(insn, parsingState);
        break;

      case MONITOR_ENTER:
      case MONITOR_EXIT:
        parsedInsn = new DexInstruction_Monitor(insn, parsingState);
        break;

      case CHECK_CAST:
        parsedInsn = new DexInstruction_CheckCast(insn, parsingState);
        break;

      case INSTANCE_OF:
        parsedInsn = new DexInstruction_InstanceOf(insn, parsingState);
        break;

      case NEW_INSTANCE:
        parsedInsn = new DexInstruction_NewInstance(insn, parsingState);
        break;

      case NEW_ARRAY:
        parsedInsn = new DexInstruction_NewArray(insn, parsingState);
        break;

      case THROW:
        parsedInsn = new DexInstruction_Throw(insn, parsingState);
        break;

//      case GOTO:
//        val insnGoto = (Instruction10t) insn;
//        parsedInsn = new DexInstruction_Goto(
//          getLabel(offset + insnGoto.getTargetAddressOffset(), labelOffsetMap));
//        break;
//
//      case GOTO_16:
//        val insnGoto16 = (Instruction20t) insn;
//        parsedInsn = new DexInstruction_Goto(
//          getLabel(offset + insnGoto16.getTargetAddressOffset(), labelOffsetMap));
//        break;
//
//      case GOTO_32:
//        val insnGoto32 = (Instruction30t) insn;
//        parsedInsn = new DexInstruction_Goto(
//          getLabel(offset + insnGoto32.getTargetAddressOffset(), labelOffsetMap));
//        break;
//
//      case IF_EQ:
//      case IF_NE:
//      case IF_LT:
//      case IF_GE:
//      case IF_GT:
//      case IF_LE:
//        val insnIfTest = (Instruction22t) insn;
//        parsedInsn = new DexInstruction_IfTest(
//          getRegister(insnIfTest.getRegisterA(), registers),
//          getRegister(insnIfTest.getRegisterB(), registers),
//          getLabel(offset + insnIfTest.getTargetAddressOffset(), labelOffsetMap),
//          DexInstruction_IfTest.Opcode.convert(insn.opcode));
//        break;
//
//      case IF_EQZ:
//      case IF_NEZ:
//      case IF_LTZ:
//      case IF_GEZ:
//      case IF_GTZ:
//      case IF_LEZ:
//        val insnIfTestZero = (Instruction21t) insn;
//        parsedInsn = new DexInstruction_IfTestZero(
//          getRegister(insnIfTestZero.getRegisterA(), registers),
//          getLabel(offset + insnIfTestZero.getTargetAddressOffset(), labelOffsetMap),
//          DexInstruction_IfTestZero.Opcode.convert(insn.opcode));
//        break;
//
//      case NEG_INT:
//      case NOT_INT:
//      case NEG_FLOAT:
//        val insnUnaryOp = (Instruction12x) insn;
//        parsedInsn = new DexInstruction_UnaryOp(
//          getRegister(insnUnaryOp.getRegisterA(), registers),
//          getRegister(insnUnaryOp.getRegisterB(), registers),
//          DexInstruction_UnaryOp.Opcode.convert(insn.opcode));
//        break;
//
//      case NEG_LONG:
//      case NOT_LONG:
//      case NEG_DOUBLE:
//        val insnUnaryOpWide = (Instruction12x) insn;
//        parsedInsn = new DexInstruction_UnaryOpWide(
//          getRegister(insnUnaryOpWide.getRegisterA(), registers),
//          getRegister(insnUnaryOpWide.getRegisterA() + 1, registers),
//          getRegister(insnUnaryOpWide.getRegisterB(), registers),
//          getRegister(insnUnaryOpWide.getRegisterB() + 1, registers),
//          DexInstruction_UnaryOpWide.Opcode.convert(insn.opcode));
//        break;
//
//      case INT_TO_FLOAT:
//      case FLOAT_TO_INT:
//      case INT_TO_BYTE:
//      case INT_TO_CHAR:
//      case INT_TO_SHORT:
//        val insnConvert = (Instruction12x) insn;
//        parsedInsn = new DexInstruction_Convert(
//          getRegister(insnConvert.getRegisterA(), registers),
//          getRegister(insnConvert.getRegisterB(), registers),
//          DexInstruction_Convert.Opcode.convert(insn.opcode));
//        break;
//
//      case INT_TO_LONG:
//      case INT_TO_DOUBLE:
//      case FLOAT_TO_LONG:
//      case FLOAT_TO_DOUBLE:
//        val insnConvertToWide = (Instruction12x) insn;
//        parsedInsn = new DexInstruction_ConvertToWide(
//          getRegister(insnConvertToWide.getRegisterA(), registers),
//          getRegister(insnConvertToWide.getRegisterA() + 1, registers),
//          getRegister(insnConvertToWide.getRegisterB(), registers),
//          DexInstruction_ConvertToWide.Opcode.convert(insn.opcode));
//        break;
//
//      case LONG_TO_INT:
//      case DOUBLE_TO_INT:
//      case LONG_TO_FLOAT:
//      case DOUBLE_TO_FLOAT:
//        val insnConvertFromWide = (Instruction12x) insn;
//        parsedInsn = new DexInstruction_ConvertFromWide(
//          getRegister(insnConvertFromWide.getRegisterA(), registers),
//          getRegister(insnConvertFromWide.getRegisterB(), registers),
//          getRegister(insnConvertFromWide.getRegisterB() + 1, registers),
//          DexInstruction_ConvertFromWide.Opcode.convert(insn.opcode));
//        break;
//
//      case LONG_TO_DOUBLE:
//      case DOUBLE_TO_LONG:
//        val insnConvertWide = (Instruction12x) insn;
//        parsedInsn = new DexInstruction_ConvertWide(
//          getRegister(insnConvertWide.getRegisterA(), registers),
//          getRegister(insnConvertWide.getRegisterA() + 1, registers),
//          getRegister(insnConvertWide.getRegisterB(), registers),
//          getRegister(insnConvertWide.getRegisterB() + 1, registers),
//          DexInstruction_ConvertWide.Opcode.convert(insn.opcode));
//        break;
//
//      case ADD_INT:
//      case SUB_INT:
//      case MUL_INT:
//      case DIV_INT:
//      case REM_INT:
//      case AND_INT:
//      case OR_INT:
//      case XOR_INT:
//      case SHL_INT:
//      case SHR_INT:
//      case USHR_INT:
//      case ADD_FLOAT:
//      case SUB_FLOAT:
//      case MUL_FLOAT:
//      case DIV_FLOAT:
//      case REM_FLOAT:
//        val insnBinaryOp = (Instruction23x) insn;
//        parsedInsn = new DexInstruction_BinaryOp(
//          getRegister(insnBinaryOp.getRegisterA(), registers),
//          getRegister(insnBinaryOp.getRegisterB(), registers),
//          getRegister(insnBinaryOp.getRegisterC(), registers),
//          DexInstruction_BinaryOp.Opcode.convert(insn.opcode));
//        break;
//
//      case ADD_INT_2ADDR:
//      case SUB_INT_2ADDR:
//      case MUL_INT_2ADDR:
//      case DIV_INT_2ADDR:
//      case REM_INT_2ADDR:
//      case AND_INT_2ADDR:
//      case OR_INT_2ADDR:
//      case XOR_INT_2ADDR:
//      case SHL_INT_2ADDR:
//      case SHR_INT_2ADDR:
//      case USHR_INT_2ADDR:
//      case ADD_FLOAT_2ADDR:
//      case SUB_FLOAT_2ADDR:
//      case MUL_FLOAT_2ADDR:
//      case DIV_FLOAT_2ADDR:
//      case REM_FLOAT_2ADDR:
//        val insnBinaryOp2addr = (Instruction12x) insn;
//        parsedInsn = new DexInstruction_BinaryOp(
//          getRegister(insnBinaryOp2addr.getRegisterA(), registers),
//          getRegister(insnBinaryOp2addr.getRegisterA(), registers),
//          getRegister(insnBinaryOp2addr.getRegisterB(), registers),
//          DexInstruction_BinaryOp.Opcode.convert(insn.opcode));
//        break;
//
//      case ADD_LONG:
//      case SUB_LONG:
//      case MUL_LONG:
//      case DIV_LONG:
//      case REM_LONG:
//      case AND_LONG:
//      case OR_LONG:
//      case XOR_LONG:
//      case SHL_LONG:
//      case SHR_LONG:
//      case USHR_LONG:
//      case ADD_DOUBLE:
//      case SUB_DOUBLE:
//      case MUL_DOUBLE:
//      case DIV_DOUBLE:
//      case REM_DOUBLE:
//        val insnBinaryOpWide = (Instruction23x) insn;
//        parsedInsn = new DexInstruction_BinaryOpWide(
//          getRegister(insnBinaryOpWide.getRegisterA(), registers),
//          getRegister(insnBinaryOpWide.getRegisterA() + 1, registers),
//          getRegister(insnBinaryOpWide.getRegisterB(), registers),
//          getRegister(insnBinaryOpWide.getRegisterB() + 1, registers),
//          getRegister(insnBinaryOpWide.getRegisterC(), registers),
//          getRegister(insnBinaryOpWide.getRegisterC() + 1, registers),
//          DexInstruction_BinaryOpWide.Opcode.convert(insn.opcode));
//        break;
//
//      case ADD_LONG_2ADDR:
//      case SUB_LONG_2ADDR:
//      case MUL_LONG_2ADDR:
//      case DIV_LONG_2ADDR:
//      case REM_LONG_2ADDR:
//      case AND_LONG_2ADDR:
//      case OR_LONG_2ADDR:
//      case XOR_LONG_2ADDR:
//      case SHL_LONG_2ADDR:
//      case SHR_LONG_2ADDR:
//      case USHR_LONG_2ADDR:
//      case ADD_DOUBLE_2ADDR:
//      case SUB_DOUBLE_2ADDR:
//      case MUL_DOUBLE_2ADDR:
//      case DIV_DOUBLE_2ADDR:
//      case REM_DOUBLE_2ADDR:
//        val insnBinaryOpWide2addr = (Instruction12x) insn;
//        parsedInsn = new DexInstruction_BinaryOpWide(
//          getRegister(insnBinaryOpWide2addr.getRegisterA(), registers),
//          getRegister(insnBinaryOpWide2addr.getRegisterA() + 1, registers),
//          getRegister(insnBinaryOpWide2addr.getRegisterA(), registers),
//          getRegister(insnBinaryOpWide2addr.getRegisterA() + 1, registers),
//          getRegister(insnBinaryOpWide2addr.getRegisterB(), registers),
//          getRegister(insnBinaryOpWide2addr.getRegisterB() + 1, registers),
//          DexInstruction_BinaryOpWide.Opcode.convert(insn.opcode));
//        break;

      default:
        // TODO: throw exception
        parsedInsn = new DexInstruction_Unknown();
        break;
      }

      code.add(parsedInsn);
      insnOffsetMap.put(offset, parsedInsn);
      offset += insn.getSize(0); // arg is ignored
    }

    parsingState.placeLabels(code, insnOffsetMap);

    return code;
  }
}
