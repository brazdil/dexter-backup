package uk.ac.cam.db538.dexter.dex.code.insn;

import org.apache.commons.lang3.StringEscapeUtils;
import org.jf.dexlib.StringIdItem;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction21c;
import org.jf.dexlib.Code.Format.Instruction31c;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;

import lombok.Getter;
import lombok.val;

public class DexInstruction_ConstString extends DexInstruction {

  @Getter private final DexRegister regTo;
  @Getter private final String stringConstant;

  // CAREFUL: need to produce the Jumbo instruction if
  //          the resulting StringDataItem has more than 16-bit id

  public DexInstruction_ConstString(DexCode methodCode, DexRegister to, String value) {
    super(methodCode);

    regTo = to;
    stringConstant = value;
  }

  public DexInstruction_ConstString(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException {
    super(methodCode);

    if (insn instanceof Instruction21c && insn.opcode == Opcode.CONST_STRING) {

      val insnConstString = (Instruction21c) insn;
      regTo = parsingState.getRegister(insnConstString.getRegisterA());
      stringConstant = ((StringIdItem) insnConstString.getReferencedItem()).getStringValue();

    } else if (insn instanceof Instruction31c && insn.opcode == Opcode.CONST_STRING_JUMBO) {

      val insnConstStringJumbo = (Instruction31c) insn;
      regTo = parsingState.getRegister(insnConstStringJumbo.getRegisterA());
      stringConstant = ((StringIdItem) insnConstStringJumbo.getReferencedItem()).getStringValue();

    } else
      throw new InstructionParsingException("Unknown instruction format or opcode");
  }

  @Override
  public String getOriginalAssembly() {
    String escapedVal = StringEscapeUtils.escapeJava(stringConstant);
    if (escapedVal.length() > 15)
      escapedVal = escapedVal.substring(0, 15) + "...";
    return "const-string v" + regTo.getOriginalIndexString() + ", \"" + escapedVal + "\"";
  }
}
