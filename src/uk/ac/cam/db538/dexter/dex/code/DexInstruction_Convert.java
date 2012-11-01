package uk.ac.cam.db538.dexter.dex.code;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Format.Instruction12x;

import lombok.Getter;
import lombok.val;

public class DexInstruction_Convert extends DexInstruction {

  public static enum Opcode {
    IntToFloat("int-to-float"),
    FloatToInt("float-to-int"),
    IntToByte("int-to-byte"),
    IntToChar("int-to-char"),
    IntToShort("int-to-short");

    @Getter private final String AssemblyName;

    private Opcode(String assemblyName) {
      AssemblyName = assemblyName;
    }

    public static Opcode convert(org.jf.dexlib.Code.Opcode opcode) {
      switch (opcode) {
      case INT_TO_FLOAT:
        return IntToFloat;
      case FLOAT_TO_INT:
        return FloatToInt;
      case INT_TO_BYTE:
        return IntToByte;
      case INT_TO_CHAR:
        return IntToChar;
      case INT_TO_SHORT:
        return IntToShort;
      default:
        return null;
      }
    }

    public static org.jf.dexlib.Code.Opcode convert(Opcode opcode) {
      switch (opcode) {
      case IntToFloat:
        return org.jf.dexlib.Code.Opcode.INT_TO_FLOAT;
      case FloatToInt:
        return org.jf.dexlib.Code.Opcode.FLOAT_TO_INT;
      case IntToByte:
        return org.jf.dexlib.Code.Opcode.INT_TO_BYTE;
      case IntToChar:
        return org.jf.dexlib.Code.Opcode.INT_TO_CHAR;
      case IntToShort:
        return org.jf.dexlib.Code.Opcode.INT_TO_SHORT;
      default:
        return null;
      }
    }
  }

  @Getter private final DexRegister RegTo;
  @Getter private final DexRegister RegFrom;
  @Getter private final Opcode InsnOpcode;

  public DexInstruction_Convert(DexRegister to, DexRegister from, Opcode opcode) {
    RegTo = to;
    RegFrom = from;
    InsnOpcode = opcode;
  }

  public DexInstruction_Convert(Instruction insn, InstructionParsingState parsingState) throws DexInstructionParsingException {
    if (insn instanceof Instruction12x && Opcode.convert(insn.opcode) != null) {

      val insnConvert = (Instruction12x) insn;
      RegTo = parsingState.getRegister(insnConvert.getRegisterA());
      RegFrom = parsingState.getRegister(insnConvert.getRegisterB());
      InsnOpcode = Opcode.convert(insn.opcode);

    } else
      throw new DexInstructionParsingException("Unknown instruction format or opcode");
  }

  @Override
  public String getOriginalAssembly() {
    return InsnOpcode.getAssemblyName() + " v" + RegTo.getId() + ", v" + RegFrom.getId();
  }
}
