package uk.ac.cam.db538.dexter.dex.code.insn;

import org.apache.commons.lang3.StringEscapeUtils;
import org.jf.dexlib.StringIdItem;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction21c;
import org.jf.dexlib.Code.Format.Instruction31c;

import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.DexStringConstant;

import lombok.Getter;
import lombok.val;

public class DexInstruction_ConstString extends DexInstruction {

  @Getter private final DexRegister RegTo;
  @Getter private final DexStringConstant StringConstant;

  // CAREFUL: need to produce the Jumbo instruction if
  //          the resulting StringDataItem has more than 16-bit id

  public DexInstruction_ConstString(DexRegister to, DexStringConstant value) {
    RegTo = to;
    StringConstant = value;
  }

  public DexInstruction_ConstString(Instruction insn, ParsingState parsingState) throws InstructionParsingException {
    if (insn instanceof Instruction21c && insn.opcode == Opcode.CONST_STRING) {

      val insnConstString = (Instruction21c) insn;
      RegTo = parsingState.getRegister(insnConstString.getRegisterA());
      StringConstant = DexStringConstant.parse(((StringIdItem) insnConstString.getReferencedItem()), parsingState.getCache());

    } else if (insn instanceof Instruction31c && insn.opcode == Opcode.CONST_STRING_JUMBO) {

      val insnConstStringJumbo = (Instruction31c) insn;
      RegTo = parsingState.getRegister(insnConstStringJumbo.getRegisterA());
      StringConstant = DexStringConstant.parse(((StringIdItem) insnConstStringJumbo.getReferencedItem()), parsingState.getCache());

    } else
      throw new InstructionParsingException("Unknown instruction format or opcode");
  }

  @Override
  public String getOriginalAssembly() {
    String escapedVal = StringEscapeUtils.escapeJava(StringConstant.getValue());
    if (escapedVal.length() > 15)
      escapedVal = escapedVal.substring(0, 15) + "...";
    return "const-string v" + RegTo.getId() + ", \"" + escapedVal + "\"";
  }
}
