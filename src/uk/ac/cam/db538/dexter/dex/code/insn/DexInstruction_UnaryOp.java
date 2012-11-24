package uk.ac.cam.db538.dexter.dex.code.insn;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Format.Instruction12x;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;

import lombok.Getter;
import lombok.val;

public class DexInstruction_UnaryOp extends DexInstruction {

  @Getter private final DexRegister regTo;
  @Getter private final DexRegister regFrom;
  @Getter private final Opcode_UnaryOp insnOpcode;

  public DexInstruction_UnaryOp(DexCode methodCode, DexRegister to, DexRegister from, Opcode_UnaryOp opcode) {
    super(methodCode);
    regTo = to;
    regFrom = from;
    insnOpcode = opcode;
  }

  public DexInstruction_UnaryOp(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException {
    super(methodCode);
    if (insn instanceof Instruction12x && Opcode_UnaryOp.convert(insn.opcode) != null) {

      val insnUnaryOp = (Instruction12x) insn;
      regTo = parsingState.getRegister(insnUnaryOp.getRegisterA());
      regFrom = parsingState.getRegister(insnUnaryOp.getRegisterB());
      insnOpcode = Opcode_UnaryOp.convert(insn.opcode);

    } else
      throw new InstructionParsingException("Unknown instruction format or opcode");
  }

  @Override
  public String getOriginalAssembly() {
    return insnOpcode.getAssemblyName() + " v" + regTo.getOriginalIndexString() + ", v" + regFrom.getOriginalIndexString();
  }
}
