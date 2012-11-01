package uk.ac.cam.db538.dexter.dex.code;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction10t;

import lombok.Getter;
import lombok.val;

public class DexInstruction_Goto extends DexInstruction {

  @Getter private final DexLabel Target;

  public DexInstruction_Goto(DexLabel target) {
    Target = target;
  }

//  public DexInstruction_Goto(Instruction insn, InstructionParsingState parsingState) throws DexInstructionParsingException {
//	    if ( insn instanceof Instruction10t && insn.opcode == Opcode.GOTO) {
//
//	      val insnGoto = (Instruction10t) insn;
//	      Target = parsingState.getLabel(insnGoto.getTargetAddressOffset());
//
//	    } else
//	      throw new DexInstructionParsingException("Unknown instruction format or opcode");
//	  }

  @Override
  public String getOriginalAssembly() {
    return "goto L" + Target.getOriginalOffset();
  }
}
