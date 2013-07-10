package uk.ac.cam.db538.dexter.dex.code.insn;

import lombok.Getter;

import org.jf.dexlib.Code.Instruction;

import uk.ac.cam.db538.dexter.dex.code.DexCode;

public class DexInstruction_Unknown extends DexInstruction {

  @Getter private final String opcode;

  public DexInstruction_Unknown(DexCode methodCode, Instruction insn) {
    super(methodCode);
    opcode = insn.opcode.name();
    System.out.println("Unknown instruction in " + getParentClass().getClassDef().getType().getPrettyName() +
                       "..." + getParentMethod().getMethodDef().getMethodId().getName());
  }

  @Override
  public String getOriginalAssembly() {
    return "??? " + opcode;
  }

  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }
}
