package uk.ac.cam.db538.dexter.dex.code.insn;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Format.Instruction12x;

import uk.ac.cam.db538.dexter.dex.code.DexRegister;

import lombok.Getter;
import lombok.val;

public class DexInstruction_UnaryOp extends DexInstruction {

  public static enum Opcode {
    NegInt("neg-int"),
    NotInt("not-int"),
    NegFloat("neg-float");

    @Getter private final String AssemblyName;

    private Opcode(String assemblyName) {
      AssemblyName = assemblyName;
    }

    public static Opcode convert(org.jf.dexlib.Code.Opcode opcode) {
      switch (opcode) {
      case NEG_INT:
        return NegInt;
      case NOT_INT:
        return NotInt;
      case NEG_FLOAT:
        return NegFloat;
      default:
        return null;
      }
    }

    public static org.jf.dexlib.Code.Opcode convert(Opcode opcode) {
      switch (opcode) {
      case NegInt:
        return org.jf.dexlib.Code.Opcode.NEG_INT;
      case NotInt:
        return org.jf.dexlib.Code.Opcode.NOT_INT;
      case NegFloat:
        return org.jf.dexlib.Code.Opcode.NEG_FLOAT;
      default:
        return null;
      }
    }
  }

  @Getter private final DexRegister RegTo;
  @Getter private final DexRegister RegFrom;
  @Getter private final Opcode InsnOpcode;

  public DexInstruction_UnaryOp(DexRegister to, DexRegister from, Opcode opcode) {
    RegTo = to;
    RegFrom = from;
    InsnOpcode = opcode;
  }

  public DexInstruction_UnaryOp(Instruction insn, InstructionParsingState parsingState) throws DexInstructionParsingException {
    if (insn instanceof Instruction12x && Opcode.convert(insn.opcode) != null) {

      val insnUnaryOp = (Instruction12x) insn;
      RegTo = parsingState.getRegister(insnUnaryOp.getRegisterA());
      RegFrom = parsingState.getRegister(insnUnaryOp.getRegisterB());
      InsnOpcode = Opcode.convert(insn.opcode);

    } else
      throw new DexInstructionParsingException("Unknown instruction format or opcode");
  }

  @Override
  public String getOriginalAssembly() {
    return InsnOpcode.getAssemblyName() + " v" + RegTo.getId() + ", v" + RegFrom.getId();
  }
}
