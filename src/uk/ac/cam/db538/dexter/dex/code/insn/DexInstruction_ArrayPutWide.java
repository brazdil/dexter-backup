package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction23x;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.insn.macro.DexMacro_SetObjectTaint;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.UnknownTypeException;

public class DexInstruction_ArrayPutWide extends DexInstruction {

  @Getter private final DexRegister regFrom1;
  @Getter private final DexRegister regFrom2;
  @Getter private final DexRegister regArray;
  @Getter private final DexRegister regIndex;

  public DexInstruction_ArrayPutWide(DexCode methodCode, DexRegister from1, DexRegister from2, DexRegister array, DexRegister index) {
    super(methodCode);

    this.regFrom1 = from1;
    this.regFrom2 = from2;
    this.regArray = array;
    this.regIndex = index;
  }

  public DexInstruction_ArrayPutWide(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException, UnknownTypeException {
    super(methodCode);

    if (insn instanceof Instruction23x && insn.opcode == Opcode.APUT_WIDE) {

      val insnArrayPutWide = (Instruction23x) insn;
      regFrom1 = parsingState.getRegister(insnArrayPutWide.getRegisterA());
      regFrom2 = parsingState.getRegister(insnArrayPutWide.getRegisterA() + 1);
      regArray = parsingState.getRegister(insnArrayPutWide.getRegisterB());
      regIndex = parsingState.getRegister(insnArrayPutWide.getRegisterC());
    } else
      throw FORMAT_EXCEPTION;
  }

  @Override
  public String getOriginalAssembly() {
    return "aput-wide " + regFrom1.getOriginalIndexString() + "|" + regFrom2.getOriginalIndexString()
           + ", {" + regArray.getOriginalIndexString() + "}[" + regIndex.getOriginalIndexString() + "]";
  }

  @Override
  public Set<DexRegister> lvaReferencedRegisters() {
    return createSet(regFrom1, regFrom2, regArray, regIndex);
  }

  @Override
  public void instrument(DexCode_InstrumentationState state) {
    val code = getMethodCode();
    val regTotalTaint = state.getTaintRegister(regArray);
    code.replace(this, new DexCodeElement[] {
                   this,
                   new DexInstruction_BinaryOp(code, regTotalTaint, state.getTaintRegister(regFrom1), state.getTaintRegister(regIndex), Opcode_BinaryOp.OrInt),
                   new DexMacro_SetObjectTaint(code, regArray, regTotalTaint)
                 });
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
