package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.TypeIdItem;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction21c;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.insn.macro.DexMacro_GetObjectTaint;
import uk.ac.cam.db538.dexter.dex.code.insn.macro.DexMacro_SetObjectTaint;
import uk.ac.cam.db538.dexter.dex.type.DexType_Class;
import uk.ac.cam.db538.dexter.dex.type.DexType_Reference;
import uk.ac.cam.db538.dexter.dex.type.UnknownTypeException;

public class DexInstruction_CheckCast extends DexInstruction {

  @Getter private final DexRegister regObject;
  @Getter private final DexType_Reference value;

  // CAREFUL: likely to throw exception

  public DexInstruction_CheckCast(DexCode methodCode, DexRegister object, DexType_Reference value) {
    super(methodCode);

    this.regObject = object;
    this.value = value;
  }

  public DexInstruction_CheckCast(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException, UnknownTypeException {
    super(methodCode);

    if (insn instanceof Instruction21c && insn.opcode == Opcode.CHECK_CAST) {

      val insnCheckCast = (Instruction21c) insn;
      this.regObject = parsingState.getRegister(insnCheckCast.getRegisterA());
      this.value = DexType_Reference.parse(
                     ((TypeIdItem) insnCheckCast.getReferencedItem()).getTypeDescriptor(),
                     parsingState.getCache());

    } else
      throw FORMAT_EXCEPTION;
  }

  @Override
  public String getOriginalAssembly() {
    return "check-cast " + regObject.getOriginalIndexString() + ", " + value.getDescriptor();
  }

  @Override
  public Set<DexRegister> lvaReferencedRegisters() {
    return createSet(regObject);
  }

  @Override
  public Set<DexRegister> lvaDefinedRegisters() {
    // it defines it, because the object gets its type changed inside the VM
    return createSet(regObject);
  }

  @Override
  public void instrument(DexCode_InstrumentationState state) {
    val code = getMethodCode();

    val regException = new DexRegister();
    val regTaint = new DexRegister();
    val getObjTaint = new DexMacro_GetObjectTaint(code, regTaint, this.regObject);
    val setExTaint = new DexMacro_SetObjectTaint(code, regException, regTaint);

    code.replace(this, throwingInsn_GenerateSurroundingCatchBlock(
                   new DexCodeElement[] { this },
                   new DexCodeElement[] { getObjTaint, setExTaint },
                   regException));
  }

  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }

  @Override
  protected DexType_Class[] throwsExceptions() {
	return getParentFile().getParsingCache().LIST_Error_ClassCastException;
  }
  
}
