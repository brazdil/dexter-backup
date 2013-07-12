package uk.ac.cam.db538.dexter.dex.code.insn;

import lombok.Getter;
import lombok.Setter;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction10x;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.CodeParserState;

public class DexInstruction_Nop extends DexInstruction {

  @Getter @Setter private boolean forcedAssembly;

  public DexInstruction_Nop(DexCode methodCode) {
    super(methodCode);
    this.forcedAssembly = true;
  }

  public DexInstruction_Nop(DexCode methodCode, Instruction insn, CodeParserState parsingState) {
    super(methodCode);

    if (!(insn instanceof Instruction10x) || insn.opcode != Opcode.NOP)
      throw FORMAT_EXCEPTION;

    this.forcedAssembly = false;
  }

  @Override
  public String toString() {
    return "nop";
  }

  @Override
  public void instrument(DexCode_InstrumentationState mapping) { }

  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }
}
