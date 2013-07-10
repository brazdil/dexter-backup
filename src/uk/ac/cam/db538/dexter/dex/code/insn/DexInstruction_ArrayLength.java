package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction12x;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.insn.macro.DexMacro_GetObjectTaint;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.UnknownTypeException;

public class DexInstruction_ArrayLength extends DexInstruction {

  @Getter private final DexRegister regTo;
  @Getter private final DexRegister regArray;

  public DexInstruction_ArrayLength(DexCode methodCode, DexRegister to, DexRegister array) {
    super(methodCode);

    this.regTo = to;
    this.regArray = array;
  }

  public DexInstruction_ArrayLength(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException, UnknownTypeException {
    super(methodCode);

    if (insn instanceof Instruction12x && insn.opcode == Opcode.ARRAY_LENGTH) {

      val insnInstanceOf = (Instruction12x) insn;
      regTo = parsingState.getRegister(insnInstanceOf.getRegisterA());
      regArray = parsingState.getRegister(insnInstanceOf.getRegisterB());

    } else
      throw FORMAT_EXCEPTION;
  }

  @Override
  public String getOriginalAssembly() {
    return "array-length " + regTo.getOriginalIndexString() + ", {" + regArray.getOriginalIndexString() + "}";
  }

  @Override
  public Set<DexRegister> lvaDefinedRegisters() {
    return createSet(regTo);
  }

  @Override
  public Set<DexRegister> lvaReferencedRegisters() {
    return createSet(regArray);
  }

  @Override
  public void instrument(DexCode_InstrumentationState state) {
    // length needs to carry the taint of the array object
    val code = getMethodCode();
    code.replace(this,
                 new DexCodeElement[] {
                   new DexMacro_GetObjectTaint(code, state.getTaintRegister(regTo), regArray),
                   this
                 });
  }

  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }
  
  @Override
  protected DexClassType[] throwsExceptions() {
	return getParentFile().getTypeCache().LIST_Error_NullPointerException;
  }
  
}
