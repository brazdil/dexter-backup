package uk.ac.cam.db538.dexter.dex.code.insn.macro;

import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import lombok.val;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstructionVisitor;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Invoke;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_MoveResult;

public class DexMacro_GetObjectTaint extends DexMacro {

  @Getter private final DexRegister regTo;
  @Getter private final DexRegister regObject;

  public DexMacro_GetObjectTaint(DexCode methodCode, DexRegister regTo, DexRegister regObject) {
    super(methodCode);

    this.regTo = regTo;
    this.regObject = regObject;
  }

  @Override
  public List<? extends DexCodeElement> unwrap() {
    val code = getMethodCode();
    val dex = getParentFile();

    val methodGetTaint = dex.getAuxiliaryDex().getMethod_TaintGet();

    return createList(
             (DexCodeElement) new DexInstruction_Invoke(
               code,
               methodGetTaint,
               Arrays.asList(new DexRegister[] { regObject })),
             (DexCodeElement) new DexInstruction_MoveResult(
               code,
               regTo,
               false));
  }

  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }
}
