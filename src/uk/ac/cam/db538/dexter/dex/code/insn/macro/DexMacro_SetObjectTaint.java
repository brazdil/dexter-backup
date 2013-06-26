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
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_Invoke;

public class DexMacro_SetObjectTaint extends DexMacro {

  @Getter private final DexRegister regObject;
  @Getter private final DexRegister regTaint;

  public DexMacro_SetObjectTaint(DexCode methodCode, DexRegister regObject, DexRegister regTaint) {
    super(methodCode);

    this.regObject = regObject;
    this.regTaint = regTaint;
  }

  @Override
  public List<DexCodeElement> unwrap() {
    val code = getMethodCode();
    val dex = getParentFile();

    val classStorage = dex.getObjectTaintStorage_Type();
    val methodSetTaint = dex.getObjectTaintStorage_Set();

    return createList(
             (DexCodeElement) new DexInstruction_Invoke(
               code,
               classStorage,
               methodSetTaint.getName(),
               methodSetTaint.getPrototype(),
               Arrays.asList(new DexRegister[] { regObject, regTaint }),
               Opcode_Invoke.Static)
           );
  }

  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }
}
