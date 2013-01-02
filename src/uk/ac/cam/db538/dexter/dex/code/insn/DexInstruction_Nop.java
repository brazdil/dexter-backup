package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction10x;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_AssemblingState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;

public class DexInstruction_Nop extends DexInstruction {

  @Getter @Setter private boolean forcedAssembly;

  public DexInstruction_Nop(DexCode methodCode) {
    super(methodCode);
    this.forcedAssembly = true;
  }

  public DexInstruction_Nop(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) {
    super(methodCode);

    if (!(insn instanceof Instruction10x) || insn.opcode != Opcode.NOP)
      throw FORMAT_EXCEPTION;

    this.forcedAssembly = false;
  }

  @Override
  public String getOriginalAssembly() {
    return "nop";
  }

  @Override
  public Instruction[] assembleBytecode(DexCode_AssemblingState state) throws InstructionAssemblyException {
    if (forcedAssembly)
      return new Instruction[] { new Instruction10x(Opcode.NOP) };
    else
      return new Instruction[] { };
  }

  @Override
  public DexCodeElement[] instrument(DexCode_InstrumentationState mapping) {
    return new DexCodeElement[] { this };
  }

  @Override
  protected DexCodeElement gcReplaceWithTemporaries(Map<DexRegister, DexRegister> mapping) {
    return this;
  }
}
