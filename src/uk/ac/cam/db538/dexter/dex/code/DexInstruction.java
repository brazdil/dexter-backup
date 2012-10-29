package uk.ac.cam.db538.dexter.dex.code;

import java.util.HashMap;
import java.util.Map;

import lombok.val;

import org.jf.dexlib.StringIdItem;
import org.jf.dexlib.TypeIdItem;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction10t;
import org.jf.dexlib.Code.Format.Instruction11n;
import org.jf.dexlib.Code.Format.Instruction11x;
import org.jf.dexlib.Code.Format.Instruction12x;
import org.jf.dexlib.Code.Format.Instruction20t;
import org.jf.dexlib.Code.Format.Instruction21c;
import org.jf.dexlib.Code.Format.Instruction21h;
import org.jf.dexlib.Code.Format.Instruction21s;
import org.jf.dexlib.Code.Format.Instruction21t;
import org.jf.dexlib.Code.Format.Instruction22c;
import org.jf.dexlib.Code.Format.Instruction22t;
import org.jf.dexlib.Code.Format.Instruction22x;
import org.jf.dexlib.Code.Format.Instruction30t;
import org.jf.dexlib.Code.Format.Instruction31c;
import org.jf.dexlib.Code.Format.Instruction31i;
import org.jf.dexlib.Code.Format.Instruction32x;
import org.jf.dexlib.Code.Format.Instruction51l;

import uk.ac.cam.db538.dexter.dex.DexParsingCache;
import uk.ac.cam.db538.dexter.dex.type.DexArrayType;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexReferenceType;
import uk.ac.cam.db538.dexter.dex.type.UnknownTypeException;

public abstract class DexInstruction extends DexCodeElement {

  private static DexRegister getRegister(int id, Map<Integer, DexRegister> map) {
    val objId = new Integer(id);
    val register = map.get(objId);
    if (register == null) {
      val newRegister = new DexRegister(id);
      map.put(objId, newRegister);
      return newRegister;
    } else
      return register;
  }

  private static DexLabel getLabel(long offset, Map<Long, DexLabel> map) {
    val objOffset = new Long(offset);
    val label = map.get(objOffset);
    if (label == null) {
      val newLabel = new DexLabel(offset);
      map.put(objOffset, newLabel);
      return newLabel;
    } else
      return label;
  }

