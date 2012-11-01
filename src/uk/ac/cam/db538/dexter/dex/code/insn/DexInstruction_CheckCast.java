package uk.ac.cam.db538.dexter.dex.code.insn;

import org.jf.dexlib.TypeIdItem;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction21c;

import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.type.DexReferenceType;
import uk.ac.cam.db538.dexter.dex.type.UnknownTypeException;

import lombok.Getter;
import lombok.val;

public class DexInstruction_CheckCast extends DexInstruction {

  @Getter private final DexRegister RegTo;
  @Getter private final DexReferenceType Value;

  // CAREFUL: likely to throw exception

  public DexInstruction_CheckCast(DexRegister to, DexReferenceType value) {
    RegTo = to;
    Value = value;
  }

  public DexInstruction_CheckCast(Instruction insn, InstructionParsingState parsingState) throws DexInstructionParsingException, UnknownTypeException {
    if (insn instanceof Instruction21c && insn.opcode == Opcode.CHECK_CAST) {

      val insnCheckCast = (Instruction21c) insn;
      RegTo = parsingState.getRegister(insnCheckCast.getRegisterA());
      Value = DexReferenceType.parse(
                ((TypeIdItem) insnCheckCast.getReferencedItem()).getTypeDescriptor(),
                parsingState.getCache());

    } else
      throw new DexInstructionParsingException("Unknown instruction format or opcode");
  }

  @Override
  public String getOriginalAssembly() {
    return "check-cast v" + RegTo.getId() + ", " + Value.getDescriptor();
  }
}