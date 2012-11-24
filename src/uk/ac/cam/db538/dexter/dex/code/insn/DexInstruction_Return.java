package uk.ac.cam.db538.dexter.dex.code.insn;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction11x;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;

import lombok.Getter;
import lombok.val;

public class DexInstruction_Return extends DexInstruction {

  @Getter private final DexRegister RegFrom;
  @Getter private final boolean ObjectMoving;

  public DexInstruction_Return(DexCode methodCode, DexRegister from, boolean objectMoving) {
    super(methodCode);

    RegFrom = from;
    ObjectMoving = objectMoving;
  }

  public DexInstruction_Return(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException {
    super(methodCode);

    if (insn instanceof Instruction11x &&
        (insn.opcode == Opcode.RETURN || insn.opcode == Opcode.RETURN_OBJECT)) {

      val insnReturn = (Instruction11x) insn;
      RegFrom = parsingState.getRegister(insnReturn.getRegisterA());
      ObjectMoving = insn.opcode == Opcode.RETURN_OBJECT;

    } else
      throw new InstructionParsingException("Unknown instruction format or opcode");
  }

  @Override
  public String getOriginalAssembly() {
    return "return" + (ObjectMoving ? "-object" : "") +
           " v" + RegFrom.getOriginalIndexString();
  }
}