  public static DexCode parse(Instruction[] instructions, DexParsingCache cache) throws UnknownTypeException, DexInstructionParsingException {
    val code = new DexCode();
    val registers = new HashMap<Integer, DexRegister>();

    // What happens here:
    // - each instruction is parsed
    //   - offset of each instruction is stored
    //   - labels created in jumping instructions are stored
    //     separately, together with desired offsets
    // - labels are placed in the right position inside
    //   the instruction list

    val insnOffsetMap = new HashMap<Long, DexInstruction>();
    val labelOffsetMap = new HashMap<Long, DexLabel>();
    long offset = 0L;

    for (val insn : instructions) {
      DexInstruction parsedInsn = null;

      switch (insn.opcode) {

      case NOP:
        parsedInsn = new DexInstruction_Nop();
        break;

      case MOVE:
      case MOVE_OBJECT:
        val insnMove = (Instruction12x) insn;
        parsedInsn = new DexInstruction_Move(
          getRegister(insnMove.getRegisterA(), registers),
          getRegister(insnMove.getRegisterB(), registers),
          insn.opcode == Opcode.MOVE_OBJECT);
        break;

      case MOVE_FROM16:
      case MOVE_OBJECT_FROM16:
        val insnMoveFrom16 = (Instruction22x) insn;
        parsedInsn = new DexInstruction_Move(
          getRegister(insnMoveFrom16.getRegisterA(), registers),
          getRegister(insnMoveFrom16.getRegisterB(), registers),
          insn.opcode == Opcode.MOVE_OBJECT_FROM16);
        break;

      case MOVE_16:
      case MOVE_OBJECT_16:
        val insnMove16 = (Instruction32x) insn;
        parsedInsn = new DexInstruction_Move(
          getRegister(insnMove16.getRegisterA(), registers),
          getRegister(insnMove16.getRegisterB(), registers),
          insn.opcode == Opcode.MOVE_OBJECT_16);
        break;

      case MOVE_WIDE:
        val insnMoveWide = (Instruction12x) insn;
        parsedInsn = new DexInstruction_MoveWide(
          getRegister(insnMoveWide.getRegisterA(), registers),
          getRegister(insnMoveWide.getRegisterA() + 1, registers),
          getRegister(insnMoveWide.getRegisterB(), registers),
          getRegister(insnMoveWide.getRegisterB() + 1, registers));
        break;

      case MOVE_WIDE_FROM16:
        val insnMoveWideFrom16 = (Instruction22x) insn;
        parsedInsn = new DexInstruction_MoveWide(
          getRegister(insnMoveWideFrom16.getRegisterA(), registers),
          getRegister(insnMoveWideFrom16.getRegisterA() + 1, registers),
          getRegister(insnMoveWideFrom16.getRegisterB(), registers),
          getRegister(insnMoveWideFrom16.getRegisterB() + 1, registers));
        break;

      case MOVE_WIDE_16:
        val insnMoveWide16 = (Instruction32x) insn;
        parsedInsn = new DexInstruction_MoveWide(
          getRegister(insnMoveWide16.getRegisterA(), registers),
          getRegister(insnMoveWide16.getRegisterA() + 1, registers),
          getRegister(insnMoveWide16.getRegisterB(), registers),
          getRegister(insnMoveWide16.getRegisterB() + 1, registers));
        break;

      case MOVE_RESULT:
      case MOVE_RESULT_OBJECT:
        val insnMoveResult = (Instruction11x) insn;
        parsedInsn = new DexInstruction_MoveResult(
          getRegister(insnMoveResult.getRegisterA(), registers),
          insn.opcode == Opcode.MOVE_RESULT_OBJECT);
        break;

      case MOVE_RESULT_WIDE:
        val insnMoveResultWide = (Instruction11x) insn;
        parsedInsn = new DexInstruction_MoveResultWide(
          getRegister(insnMoveResultWide.getRegisterA(), registers),
          getRegister(insnMoveResultWide.getRegisterA() + 1, registers));
        break;

      case MOVE_EXCEPTION:
        val insnMoveException = (Instruction11x) insn;
        parsedInsn = new DexInstruction_MoveException(
          getRegister(insnMoveException.getRegisterA(), registers));
        break;

      case RETURN_VOID:
        parsedInsn = new DexInstruction_ReturnVoid();
        break;

      case RETURN:
      case RETURN_OBJECT:
        val insnReturn = (Instruction11x) insn;
        parsedInsn = new DexInstruction_Return(
          getRegister(insnReturn.getRegisterA(), registers),
          insn.opcode == Opcode.RETURN_OBJECT);
        break;

      case RETURN_WIDE:
        val insnReturnWide = (Instruction11x) insn;
        parsedInsn = new DexInstruction_ReturnWide(
          getRegister(insnReturnWide.getRegisterA(), registers),
          getRegister(insnReturnWide.getRegisterA() + 1, registers));
        break;

      case CONST_4:
        val insnConst4 = (Instruction11n) insn;
        parsedInsn = new DexInstruction_Const(
          getRegister(insnConst4.getRegisterA(), registers),
          insnConst4.getLiteral());
        break;

      case CONST_16:
        val insnConst16 = (Instruction21s) insn;
        parsedInsn = new DexInstruction_Const(
          getRegister(insnConst16.getRegisterA(), registers),
          insnConst16.getLiteral());
        break;

      case CONST:
        val insnConst = (Instruction31i) insn;
        parsedInsn = new DexInstruction_Const(
          getRegister(insnConst.getRegisterA(), registers),
          insnConst.getLiteral());
        break;

      case CONST_HIGH16:
        // we store const/high16 exactly the same as other const instructions,
        // it gets converted back automatically
        val insnConstHigh16 = (Instruction21h) insn;
        parsedInsn = new DexInstruction_Const(
          getRegister(insnConstHigh16.getRegisterA(), registers),
          insnConstHigh16.getLiteral() << 16);
        break;

      case CONST_WIDE_16:
        val insnConstWide16 = (Instruction21s) insn;
        parsedInsn = new DexInstruction_ConstWide(
          getRegister(insnConstWide16.getRegisterA(), registers),
          getRegister(insnConstWide16.getRegisterA() + 1, registers),
          insnConstWide16.getLiteral());
        break;

      case CONST_WIDE_32:
        val insnConstWide32 = (Instruction31i) insn;
        parsedInsn = new DexInstruction_ConstWide(
          getRegister(insnConstWide32.getRegisterA(), registers),
          getRegister(insnConstWide32.getRegisterA() + 1, registers),
          insnConstWide32.getLiteral());
        break;

      case CONST_WIDE:
        val insnConstWide = (Instruction51l) insn;
        parsedInsn = new DexInstruction_ConstWide(
          getRegister(insnConstWide.getRegisterA(), registers),
          getRegister(insnConstWide.getRegisterA() + 1, registers),
          insnConstWide.getLiteral());
        break;

      case CONST_WIDE_HIGH16:
        val insnConstWideHigh16 = (Instruction21h) insn;
        parsedInsn = new DexInstruction_ConstWide(
          getRegister(insnConstWideHigh16.getRegisterA(), registers),
          getRegister(insnConstWideHigh16.getRegisterA() + 1, registers),
          insnConstWideHigh16.getLiteral() << 48);
        break;

      case CONST_STRING:
        val insnConstString = (Instruction21c) insn;
        parsedInsn = new DexInstruction_ConstString(
          getRegister(insnConstString.getRegisterA(), registers),
          DexStringConstant.create(((StringIdItem) insnConstString.getReferencedItem()), cache));
        break;

      case CONST_STRING_JUMBO:
        val insnConstStringJumbo = (Instruction31c) insn;
        parsedInsn = new DexInstruction_ConstString(
          getRegister(insnConstStringJumbo.getRegisterA(), registers),
          DexStringConstant.create(((StringIdItem) insnConstStringJumbo.getReferencedItem()), cache));
        break;

      case CONST_CLASS:
        val insnConstClass = (Instruction21c) insn;
        parsedInsn = new DexInstruction_ConstClass(
          getRegister(insnConstClass.getRegisterA(), registers),
          DexReferenceType.parse(
            ((TypeIdItem) insnConstClass.getReferencedItem()).getTypeDescriptor(),
            cache));
        break;

      case MONITOR_ENTER:
      case MONITOR_EXIT:
        val insnMonitor = (Instruction11x) insn;
        parsedInsn = new DexInstruction_Monitor(
          getRegister(insnMonitor.getRegisterA(), registers),
          insn.opcode == Opcode.MONITOR_ENTER);
        break;

      case CHECK_CAST:
        val insnCheckCast = (Instruction21c) insn;
        parsedInsn = new DexInstruction_CheckCast(
          getRegister(insnCheckCast.getRegisterA(), registers),
          DexReferenceType.parse(
            ((TypeIdItem) insnCheckCast.getReferencedItem()).getTypeDescriptor(),
            cache));
        break;

      case INSTANCE_OF:
        val insnInstanceOf = (Instruction22c) insn;
        parsedInsn = new DexInstruction_InstanceOf(
          getRegister(insnInstanceOf.getRegisterA(), registers),
          getRegister(insnInstanceOf.getRegisterB(), registers),
          DexReferenceType.parse(
            ((TypeIdItem) insnInstanceOf.getReferencedItem()).getTypeDescriptor(),
            cache));
        break;

      case NEW_INSTANCE:
        val insnNewInstance = (Instruction21c) insn;
        parsedInsn = new DexInstruction_NewInstance(
          getRegister(insnNewInstance.getRegisterA(), registers),
          DexClassType.parse(
            ((TypeIdItem) insnNewInstance.getReferencedItem()).getTypeDescriptor(),
            cache));
        break;

      case NEW_ARRAY:
        val insnNewArray = (Instruction22c) insn;
        parsedInsn = new DexInstruction_NewArray(
          getRegister(insnNewArray.getRegisterA(), registers),
          getRegister(insnNewArray.getRegisterB(), registers),
          DexArrayType.parse(
            ((TypeIdItem) insnNewArray.getReferencedItem()).getTypeDescriptor(),
            cache));
        break;

      case THROW:
        val insnThrow = (Instruction11x) insn;
        parsedInsn = new DexInstruction_Throw(
          getRegister(insnThrow.getRegisterA(), registers));
        break;

      case GOTO:
        val insnGoto = (Instruction10t) insn;
        parsedInsn = new DexInstruction_Goto(
          getLabel(offset + insnGoto.getTargetAddressOffset(), labelOffsetMap));
        break;

      case GOTO_16:
        val insnGoto16 = (Instruction20t) insn;
        parsedInsn = new DexInstruction_Goto(
          getLabel(offset + insnGoto16.getTargetAddressOffset(), labelOffsetMap));
        break;

      case GOTO_32:
        val insnGoto32 = (Instruction30t) insn;
        parsedInsn = new DexInstruction_Goto(
          getLabel(offset + insnGoto32.getTargetAddressOffset(), labelOffsetMap));
        break;

      case IF_EQ:
      case IF_NE:
      case IF_LT:
      case IF_GE:
      case IF_GT:
      case IF_LE:
        val insnIfTest = (Instruction22t) insn;
        DexInstruction_IfTest.Operation insnIfTest_Type = null;
        switch (insn.opcode) {
        case IF_EQ:
          insnIfTest_Type = DexInstruction_IfTest.Operation.eq;
          break;
        case IF_NE:
          insnIfTest_Type = DexInstruction_IfTest.Operation.ne;
          break;
        case IF_LT:
          insnIfTest_Type = DexInstruction_IfTest.Operation.lt;
          break;
        case IF_GE:
          insnIfTest_Type = DexInstruction_IfTest.Operation.ge;
          break;
        case IF_GT:
          insnIfTest_Type = DexInstruction_IfTest.Operation.gt;
          break;
        case IF_LE:
          insnIfTest_Type = DexInstruction_IfTest.Operation.le;
          break;
        }
        parsedInsn = new DexInstruction_IfTest(
          getRegister(insnIfTest.getRegisterA(), registers),
          getRegister(insnIfTest.getRegisterB(), registers),
          getLabel(offset + insnIfTest.getTargetAddressOffset(), labelOffsetMap),
          insnIfTest_Type);
        break;

      case IF_EQZ:
      case IF_NEZ:
      case IF_LTZ:
      case IF_GEZ:
      case IF_GTZ:
      case IF_LEZ:
        val insnIfTestZero = (Instruction21t) insn;
        DexInstruction_IfTestZero.Operation insnIfTestZero_Type = null;
        switch (insn.opcode) {
        case IF_EQZ:
          insnIfTestZero_Type = DexInstruction_IfTestZero.Operation.eqz;
          break;
        case IF_NEZ:
          insnIfTestZero_Type = DexInstruction_IfTestZero.Operation.nez;
          break;
        case IF_LTZ:
          insnIfTestZero_Type = DexInstruction_IfTestZero.Operation.ltz;
          break;
        case IF_GEZ:
          insnIfTestZero_Type = DexInstruction_IfTestZero.Operation.gez;
          break;
        case IF_GTZ:
          insnIfTestZero_Type = DexInstruction_IfTestZero.Operation.gtz;
          break;
        case IF_LEZ:
          insnIfTestZero_Type = DexInstruction_IfTestZero.Operation.lez;
          break;
        }
        parsedInsn = new DexInstruction_IfTestZero(
          getRegister(insnIfTestZero.getRegisterA(), registers),
          getLabel(offset + insnIfTestZero.getTargetAddressOffset(), labelOffsetMap),
          insnIfTestZero_Type);
        break;

      case NEG_INT:
      case NOT_INT:
      case NEG_FLOAT:
        val insnUnaryOp = (Instruction12x) insn;
        DexInstruction_UnaryOp.Operation insnUnaryOp_Operation = null;
        DexInstruction_UnaryOp.Operand insnUnaryOp_Operand = null;
        switch (insn.opcode) {
        case NEG_INT:
          insnUnaryOp_Operation = DexInstruction_UnaryOp.Operation.neg;
          insnUnaryOp_Operand = DexInstruction_UnaryOp.Operand.Int;
          break;
        case NOT_INT:
          insnUnaryOp_Operation = DexInstruction_UnaryOp.Operation.not;
          insnUnaryOp_Operand = DexInstruction_UnaryOp.Operand.Int;
          break;
        case NEG_FLOAT:
          insnUnaryOp_Operation = DexInstruction_UnaryOp.Operation.neg;
          insnUnaryOp_Operand = DexInstruction_UnaryOp.Operand.Float;
          break;
        }
        parsedInsn = new DexInstruction_UnaryOp(
          getRegister(insnUnaryOp.getRegisterA(), registers),
          getRegister(insnUnaryOp.getRegisterB(), registers),
          insnUnaryOp_Operation,
          insnUnaryOp_Operand);
        break;

      case NEG_LONG:
      case NOT_LONG:
      case NEG_DOUBLE:
        val insnUnaryOpWide = (Instruction12x) insn;
        DexInstruction_UnaryOpWide.Operation insnUnaryOpWide_Operation = null;
        DexInstruction_UnaryOpWide.Operand insnUnaryOpWide_Operand = null;
        switch (insn.opcode) {
        case NEG_LONG:
          insnUnaryOpWide_Operation = DexInstruction_UnaryOpWide.Operation.neg;
          insnUnaryOpWide_Operand = DexInstruction_UnaryOpWide.Operand.Long;
          break;
        case NOT_LONG:
          insnUnaryOpWide_Operation = DexInstruction_UnaryOpWide.Operation.not;
          insnUnaryOpWide_Operand = DexInstruction_UnaryOpWide.Operand.Long;
          break;
        case NEG_DOUBLE:
          insnUnaryOpWide_Operation = DexInstruction_UnaryOpWide.Operation.neg;
          insnUnaryOpWide_Operand = DexInstruction_UnaryOpWide.Operand.Double;
          break;
        }
        parsedInsn = new DexInstruction_UnaryOpWide(
          getRegister(insnUnaryOpWide.getRegisterA(), registers),
          getRegister(insnUnaryOpWide.getRegisterA() + 1, registers),
          getRegister(insnUnaryOpWide.getRegisterB(), registers),
          getRegister(insnUnaryOpWide.getRegisterB() + 1, registers),
          insnUnaryOpWide_Operation,
          insnUnaryOpWide_Operand);
        break;

      default:
        // TODO: throw exception
        parsedInsn = new DexInstruction_Unknown();
        break;
      }

      code.add(parsedInsn);
      insnOffsetMap.put(offset, parsedInsn);
      offset += insn.getSize(0); // arg is ignored
    }

    // place the labels
    for (val entry : labelOffsetMap.entrySet()) {
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

    return code;
  }
}
