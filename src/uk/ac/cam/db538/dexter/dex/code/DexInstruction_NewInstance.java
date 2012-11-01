package uk.ac.cam.db538.dexter.dex.code;

import org.jf.dexlib.TypeIdItem;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction21c;

import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.UnknownTypeException;

import lombok.Getter;
import lombok.val;

public class DexInstruction_NewInstance extends DexInstruction {

  @Getter private final DexRegister RegTo;
  @Getter private final DexClassType Value;

  public DexInstruction_NewInstance(DexRegister to, DexClassType value) {
    RegTo = to;
    Value = value;
  }

  public DexInstruction_NewInstance(Instruction insn, InstructionParsingState parsingState) throws DexInstructionParsingException, UnknownTypeException {
    if (insn instanceof Instruction21c && insn.opcode == Opcode.NEW_INSTANCE) {

      val insnNewInstance = (Instruction21c) insn;
      RegTo = parsingState.getRegister(insnNewInstance.getRegisterA());
      Value = DexClassType.parse(
                ((TypeIdItem) insnNewInstance.getReferencedItem()).getTypeDescriptor(),
                parsingState.getCache());

    } else
      throw new DexInstructionParsingException("Unknown instruction format or opcode");
  }

  @Override
  public String getOriginalAssembly() {
    return "new-instance v" + RegTo.getId() + ", " + Value.getDescriptor();
  }
}
