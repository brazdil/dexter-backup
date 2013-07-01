package uk.ac.cam.db538.dexter.dex.code.insn.macro;

import java.util.List;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction;

public abstract class DexMacro extends DexInstruction {

  public DexMacro(DexCode methodCode) {
    super(methodCode);
  }

  @Override
  public String getOriginalAssembly() {
    throw new UnsupportedOperationException("Pseudoinstructions should not be displayed");
  }

  public abstract List<DexCodeElement> unwrap();
}
