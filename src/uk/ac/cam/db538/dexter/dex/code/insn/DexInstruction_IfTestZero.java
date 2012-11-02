package uk.ac.cam.db538.dexter.dex.code.insn;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Format.Instruction21t;

import uk.ac.cam.db538.dexter.dex.code.DexLabel;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;

import lombok.Getter;
import lombok.val;

public class DexInstruction_IfTestZero extends DexInstruction {

  @Getter private final DexRegister Reg;
  @Getter private final DexLabel Target;
  @Getter private final Opcode_IfTestZero InsnOpcode;

  public DexInstruction_IfTestZero(DexRegister reg, DexLabel target, Opcode_IfTestZero opcode) {
    Reg = reg;
    Target = target;
    InsnOpcode = opcode;
  }

  public DexInstruction_IfTestZero(Instruction insn, ParsingState parsingState) throws InstructionParsingException {
    if (insn instanceof Instruction21t && Opcode_IfTestZero.convert(insn.opcode) != null) {

      val insnIfTestZero = (Instruction21t) insn;
      Reg = parsingState.getRegister(insnIfTestZero.getRegisterA());
      Target = parsingState.getLabel(insnIfTestZero.getTargetAddressOffset());
      InsnOpcode = Opcode_IfTestZero.convert(insn.opcode);

    } else
      throw new InstructionParsingException("Unknown instruction format or opcode");
  }

  @Override
  public String getOriginalAssembly() {
    return "if-" + InsnOpcode.name() + " v" + Reg.getId() +
           ", L" + Target.getOriginalOffset();
  }
}
