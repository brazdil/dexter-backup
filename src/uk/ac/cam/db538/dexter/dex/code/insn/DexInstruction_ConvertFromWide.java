package uk.ac.cam.db538.dexter.dex.code.insn;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Format.Instruction12x;

import uk.ac.cam.db538.dexter.dex.code.DexRegister;

import lombok.Getter;
import lombok.val;

public class DexInstruction_ConvertFromWide extends DexInstruction {

  @Getter private final DexRegister RegTo;
  @Getter private final DexRegister RegFrom1;
  @Getter private final DexRegister RegFrom2;
  @Getter private final Opcode_ConvertFromWide InsnOpcode;

  public DexInstruction_ConvertFromWide(DexRegister to, DexRegister from1, DexRegister from2, Opcode_ConvertFromWide opcode) {
    RegTo = to;
    RegFrom1 = from1;
    RegFrom2 = from2;
    InsnOpcode = opcode;
  }

  public DexInstruction_ConvertFromWide(Instruction insn, ParsingState parsingState) throws InstructionParsingException {
    if (insn instanceof Instruction12x && Opcode_ConvertFromWide.convert(insn.opcode) != null) {

      val insnConvert = (Instruction12x) insn;
      RegTo = parsingState.getRegister(insnConvert.getRegisterA());
      RegFrom1 = parsingState.getRegister(insnConvert.getRegisterB());
      RegFrom2 = parsingState.getRegister(insnConvert.getRegisterB() + 1);
      InsnOpcode = Opcode_ConvertFromWide.convert(insn.opcode);

    } else
      throw new InstructionParsingException("Unknown instruction format or opcode");
  }

  @Override
  public String getOriginalAssembly() {
    return InsnOpcode.getAssemblyName() + " v" + RegTo.getId() + ", v" + RegFrom1.getId();
  }
}
