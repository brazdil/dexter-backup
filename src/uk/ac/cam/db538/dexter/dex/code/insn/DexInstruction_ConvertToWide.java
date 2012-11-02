package uk.ac.cam.db538.dexter.dex.code.insn;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Format.Instruction12x;

import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.reg.DexRegister;

import lombok.Getter;
import lombok.val;

public class DexInstruction_ConvertToWide extends DexInstruction {

  @Getter private final DexRegister RegTo1;
  @Getter private final DexRegister RegTo2;
  @Getter private final DexRegister RegFrom;
  @Getter private final Opcode_ConvertToWide InsnOpcode;

  public DexInstruction_ConvertToWide(DexRegister to1, DexRegister to2, DexRegister from, Opcode_ConvertToWide opcode) {
    RegTo1 = to1;
    RegTo2 = to2;
    RegFrom = from;
    InsnOpcode = opcode;
  }

  public DexInstruction_ConvertToWide(Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException {
    if (insn instanceof Instruction12x && Opcode_ConvertToWide.convert(insn.opcode) != null) {

      val insnConvert = (Instruction12x) insn;
      RegTo1 = parsingState.getRegister(insnConvert.getRegisterA());
      RegTo2 = parsingState.getRegister(insnConvert.getRegisterA() + 1);
      RegFrom = parsingState.getRegister(insnConvert.getRegisterB());
      InsnOpcode = Opcode_ConvertToWide.convert(insn.opcode);

    } else
      throw new InstructionParsingException("Unknown instruction format or opcode");
  }

  @Override
  public String getOriginalAssembly() {
    return InsnOpcode.getAssemblyName() + " v" + RegTo1.getId() + ", v" + RegFrom.getId();
  }
}
