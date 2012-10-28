package uk.ac.cam.db538.dexter.dex.code;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lombok.val;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction11n;
import org.jf.dexlib.Code.Format.Instruction11x;
import org.jf.dexlib.Code.Format.Instruction12x;
import org.jf.dexlib.Code.Format.Instruction21h;
import org.jf.dexlib.Code.Format.Instruction21s;
import org.jf.dexlib.Code.Format.Instruction22x;
import org.jf.dexlib.Code.Format.Instruction31i;
import org.jf.dexlib.Code.Format.Instruction32x;

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

  public static List<DexInstruction> parse(Instruction[] instructions) {
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
      }
    }

    return list;
  }
}
