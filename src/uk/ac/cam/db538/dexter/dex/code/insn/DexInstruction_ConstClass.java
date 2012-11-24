package uk.ac.cam.db538.dexter.dex.code.insn;

import org.jf.dexlib.TypeIdItem;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction21c;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.type.DexReferenceType;
import uk.ac.cam.db538.dexter.dex.type.UnknownTypeException;

import lombok.Getter;
import lombok.val;

public class DexInstruction_ConstClass extends DexInstruction {

  @Getter private final DexRegister RegTo;
  @Getter private final DexReferenceType Value;

  public DexInstruction_ConstClass(DexCode methodCode, DexRegister to, DexReferenceType value) {
    super(methodCode);

    RegTo = to;
    Value = value;
  }

  public DexInstruction_ConstClass(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException, UnknownTypeException {
    super(methodCode);

    if (insn instanceof Instruction21c && insn.opcode == Opcode.CONST_CLASS) {

      val insnConstClass = (Instruction21c) insn;
      RegTo = parsingState.getRegister(insnConstClass.getRegisterA());
      Value = DexReferenceType.parse(
                ((TypeIdItem) insnConstClass.getReferencedItem()).getTypeDescriptor(),
                parsingState.getCache());

    } else
      throw new InstructionParsingException("Unknown instruction format or opcode");
  }

  @Override
  public String getOriginalAssembly() {
    return "const-class v" + RegTo.getOriginalIndexString() + ", " + Value.getDescriptor();
  }
}
