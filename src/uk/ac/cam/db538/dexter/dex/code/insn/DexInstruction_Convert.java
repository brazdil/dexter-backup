package uk.ac.cam.db538.dexter.dex.code.insn;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Format.Instruction12x;

import uk.ac.cam.db538.dexter.dex.code.DexRegister;

import lombok.Getter;
import lombok.val;

public class DexInstruction_Convert extends DexInstruction {

  @Getter private final DexRegister RegTo;
  @Getter private final DexRegister RegFrom;
  @Getter private final Opcode_Convert InsnOpcode;

  public DexInstruction_Convert(DexRegister to, DexRegister from, Opcode_Convert opcode) {
    RegTo = to;
    RegFrom = from;
    InsnOpcode = opcode;
  }

  public DexInstruction_Convert(Instruction insn, ParsingState parsingState) throws InstructionParsingException {
    if (insn instanceof Instruction12x && Opcode_Convert.convert(insn.opcode) != null) {

      val insnConvert = (Instruction12x) insn;
      RegTo = parsingState.getRegister(insnConvert.getRegisterA());
      RegFrom = parsingState.getRegister(insnConvert.getRegisterB());
      InsnOpcode = Opcode_Convert.convert(insn.opcode);

    } else
      throw new InstructionParsingException("Unknown instruction format or opcode");
  }

  @Override
  public String getOriginalAssembly() {
    return InsnOpcode.getAssemblyName() + " v" + RegTo.getId() + ", v" + RegFrom.getId();
  }
}
