package uk.ac.cam.db538.dexter.dex.code;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lombok.val;

import org.jf.dexlib.StringIdItem;
import org.jf.dexlib.TypeIdItem;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction11n;
import org.jf.dexlib.Code.Format.Instruction11x;
import org.jf.dexlib.Code.Format.Instruction12x;
import org.jf.dexlib.Code.Format.Instruction21c;
import org.jf.dexlib.Code.Format.Instruction21h;
import org.jf.dexlib.Code.Format.Instruction21s;
import org.jf.dexlib.Code.Format.Instruction22c;
import org.jf.dexlib.Code.Format.Instruction22x;
import org.jf.dexlib.Code.Format.Instruction31c;
import org.jf.dexlib.Code.Format.Instruction31i;
import org.jf.dexlib.Code.Format.Instruction32x;
import org.jf.dexlib.Code.Format.Instruction51l;

import uk.ac.cam.db538.dexter.dex.type.DexArrayType;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexReferenceType;
import uk.ac.cam.db538.dexter.dex.type.TypeCache;
import uk.ac.cam.db538.dexter.dex.type.UnknownTypeException;

public abstract class DexInstruction {

  public abstract String getOriginalAssembly();

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

  public static List<DexInstruction> parse(Instruction[] instructions, TypeCache cache) throws UnknownTypeException {
    val list = new LinkedList<DexInstruction>();
    val registers = new HashMap<Integer, DexRegister>();

    for (val insn : instructions) {
      switch (insn.opcode) {
      case NOP:
        list.add(new DexInstruction_Nop());
        break;
      case MOVE:
      case MOVE_OBJECT:
        val insnMove = (Instruction12x) insn;
        list.add(new DexInstruction_Move(
                   getRegister(insnMove.getRegisterA(), registers),
                   getRegister(insnMove.getRegisterB(), registers),
                   insn.opcode == Opcode.MOVE_OBJECT));
        break;
      case MOVE_FROM16:
      case MOVE_OBJECT_FROM16:
        val insnMoveFrom16 = (Instruction22x) insn;
        list.add(new DexInstruction_Move(
                   getRegister(insnMoveFrom16.getRegisterA(), registers),
                   getRegister(insnMoveFrom16.getRegisterB(), registers),
                   insn.opcode == Opcode.MOVE_OBJECT_FROM16));
        break;
      case MOVE_16:
      case MOVE_OBJECT_16:
        val insnMove16 = (Instruction32x) insn;
        list.add(new DexInstruction_Move(
                   getRegister(insnMove16.getRegisterA(), registers),
                   getRegister(insnMove16.getRegisterB(), registers),
                   insn.opcode == Opcode.MOVE_OBJECT_16));
        break;
      case MOVE_WIDE:
        val insnMoveWide = (Instruction12x) insn;
        list.add(new DexInstruction_MoveWide(
                   getRegister(insnMoveWide.getRegisterA(), registers),
                   getRegister(insnMoveWide.getRegisterA() + 1, registers),
                   getRegister(insnMoveWide.getRegisterB(), registers),
                   getRegister(insnMoveWide.getRegisterB() + 1, registers)));
        break;
      case MOVE_WIDE_FROM16:
        val insnMoveWideFrom16 = (Instruction22x) insn;
        list.add(new DexInstruction_MoveWide(
                   getRegister(insnMoveWideFrom16.getRegisterA(), registers),
                   getRegister(insnMoveWideFrom16.getRegisterA() + 1, registers),
                   getRegister(insnMoveWideFrom16.getRegisterB(), registers),
                   getRegister(insnMoveWideFrom16.getRegisterB() + 1, registers)));
        break;
      case MOVE_WIDE_16:
        val insnMoveWide16 = (Instruction32x) insn;
        list.add(new DexInstruction_MoveWide(
                   getRegister(insnMoveWide16.getRegisterA(), registers),
                   getRegister(insnMoveWide16.getRegisterA() + 1, registers),
                   getRegister(insnMoveWide16.getRegisterB(), registers),
                   getRegister(insnMoveWide16.getRegisterB() + 1, registers)));
        break;
      case MOVE_RESULT:
      case MOVE_RESULT_OBJECT:
        val insnMoveResult = (Instruction11x) insn;
        list.add(new DexInstruction_MoveResult(
                   getRegister(insnMoveResult.getRegisterA(), registers),
                   insn.opcode == Opcode.MOVE_RESULT_OBJECT));
        break;
      case MOVE_RESULT_WIDE:
        val insnMoveResultWide = (Instruction11x) insn;
        list.add(new DexInstruction_MoveResultWide(
                   getRegister(insnMoveResultWide.getRegisterA(), registers),
                   getRegister(insnMoveResultWide.getRegisterA() + 1, registers)));
        break;
      case MOVE_EXCEPTION:
        val insnMoveException = (Instruction11x) insn;
        list.add(new DexInstruction_MoveException(
                   getRegister(insnMoveException.getRegisterA(), registers)));
        break;
      case RETURN_VOID:
        list.add(new DexInstruction_ReturnVoid());
        break;
      case RETURN:
      case RETURN_OBJECT:
        val insnReturn = (Instruction11x) insn;
        list.add(new DexInstruction_Return(
                   getRegister(insnReturn.getRegisterA(), registers),
                   insn.opcode == Opcode.RETURN_OBJECT));
        break;
      case RETURN_WIDE:
        val insnReturnWide = (Instruction11x) insn;
        list.add(new DexInstruction_ReturnWide(
                   getRegister(insnReturnWide.getRegisterA(), registers),
                   getRegister(insnReturnWide.getRegisterA() + 1, registers)));
        break;
      case CONST_4:
        val insnConst4 = (Instruction11n) insn;
        list.add(new DexInstruction_Const(
                   getRegister(insnConst4.getRegisterA(), registers),
                   insnConst4.getLiteral()));
        break;
      case CONST_16:
        val insnConst16 = (Instruction21s) insn;
        list.add(new DexInstruction_Const(
                   getRegister(insnConst16.getRegisterA(), registers),
                   insnConst16.getLiteral()));
        break;
      case CONST:
        val insnConst = (Instruction31i) insn;
        list.add(new DexInstruction_Const(
                   getRegister(insnConst.getRegisterA(), registers),
                   insnConst.getLiteral()));
        break;
      case CONST_HIGH16:
        // we store const/high16 exactly the same as other const instructions,
        // it gets converted back automatically
        val insnConstHigh16 = (Instruction21h) insn;
        list.add(new DexInstruction_Const(
                   getRegister(insnConstHigh16.getRegisterA(), registers),
                   insnConstHigh16.getLiteral() << 16));
        break;
      case CONST_WIDE_16:
        val insnConstWide16 = (Instruction21s) insn;
        list.add(new DexInstruction_ConstWide(
                   getRegister(insnConstWide16.getRegisterA(), registers),
                   getRegister(insnConstWide16.getRegisterA() + 1, registers),
                   insnConstWide16.getLiteral()));
        break;
      case CONST_WIDE_32:
        val insnConstWide32 = (Instruction31i) insn;
        list.add(new DexInstruction_ConstWide(
                   getRegister(insnConstWide32.getRegisterA(), registers),
                   getRegister(insnConstWide32.getRegisterA() + 1, registers),
                   insnConstWide32.getLiteral()));
        break;
      case CONST_WIDE:
        val insnConstWide = (Instruction51l) insn;
        list.add(new DexInstruction_ConstWide(
                   getRegister(insnConstWide.getRegisterA(), registers),
                   getRegister(insnConstWide.getRegisterA() + 1, registers),
                   insnConstWide.getLiteral()));
        break;
      case CONST_WIDE_HIGH16:
        val insnConstWideHigh16 = (Instruction21h) insn;
        list.add(new DexInstruction_ConstWide(
                   getRegister(insnConstWideHigh16.getRegisterA(), registers),
                   getRegister(insnConstWideHigh16.getRegisterA() + 1, registers),
                   insnConstWideHigh16.getLiteral() << 48));
        break;
      case CONST_STRING:
        val insnConstString = (Instruction21c) insn;
        list.add(new DexInstruction_ConstString(
                   getRegister(insnConstString.getRegisterA(), registers),
                   ((StringIdItem) insnConstString.getReferencedItem()).getStringValue()));
        break;
      case CONST_STRING_JUMBO:
        val insnConstStringJumbo = (Instruction31c) insn;
        list.add(new DexInstruction_ConstString(
                   getRegister(insnConstStringJumbo.getRegisterA(), registers),
                   ((StringIdItem) insnConstStringJumbo.getReferencedItem()).getStringValue()));
        break;
      case CONST_CLASS:
        val insnConstClass = (Instruction21c) insn;
        list.add(new DexInstruction_ConstClass(
                   getRegister(insnConstClass.getRegisterA(), registers),
                   DexReferenceType.parse(
                     ((TypeIdItem) insnConstClass.getReferencedItem()).getTypeDescriptor(),
                     cache)));
        break;
      case MONITOR_ENTER:
      case MONITOR_EXIT:
        val insnMonitor = (Instruction11x) insn;
        list.add(new DexInstruction_Monitor(
                   getRegister(insnMonitor.getRegisterA(), registers),
                   insn.opcode == Opcode.MONITOR_ENTER));
        break;
      case CHECK_CAST:
        val insnCheckCast = (Instruction21c) insn;
        list.add(new DexInstruction_CheckCast(
                   getRegister(insnCheckCast.getRegisterA(), registers),
                   DexReferenceType.parse(
                     ((TypeIdItem) insnCheckCast.getReferencedItem()).getTypeDescriptor(),
                     cache)));
        break;
      case INSTANCE_OF:
        val insnInstanceOf = (Instruction22c) insn;
        list.add(new DexInstruction_InstanceOf(
                   getRegister(insnInstanceOf.getRegisterA(), registers),
                   getRegister(insnInstanceOf.getRegisterB(), registers),
                   DexReferenceType.parse(
                     ((TypeIdItem) insnInstanceOf.getReferencedItem()).getTypeDescriptor(),
                     cache)));
        break;
      case NEW_INSTANCE:
        val insnNewInstance = (Instruction21c) insn;
        list.add(new DexInstruction_NewInstance(
                   getRegister(insnNewInstance.getRegisterA(), registers),
                   DexClassType.parse(
                     ((TypeIdItem) insnNewInstance.getReferencedItem()).getTypeDescriptor(),
                     cache)));
        break;
      case NEW_ARRAY:
        val insnNewArray = (Instruction22c) insn;
        list.add(new DexInstruction_NewArray(
                   getRegister(insnNewArray.getRegisterA(), registers),
                   getRegister(insnNewArray.getRegisterB(), registers),
                   DexArrayType.parse(
                     ((TypeIdItem) insnNewArray.getReferencedItem()).getTypeDescriptor(),
                     cache)));
        break;
      }
    }

    return list;
  }
}
