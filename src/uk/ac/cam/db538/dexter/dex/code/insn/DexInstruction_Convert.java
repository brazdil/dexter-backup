package uk.ac.cam.db538.dexter.dex.code.insn;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Format.Instruction12x;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;

import lombok.Getter;
import lombok.val;

public class DexInstruction_Convert extends DexInstruction {

  @Getter private final DexRegister regTo;
  @Getter private final DexRegister regFrom;
  @Getter private final Opcode_Convert insnOpcode;

  public DexInstruction_Convert(DexCode methodCode, DexRegister to, DexRegister from, Opcode_Convert opcode) {
    super(methodCode);

    regTo = to;
    regFrom = from;
    insnOpcode = opcode;
  }

  public DexInstruction_Convert(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException {
    super(methodCode);

    if (insn instanceof Instruction12x && Opcode_Convert.convert(insn.opcode) != null) {

      val insnConvert = (Instruction12x) insn;
      regTo = parsingState.getRegister(insnConvert.getRegisterA());
      regFrom = parsingState.getRegister(insnConvert.getRegisterB());
      insnOpcode = Opcode_Convert.convert(insn.opcode);

    } else
      throw new InstructionParsingException("Unknown instruction format or opcode");
  }

  @Override
  public String getOriginalAssembly() {
    return insnOpcode.getAssemblyName() + " v" + regTo.getOriginalIndexString() + ", v" + regFrom.getOriginalIndexString();
  }
}
