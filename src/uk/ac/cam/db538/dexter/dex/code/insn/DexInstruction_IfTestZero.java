package uk.ac.cam.db538.dexter.dex.code.insn;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Format.Instruction21t;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexLabel;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;

import lombok.Getter;
import lombok.val;

public class DexInstruction_IfTestZero extends DexInstruction {

  @Getter private final DexRegister reg;
  @Getter private final DexLabel target;
  @Getter private final Opcode_IfTestZero insnOpcode;

  public DexInstruction_IfTestZero(DexCode methodCode, DexRegister reg, DexLabel target, Opcode_IfTestZero opcode) {
    super(methodCode);

    this.reg = reg;
    this.target = target;
    this.insnOpcode = opcode;
  }

  public DexInstruction_IfTestZero(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException {
    super(methodCode);

    if (insn instanceof Instruction21t && Opcode_IfTestZero.convert(insn.opcode) != null) {

      val insnIfTestZero = (Instruction21t) insn;
      reg = parsingState.getRegister(insnIfTestZero.getRegisterA());
      target = parsingState.getLabel(insnIfTestZero.getTargetAddressOffset());
      insnOpcode = Opcode_IfTestZero.convert(insn.opcode);

    } else
      throw new InstructionParsingException("Unknown instruction format or opcode");
  }

  @Override
  public String getOriginalAssembly() {
    return "if-" + insnOpcode.name() + " v" + reg.getOriginalIndexString() +
           ", L" + target.getOriginalAbsoluteOffset();
  }
}
