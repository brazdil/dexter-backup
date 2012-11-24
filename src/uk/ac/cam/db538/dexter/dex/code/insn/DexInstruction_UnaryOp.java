package uk.ac.cam.db538.dexter.dex.code.insn;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Format.Instruction12x;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;

import lombok.Getter;
import lombok.val;

public class DexInstruction_UnaryOp extends DexInstruction {

  @Getter private final DexRegister RegTo;
  @Getter private final DexRegister RegFrom;
  @Getter private final Opcode_UnaryOp InsnOpcode;

  public DexInstruction_UnaryOp(DexCode methodCode, DexRegister to, DexRegister from, Opcode_UnaryOp opcode) {
    super(methodCode);
    RegTo = to;
    RegFrom = from;
    InsnOpcode = opcode;
  }

  public DexInstruction_UnaryOp(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException {
    super(methodCode);
    if (insn instanceof Instruction12x && Opcode_UnaryOp.convert(insn.opcode) != null) {

      val insnUnaryOp = (Instruction12x) insn;
      RegTo = parsingState.getRegister(insnUnaryOp.getRegisterA());
      RegFrom = parsingState.getRegister(insnUnaryOp.getRegisterB());
      InsnOpcode = Opcode_UnaryOp.convert(insn.opcode);

    } else
      throw new InstructionParsingException("Unknown instruction format or opcode");
  }

  @Override
  public String getOriginalAssembly() {
    return InsnOpcode.getAssemblyName() + " v" + RegTo.getOriginalIndexString() + ", v" + RegFrom.getOriginalIndexString();
  }
}
