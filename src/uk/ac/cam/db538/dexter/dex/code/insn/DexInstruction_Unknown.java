package uk.ac.cam.db538.dexter.dex.code.insn;

import org.jf.dexlib.Code.Instruction;

import lombok.Getter;

public class DexInstruction_Unknown extends DexInstruction {

	@Getter private final String Opcode;
	
	public DexInstruction_Unknown(Instruction insn) {
		Opcode = insn.opcode.name();
	}
	
  @Override
  public String getOriginalAssembly() {
    return "??? " + Opcode;
  }
}
