package uk.ac.cam.db538.dexter.dex.code;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Format.Instruction12x;

import lombok.Getter;
import lombok.val;

public class DexInstruction_ConvertFromWide extends DexInstruction {

  public static enum Opcode {
    LongToInt("long-to-int"),
    LongToFloat("long-to-float"),
    DoubleToInt("double-to-int"),
    DoubleToFloat("double-to-float");

    @Getter private final String AssemblyName;

    private Opcode(String assemblyName) {
      AssemblyName = assemblyName;
    }

    public static Opcode convert(org.jf.dexlib.Code.Opcode opcode) {
      switch (opcode) {
      case LONG_TO_INT:
        return LongToInt;
      case LONG_TO_FLOAT:
        return LongToFloat;
      case DOUBLE_TO_INT:
        return DoubleToInt;
      case DOUBLE_TO_FLOAT:
        return DoubleToFloat;
      default:
        return null;
      }
    }

    public static org.jf.dexlib.Code.Opcode convert(Opcode opcode) {
      switch (opcode) {
      case LongToInt:
        return org.jf.dexlib.Code.Opcode.LONG_TO_INT;
      case LongToFloat:
        return org.jf.dexlib.Code.Opcode.LONG_TO_FLOAT;
      case DoubleToInt:
        return org.jf.dexlib.Code.Opcode.DOUBLE_TO_INT;
      case DoubleToFloat:
        return org.jf.dexlib.Code.Opcode.DOUBLE_TO_FLOAT;
      default:
        return null;
      }
    }
  }

  @Getter private final DexRegister RegTo;
  @Getter private final DexRegister RegFrom1;
  @Getter private final DexRegister RegFrom2;
  @Getter private final Opcode InsnOpcode;

  public DexInstruction_ConvertFromWide(DexRegister to, DexRegister from1, DexRegister from2, Opcode opcode) {
    RegTo = to;
    RegFrom1 = from1;
    RegFrom2 = from2;
    InsnOpcode = opcode;
  }

  public DexInstruction_ConvertFromWide(Instruction insn, InstructionParsingState parsingState) throws DexInstructionParsingException {
    if (insn instanceof Instruction12x && Opcode.convert(insn.opcode) != null) {

      val insnConvert = (Instruction12x) insn;
      RegTo = parsingState.getRegister(insnConvert.getRegisterA());
      RegFrom1 = parsingState.getRegister(insnConvert.getRegisterB());
      RegFrom2 = parsingState.getRegister(insnConvert.getRegisterB() + 1);
      InsnOpcode = Opcode.convert(insn.opcode);

    } else
      throw new DexInstructionParsingException("Unknown instruction format or opcode");
  }

  @Override
  public String getOriginalAssembly() {
    return InsnOpcode.getAssemblyName() + " v" + RegTo.getId() + ", v" + RegFrom1.getId();
  }
}
