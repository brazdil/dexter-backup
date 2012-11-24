package uk.ac.cam.db538.dexter.dex.code.insn;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Format.Instruction12x;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;

import lombok.Getter;
import lombok.val;

public class DexInstruction_UnaryOpWide extends DexInstruction {

  @Getter private final DexRegister regTo1;
  @Getter private final DexRegister regTo2;
  @Getter private final DexRegister regFrom1;
  @Getter private final DexRegister regFrom2;
  @Getter private final Opcode_UnaryOpWide insnOpcode;

  public DexInstruction_UnaryOpWide(DexCode methodCode, DexRegister to1, DexRegister to2, DexRegister from1, DexRegister from2, Opcode_UnaryOpWide opcode) {
    super(methodCode);
    regTo1 = to1;
    regTo2 = to2;
    regFrom1 = from1;
    regFrom2 = from2;
    insnOpcode = opcode;
  }

  public DexInstruction_UnaryOpWide(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException {
    super(methodCode);
    if (insn instanceof Instruction12x && Opcode_UnaryOpWide.convert(insn.opcode) != null) {

      val insnUnaryOp = (Instruction12x) insn;
      regTo1 = parsingState.getRegister(insnUnaryOp.getRegisterA());
      regTo2 = parsingState.getRegister(insnUnaryOp.getRegisterA() + 1);
      regFrom1 = parsingState.getRegister(insnUnaryOp.getRegisterB());
      regFrom2 = parsingState.getRegister(insnUnaryOp.getRegisterB() + 1);
      insnOpcode = Opcode_UnaryOpWide.convert(insn.opcode);

    } else
      throw new InstructionParsingException("Unknown instruction format or opcode");
  }

  @Override
  public String getOriginalAssembly() {
    return insnOpcode.getAssemblyName() + " v" + regTo1.getOriginalIndexString() + ", v" + regFrom1.getOriginalIndexString();
  }
}
