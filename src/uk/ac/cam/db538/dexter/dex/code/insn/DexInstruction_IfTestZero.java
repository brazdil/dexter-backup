package uk.ac.cam.db538.dexter.dex.code.insn;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Format.Instruction21t;

import uk.ac.cam.db538.dexter.dex.code.DexLabel;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;

import lombok.Getter;
import lombok.val;

public class DexInstruction_IfTestZero extends DexInstruction {

  public static enum Opcode {
    eqz,
    nez,
    ltz,
    gez,
    gtz,
    lez;

    public static Opcode convert(org.jf.dexlib.Code.Opcode opcode) {
      switch (opcode) {
      case IF_EQZ:
        return eqz;
      case IF_NEZ:
        return nez;
      case IF_LTZ:
        return ltz;
      case IF_GEZ:
        return gez;
      case IF_GTZ:
        return gtz;
      case IF_LEZ:
        return lez;
      default:
        return null;
      }
    }

    public static org.jf.dexlib.Code.Opcode convert(Opcode opcode) {
      switch (opcode) {
      case eqz:
        return org.jf.dexlib.Code.Opcode.IF_EQZ;
      case nez:
        return org.jf.dexlib.Code.Opcode.IF_NEZ;
      case ltz:
        return org.jf.dexlib.Code.Opcode.IF_LTZ;
      case gez:
        return org.jf.dexlib.Code.Opcode.IF_GEZ;
      case gtz:
        return org.jf.dexlib.Code.Opcode.IF_GTZ;
      case lez:
        return org.jf.dexlib.Code.Opcode.IF_LEZ;
      default:
        return null;
      }
    }
  }

  @Getter private final DexRegister Reg;
  @Getter private final DexLabel Target;
  @Getter private final Opcode InsnOpcode;

  public DexInstruction_IfTestZero(DexRegister reg, DexLabel target, Opcode opcode) {
    Reg = reg;
    Target = target;
    InsnOpcode = opcode;
  }

  public DexInstruction_IfTestZero(Instruction insn, InstructionParsingState parsingState) throws DexInstructionParsingException {
    if (insn instanceof Instruction21t && Opcode.convert(insn.opcode) != null) {

      val insnIfTestZero = (Instruction21t) insn;
      Reg = parsingState.getRegister(insnIfTestZero.getRegisterA());
      Target = parsingState.getLabel(insnIfTestZero.getTargetAddressOffset());
      InsnOpcode = Opcode.convert(insn.opcode);

    } else
      throw new DexInstructionParsingException("Unknown instruction format or opcode");
  }

  @Override
  public String getOriginalAssembly() {
    return "if-" + InsnOpcode.name() + " v" + Reg.getId() +
           ", L" + Target.getOriginalOffset();
  }
}
