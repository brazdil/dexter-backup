package uk.ac.cam.db538.dexter.dex.code;

import org.jf.dexlib.TypeIdItem;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction21c;

import uk.ac.cam.db538.dexter.dex.type.DexReferenceType;
import uk.ac.cam.db538.dexter.dex.type.UnknownTypeException;

import lombok.Getter;
import lombok.val;

public class DexInstruction_ConstClass extends DexInstruction {

  @Getter private final DexRegister RegTo;
  @Getter private final DexReferenceType Value;

  public DexInstruction_ConstClass(DexRegister to, DexReferenceType value) {
    RegTo = to;
    Value = value;
  }

  public DexInstruction_ConstClass(Instruction insn, InstructionParsingState parsingState) throws DexInstructionParsingException, UnknownTypeException {
    if (insn instanceof Instruction21c && insn.opcode == Opcode.CONST_CLASS) {

      val insnConstClass = (Instruction21c) insn;
      RegTo = parsingState.getRegister(insnConstClass.getRegisterA());
      Value = DexReferenceType.parse(
                ((TypeIdItem) insnConstClass.getReferencedItem()).getTypeDescriptor(),
                parsingState.getCache());

    } else
      throw new DexInstructionParsingException("Unknown instruction format or opcode");
  }

  @Override
  public String getOriginalAssembly() {
    return "const-class v" + RegTo.getId() + ", " + Value.getDescriptor();
  }
}
