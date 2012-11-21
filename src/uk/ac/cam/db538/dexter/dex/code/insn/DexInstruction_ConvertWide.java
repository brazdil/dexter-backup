package uk.ac.cam.db538.dexter.dex.code.insn;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Format.Instruction12x;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;

import lombok.Getter;
import lombok.val;

public class DexInstruction_ConvertWide extends DexInstruction {

  @Getter private final DexRegister RegTo1;
  @Getter private final DexRegister RegTo2;
  @Getter private final DexRegister RegFrom1;
  @Getter private final DexRegister RegFrom2;
  @Getter private final Opcode_ConvertWide InsnOpcode;

  public DexInstruction_ConvertWide(DexCode methodCode, DexRegister to1, DexRegister to2, DexRegister from1, DexRegister from2, Opcode_ConvertWide opcode) {
    super(methodCode);

    RegTo1 = to1;
    RegTo2 = to2;
    RegFrom1 = from1;
    RegFrom2 = from2;
    InsnOpcode = opcode;
  }

  public DexInstruction_ConvertWide(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException {
    super(methodCode);

    if (insn instanceof Instruction12x && Opcode_ConvertWide.convert(insn.opcode) != null) {

      val insnConvert = (Instruction12x) insn;
      RegTo1 = parsingState.getRegister(insnConvert.getRegisterA());
      RegTo2 = parsingState.getRegister(insnConvert.getRegisterA() + 1);
      RegFrom1 = parsingState.getRegister(insnConvert.getRegisterB());
      RegFrom2 = parsingState.getRegister(insnConvert.getRegisterB() + 1);
      InsnOpcode = Opcode_ConvertWide.convert(insn.opcode);

    } else
      throw new InstructionParsingException("Unknown instruction format or opcode");
  }

  @Override
  public String getOriginalAssembly() {
    return InsnOpcode.getAssemblyName() + " v" + RegTo1.getId() + ", v" + RegFrom1.getId();
  }
}
