package uk.ac.cam.db538.dexter.dex.code.insn;

import org.jf.dexlib.TypeIdItem;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction22c;

import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.type.DexArrayType;
import uk.ac.cam.db538.dexter.dex.type.UnknownTypeException;

import lombok.Getter;
import lombok.val;

public class DexInstruction_NewArray extends DexInstruction {

  @Getter private final DexRegister RegTo;
  @Getter private final DexRegister RegSize;
  @Getter private final DexArrayType Value;

  public DexInstruction_NewArray(DexRegister to, DexRegister size, DexArrayType value) {
    RegTo = to;
    RegSize = size;
    Value = value;
  }

  public DexInstruction_NewArray(Instruction insn, ParsingState parsingState) throws InstructionParsingException, UnknownTypeException {
    if (insn instanceof Instruction22c && insn.opcode == Opcode.NEW_ARRAY) {

      val insnNewArray = (Instruction22c) insn;
      RegTo = parsingState.getRegister(insnNewArray.getRegisterA());
      RegSize = parsingState.getRegister(insnNewArray.getRegisterB());
      Value = DexArrayType.parse(
                ((TypeIdItem) insnNewArray.getReferencedItem()).getTypeDescriptor(),
                parsingState.getCache());

    } else
      throw new InstructionParsingException("Unknown instruction format or opcode");
  }

  @Override
  public String getOriginalAssembly() {
    return "new-array v" + RegTo.getId() + ", v" + RegSize.getId() +
           ", " + Value.getDescriptor();
  }
}
