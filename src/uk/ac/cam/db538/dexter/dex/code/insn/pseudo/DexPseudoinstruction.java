package uk.ac.cam.db538.dexter.dex.code.insn.pseudo;

import java.util.List;
import java.util.Map;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction;

public abstract class DexPseudoinstruction extends DexInstruction {

  public DexPseudoinstruction(DexCode methodCode) {
    super(methodCode);
  }

  @Override
  public String getOriginalAssembly() {
    throw new UnsupportedOperationException("Pseudoinstructions should not be displayed");
  }

  @Override
  public DexCodeElement gcReplaceWithTemporaries(Map<DexRegister, DexRegister> mapping, boolean toRefs, boolean toDefs) {
    throw new UnsupportedOperationException();
  }

  public abstract List<DexCodeElement> unwrap();
}
