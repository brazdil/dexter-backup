package uk.ac.cam.db538.dexter.dex.code.insn;

import org.jf.dexlib.Code.Instruction;

import uk.ac.cam.db538.dexter.dex.code.DexCode;

import lombok.Getter;

public class DexInstruction_Unknown extends DexInstruction {

  @Getter private final String opcode;

  public DexInstruction_Unknown(DexCode methodCode, Instruction insn) {
    super(methodCode);
    opcode = insn.opcode.name();
  }

  @Override
  public String getOriginalAssembly() {
    return "??? " + opcode;
  }
}
