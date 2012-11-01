package uk.ac.cam.db538.dexter.dex.code;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Format.Instruction12x;

import lombok.Getter;
import lombok.val;

public class DexInstruction_UnaryOpWide extends DexInstruction {

  public static enum Opcode {
    NegLong("neg-long"),
    NotLong("not-long"),
    NegDouble("neg-double");

    @Getter private final String AssemblyName;

    private Opcode(String assemblyName) {
      AssemblyName = assemblyName;
    }

    public static Opcode convert(org.jf.dexlib.Code.Opcode opcode) {
      switch (opcode) {
      case NEG_LONG:
        return NegLong;
      case NOT_LONG:
        return NotLong;
      case NEG_DOUBLE:
        return NegDouble;
      default:
        return null;
      }
    }

    public static org.jf.dexlib.Code.Opcode convert(Opcode opcode) {
      switch (opcode) {
      case NegLong:
        return org.jf.dexlib.Code.Opcode.NEG_LONG;
      case NotLong:
        return org.jf.dexlib.Code.Opcode.NOT_LONG;
      case NegDouble:
        return org.jf.dexlib.Code.Opcode.NEG_DOUBLE;
      default:
        return null;
      }
    }
  }

  @Getter private final DexRegister RegTo1;
  @Getter private final DexRegister RegTo2;
  @Getter private final DexRegister RegFrom1;
  @Getter private final DexRegister RegFrom2;
  @Getter private final Opcode InsnOpcode;

  public DexInstruction_UnaryOpWide(DexRegister to1, DexRegister to2, DexRegister from1, DexRegister from2, Opcode opcode) {
    RegTo1 = to1;
    RegTo2 = to2;
    RegFrom1 = from1;
    RegFrom2 = from2;
    InsnOpcode = opcode;
  }

  public DexInstruction_UnaryOpWide(Instruction insn, InstructionParsingState parsingState) throws DexInstructionParsingException {
    if (insn instanceof Instruction12x && Opcode.convert(insn.opcode) != null) {

      val insnUnaryOp = (Instruction12x) insn;
      RegTo1 = parsingState.getRegister(insnUnaryOp.getRegisterA());
      RegTo2 = parsingState.getRegister(insnUnaryOp.getRegisterA() + 1);
      RegFrom1 = parsingState.getRegister(insnUnaryOp.getRegisterB());
      RegFrom2 = parsingState.getRegister(insnUnaryOp.getRegisterB() + 1);
      InsnOpcode = Opcode.convert(insn.opcode);

    } else
      throw new DexInstructionParsingException("Unknown instruction format or opcode");
  }

  @Override
  public String getOriginalAssembly() {
    return InsnOpcode.getAssemblyName() + " v" + RegTo1.getId() + ", v" + RegFrom1.getId();
  }
}
