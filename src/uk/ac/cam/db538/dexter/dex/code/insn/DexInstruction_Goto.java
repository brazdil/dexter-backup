package uk.ac.cam.db538.dexter.dex.code.insn;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction10t;
import org.jf.dexlib.Code.Format.Instruction20t;
import org.jf.dexlib.Code.Format.Instruction30t;

import uk.ac.cam.db538.dexter.dex.code.DexLabel;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;

import lombok.Getter;

public class DexInstruction_Goto extends DexInstruction {

  @Getter private final DexLabel Target;

  public DexInstruction_Goto(DexLabel target) {
    Target = target;
  }

  public DexInstruction_Goto(Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException {
    long targetOffset;
    if ( insn instanceof Instruction10t && insn.opcode == Opcode.GOTO) {
      targetOffset = ((Instruction10t) insn).getTargetAddressOffset();
    } else if ( insn instanceof Instruction20t && insn.opcode == Opcode.GOTO_16) {
      targetOffset = ((Instruction20t) insn).getTargetAddressOffset();
    } else if ( insn instanceof Instruction30t && insn.opcode == Opcode.GOTO_32) {
      targetOffset = ((Instruction30t) insn).getTargetAddressOffset();
    } else
      throw new InstructionParsingException("Unknown instruction format or opcode");

    Target = parsingState.getLabel(targetOffset);
  }

  @Override
  public String getOriginalAssembly() {
    return "goto L" + Target.getOriginalAbsoluteOffset();
  }
}
