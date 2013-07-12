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

public class DexInstruction_ArrayGet extends DexInstruction {

  @Getter private final DexRegister regTo;
  @Getter private final DexRegister regArray;
  @Getter private final DexRegister regIndex;
  @Getter private final Opcode_GetPut opcode;

  public DexInstruction_ArrayGet(DexCode methodCode, DexRegister to, DexRegister array, DexRegister index, Opcode_GetPut opcode) {
    super(methodCode);

    this.regTo = to;
    this.regArray = array;
    this.regIndex = index;
    this.opcode = opcode;
  }

  public DexInstruction_ArrayGet(DexCode methodCode, Instruction insn, CodeParserState parsingState) throws InstructionParseError, UnknownTypeException {
    super(methodCode);

    if (insn instanceof Instruction23x && Opcode_GetPut.convert_AGET(insn.opcode) != null) {

      val insnArrayGet = (Instruction23x) insn;
      regTo = parsingState.getRegister(insnArrayGet.getRegisterA());
      regArray = parsingState.getRegister(insnArrayGet.getRegisterB());
      regIndex = parsingState.getRegister(insnArrayGet.getRegisterC());
      opcode = Opcode_GetPut.convert_AGET(insn.opcode);

    } else
      throw FORMAT_EXCEPTION;
  }

  @Override
  public String getOriginalAssembly() {
    return "aget-" + opcode.getAssemblyName() + " " + regTo.getOriginalIndexString() + ", {" + regArray.getOriginalIndexString() + "}[" + regIndex.getOriginalIndexString() + "]";
  }

  @Override
  public Set<? extends uk.ac.cam.db538.dexter.dex.code.reg.DexRegister> lvaReferencedRegisters() {
    return createSet(regArray, regIndex);
  }
  @Override
  public Set<? extends uk.ac.cam.db538.dexter.dex.code.reg.DexRegister> lvaDefinedRegisters() {
    return createSet(regTo);
  }

  @Override
  public void instrument(DexCode_InstrumentationState state) {
    // need to combine the taint of the array object and the index
    val code = getMethodCode();
    val regArrayTaint = (regTo == regArray) ? new DexRegister() : state.getTaintRegister(regArray);
    if (opcode != Opcode_GetPut.Object) {
      code.replace(this,
                   new DexCodeElement[] {
                     new DexMacro_GetObjectTaint(code, regArrayTaint, regArray),
                     this,
                     new DexInstruction_BinaryOp(code, state.getTaintRegister(regTo), regArrayTaint, state.getTaintRegister(regIndex), Opcode_BinaryOp.OrInt)
                   });
    } else {
      val regTotalTaint = new DexRegister();
      code.replace(this,
                   new DexCodeElement[] {
                     new DexMacro_GetObjectTaint(code, regArrayTaint, regArray),
                     new DexInstruction_BinaryOp(code, regTotalTaint, regArrayTaint, state.getTaintRegister(regIndex), Opcode_BinaryOp.OrInt),
                     this,
                     new DexMacro_SetObjectTaint(code, regTo, regTotalTaint)
                   });
    }
  }

  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }
  
  @Override
  protected DexClassType[] throwsExceptions() {
	return getParentFile().getTypeCache().LIST_Error_Null_ArrayIndexOutOfBounds;
  }
  
}
