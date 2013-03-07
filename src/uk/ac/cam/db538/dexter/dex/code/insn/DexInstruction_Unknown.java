package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.Map;

import lombok.Getter;

import org.jf.dexlib.Code.Instruction;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;

public class DexInstruction_Unknown extends DexInstruction {

  @Getter private final String opcode;

  public DexInstruction_Unknown(DexCode methodCode, Instruction insn) {
    super(methodCode);
    opcode = insn.opcode.name();
    System.out.println("Unknown instruction in " + getParentClass().getType().getPrettyName() +
                       "..." + getParentMethod().getName());
  }

  @Override
  public String getOriginalAssembly() {
    return "??? " + opcode;
  }

  @Override
  protected DexCodeElement gcReplaceWithTemporaries(Map<DexRegister, DexRegister> mapping, boolean toRefs, boolean toDefs) {
    return this;
  }
}
