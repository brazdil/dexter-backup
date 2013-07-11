package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Format.Instruction23x;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.CodeParserState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.insn.macro.DexMacro_GetObjectTaint;
import uk.ac.cam.db538.dexter.dex.code.insn.macro.DexMacro_SetObjectTaint;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.UnknownTypeException;

public class DexInstruction_ArrayPut extends DexInstruction {

  @Getter private final DexRegister regFrom;
  @Getter private final DexRegister regArray;
  @Getter private final DexRegister regIndex;
  @Getter private final Opcode_GetPut opcode;

  public DexInstruction_ArrayPut(DexCode methodCode, DexRegister from, DexRegister array, DexRegister index, Opcode_GetPut opcode) {
    super(methodCode);

    this.regFrom = from;
    this.regArray = array;
    this.regIndex = index;
    this.opcode = opcode;
  }

  public DexInstruction_ArrayPut(DexCode methodCode, Instruction insn, CodeParserState parsingState) throws InstructionParseError, UnknownTypeException {
    super(methodCode);

    if (insn instanceof Instruction23x && Opcode_GetPut.convert_APUT(insn.opcode) != null) {

      val insnStaticPut = (Instruction23x) insn;
      regFrom = parsingState.getRegister(insnStaticPut.getRegisterA());
      regArray = parsingState.getRegister(insnStaticPut.getRegisterB());
      regIndex = parsingState.getRegister(insnStaticPut.getRegisterC());
      opcode = Opcode_GetPut.convert_APUT(insn.opcode);

    } else
      throw FORMAT_EXCEPTION;
  }

  @Override
  public String getOriginalAssembly() {
    return "aput-" + opcode.getAssemblyName() + " " + regFrom.getOriginalIndexString() + ", {" + regArray.getOriginalIndexString() + "}[" + regIndex.getOriginalIndexString() + "]";
  }

  @Override
  public Set<DexRegister> lvaReferencedRegisters() {
    return createSet(regFrom, regArray, regIndex);
  }

  @Override
  public void instrument(DexCode_InstrumentationState state) {
    // primitives should copy the the taint to the array object
    // all types should copy the taint of the index to the array object
    val code = getMethodCode();
    val regTotalTaint = state.getTaintRegister(regArray);
    if (opcode != Opcode_GetPut.Object) {
      code.replace(this,
                   new DexCodeElement[] {
                     this,
                     new DexInstruction_BinaryOp(code, regTotalTaint, state.getTaintRegister(regFrom), state.getTaintRegister(regIndex), Opcode_BinaryOp.OrInt),
                     new DexMacro_SetObjectTaint(code, regArray, regTotalTaint)
                   });
    } else {
      code.replace(this,
                   new DexCodeElement[] {
                     this,
                     new DexMacro_GetObjectTaint(code, state.getTaintRegister(regFrom), regFrom),
                     new DexInstruction_BinaryOp(code, regTotalTaint, state.getTaintRegister(regFrom), state.getTaintRegister(regIndex), Opcode_BinaryOp.OrInt),
                     new DexMacro_SetObjectTaint(code, regArray, state.getTaintRegister(regFrom))
                   });
    }
  }

  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }
  
  @Override
  protected DexClassType[] throwsExceptions() {
	if (opcode == Opcode_GetPut.Object)
		return getParentFile().getTypeCache().LIST_Error_Null_ArrayIndex_ArrayStore;
	else
		return getParentFile().getTypeCache().LIST_Error_Null_ArrayIndexOutOfBounds;
  }
  
}
