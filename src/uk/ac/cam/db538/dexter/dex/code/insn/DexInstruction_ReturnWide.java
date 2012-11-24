package uk.ac.cam.db538.dexter.dex.code.insn;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction11x;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;

import lombok.Getter;
import lombok.val;

public class DexInstruction_ReturnWide extends DexInstruction {

  @Getter private final DexRegister RegFrom1;
  @Getter private final DexRegister RegFrom2;

  public DexInstruction_ReturnWide(DexCode methodCode, DexRegister from1, DexRegister from2) {
    super(methodCode);
    RegFrom1 = from1;
    RegFrom2 = from2;
  }

  public DexInstruction_ReturnWide(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException {
    super(methodCode);

    if (insn instanceof Instruction11x && insn.opcode == Opcode.RETURN_WIDE) {

      val insnReturnWide = (Instruction11x) insn;
      RegFrom1 = parsingState.getRegister(insnReturnWide.getRegisterA());
      RegFrom2 = parsingState.getRegister(insnReturnWide.getRegisterA() + 1);

    } else
      throw new InstructionParsingException("Unknown instruction format or opcode");
  }

  @Override
  public String getOriginalAssembly() {
    return "return-wide v" + RegFrom1.getOriginalIndexString();
  }
}
