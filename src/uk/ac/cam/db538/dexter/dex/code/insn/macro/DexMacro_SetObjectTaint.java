package uk.ac.cam.db538.dexter.dex.code.insn.macro;

import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import lombok.val;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.elem.DexLabel;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstructionVisitor;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_IfTestZero;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Invoke;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_IfTestZero;

public class DexMacro_SetObjectTaint extends DexMacro {

  @Getter private final DexRegister regObject;
  @Getter private final DexRegister regTaint;

  public DexMacro_SetObjectTaint(DexCode methodCode, DexRegister regObject, DexRegister regTaint) {
    super(methodCode);

    this.regObject = regObject;
    this.regTaint = regTaint;
  }

  @Override
  public List<? extends DexCodeElement> unwrap() {
    val code = getMethodCode();
    val dex = getParentFile();

    val methodSetTaint = dex.getAuxiliaryDex().getMethod_TaintSet();
    
    val labelAfter = new DexLabel(code);

    return createList(
    		new DexInstruction_IfTestZero(code, regTaint, labelAfter, Opcode_IfTestZero.eqz), 
            new DexInstruction_Invoke(
               code,
               methodSetTaint,
               Arrays.asList(new DexRegister[] { regObject, regTaint })),
            labelAfter);
  }

  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }
}
