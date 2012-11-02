package uk.ac.cam.db538.dexter.dex.code.insn;

import org.jf.dexlib.TypeIdItem;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction22c;

import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.reg.DexRegister;
import uk.ac.cam.db538.dexter.dex.type.DexReferenceType;
import uk.ac.cam.db538.dexter.dex.type.UnknownTypeException;

import lombok.Getter;
import lombok.val;

public class DexInstruction_InstanceOf extends DexInstruction {

  @Getter private final DexRegister RegTo;
  @Getter private final DexRegister RegFrom;
  @Getter private final DexReferenceType Value;

  // CAREFUL: likely to throw exception

  public DexInstruction_InstanceOf(DexRegister to, DexRegister from, DexReferenceType value) {
    RegTo = to;
    RegFrom = from;
    Value = value;
  }

  public DexInstruction_InstanceOf(Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException, UnknownTypeException {
    if (insn instanceof Instruction22c && insn.opcode == Opcode.INSTANCE_OF) {

      val insnInstanceOf = (Instruction22c) insn;
      RegTo = parsingState.getRegister(insnInstanceOf.getRegisterA());
      RegFrom = parsingState.getRegister(insnInstanceOf.getRegisterB());
      Value = DexReferenceType.parse(
                ((TypeIdItem) insnInstanceOf.getReferencedItem()).getTypeDescriptor(),
                parsingState.getCache());

    } else
      throw new InstructionParsingException("Unknown instruction format or opcode");
  }

  @Override
  public String getOriginalAssembly() {
    return "instance-of v" + RegTo.getId() + ", v" + RegFrom.getId() +
           ", " + Value.getDescriptor();
  }
}
