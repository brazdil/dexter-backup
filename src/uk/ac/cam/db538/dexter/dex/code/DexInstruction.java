package uk.ac.cam.db538.dexter.dex.code;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lombok.val;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction11x;
import org.jf.dexlib.Code.Format.Instruction12x;
import org.jf.dexlib.Code.Format.Instruction22x;
import org.jf.dexlib.Code.Format.Instruction32x;

public abstract class DexInstruction {

  public abstract String getOriginalInstruction();

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
        list.add(new DexNopInstruction());
        break;
      case MOVE:
      case MOVE_OBJECT:
        val insnMove = (Instruction12x) insn;
        list.add(new DexMoveInstruction(
                   getRegister(insnMove.getRegisterA(), registers),
                   getRegister(insnMove.getRegisterB(), registers),
                   insn.opcode == Opcode.MOVE_OBJECT));
        break;
      case MOVE_FROM16:
      case MOVE_OBJECT_FROM16:
        val insnMoveFrom16 = (Instruction22x) insn;
        list.add(new DexMoveInstruction(
                   getRegister(insnMoveFrom16.getRegisterA(), registers),
                   getRegister(insnMoveFrom16.getRegisterB(), registers),
                   insn.opcode == Opcode.MOVE_OBJECT_FROM16));
        break;
      case MOVE_16:
      case MOVE_OBJECT_16:
        val insnMove16 = (Instruction32x) insn;
        list.add(new DexMoveInstruction(
                   getRegister(insnMove16.getRegisterA(), registers),
                   getRegister(insnMove16.getRegisterB(), registers),
                   insn.opcode == Opcode.MOVE_OBJECT_16));
        break;
      case MOVE_WIDE:
        val insnMoveWide = (Instruction12x) insn;
        list.add(new DexMoveWideInstruction(
                   getRegister(insnMoveWide.getRegisterA(), registers),
                   getRegister(insnMoveWide.getRegisterA() + 1, registers),
                   getRegister(insnMoveWide.getRegisterB(), registers),
                   getRegister(insnMoveWide.getRegisterB() + 1, registers)));
        break;
      case MOVE_WIDE_FROM16:
        val insnMoveWideFrom16 = (Instruction22x) insn;
        list.add(new DexMoveWideInstruction(
                   getRegister(insnMoveWideFrom16.getRegisterA(), registers),
                   getRegister(insnMoveWideFrom16.getRegisterA() + 1, registers),
                   getRegister(insnMoveWideFrom16.getRegisterB(), registers),
                   getRegister(insnMoveWideFrom16.getRegisterB() + 1, registers)));
        break;
      case MOVE_WIDE_16:
        val insnMoveWide16 = (Instruction32x) insn;
        list.add(new DexMoveWideInstruction(
                   getRegister(insnMoveWide16.getRegisterA(), registers),
                   getRegister(insnMoveWide16.getRegisterA() + 1, registers),
                   getRegister(insnMoveWide16.getRegisterB(), registers),
                   getRegister(insnMoveWide16.getRegisterB() + 1, registers)));
        break;
      case MOVE_RESULT:
      case MOVE_RESULT_OBJECT:
        val insnMoveResult = (Instruction11x) insn;
        list.add(new DexMoveResultInstruction(
                   getRegister(insnMoveResult.getRegisterA(), registers),
                   insn.opcode == Opcode.MOVE_RESULT_OBJECT));
        break;
      case MOVE_RESULT_WIDE:
        val insnMoveResultWide = (Instruction11x) insn;
        list.add(new DexMoveResultWideInstruction(
                   getRegister(insnMoveResultWide.getRegisterA(), registers),
                   getRegister(insnMoveResultWide.getRegisterA() + 1, registers)));
        break;
      case MOVE_EXCEPTION:
        val insnMoveException = (Instruction11x) insn;
        list.add(new DexMoveExceptionInstruction(
                   getRegister(insnMoveException.getRegisterA(), registers)));
        break;
      case RETURN_VOID:
        list.add(new DexReturnVoidInstruction());
        break;
      case RETURN:
      case RETURN_OBJECT:
        val insnReturn = (Instruction11x) insn;
        list.add(new DexReturnInstruction(
                   getRegister(insnReturn.getRegisterA(), registers),
                   insn.opcode == Opcode.RETURN_OBJECT));
        break;
      case RETURN_WIDE:
        val insnReturnWide = (Instruction11x) insn;
        list.add(new DexReturnWideInstruction(
                   getRegister(insnReturnWide.getRegisterA(), registers),
                   getRegister(insnReturnWide.getRegisterA() + 1, registers)));
        break;
      }
    }

    return list;
  }
}
