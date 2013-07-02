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
import uk.ac.cam.db538.dexter.dex.code.insn.macro.DexMacro_GetObjectTaint;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.UnknownTypeException;

public class DexInstruction_ArrayGetWide extends DexInstruction {

  @Getter private final DexRegister regTo1;
  @Getter private final DexRegister regTo2;
  @Getter private final DexRegister regArray;
  @Getter private final DexRegister regIndex;

  public DexInstruction_ArrayGetWide(DexCode methodCode, DexRegister to1, DexRegister to2, DexRegister array, DexRegister index) {
    super(methodCode);

    this.regTo1 = to1;
    this.regTo2 = to2;
    this.regArray = array;
    this.regIndex = index;
  }

  public DexInstruction_ArrayGetWide(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException, UnknownTypeException {
    super(methodCode);

    if (insn instanceof Instruction23x && insn.opcode == Opcode.AGET_WIDE) {

      val insnStaticGet = (Instruction23x) insn;
      regTo1 = parsingState.getRegister(insnStaticGet.getRegisterA());
      regTo2 = parsingState.getRegister(insnStaticGet.getRegisterA() + 1);
      regArray = parsingState.getRegister(insnStaticGet.getRegisterB());
      regIndex = parsingState.getRegister(insnStaticGet.getRegisterC());

    } else
      throw FORMAT_EXCEPTION;
  }

  @Override
  public String getOriginalAssembly() {
    return "aget-wide " + regTo1.getOriginalIndexString() + "|" + regTo2.getOriginalIndexString()
           + ", {" + regArray.getOriginalIndexString() + "}[" + regIndex.getOriginalIndexString() + "]";
  }

  @Override
  public Set<DexRegister> lvaReferencedRegisters() {
    return createSet(regArray, regIndex);
  }

  @Override
  public Set<DexRegister> lvaDefinedRegisters() {
    return createSet(regTo1, regTo2);
  }

  @Override
  public void instrument(DexCode_InstrumentationState state) {
    // need to combine the taint of the array object and the index
    val code = getMethodCode();
    val regArrayTaint = (regTo1 == regArray) ? new DexRegister() : state.getTaintRegister(regArray);
    code.replace(this,
                 new DexCodeElement[] {
                   new DexMacro_GetObjectTaint(code, regArrayTaint, regArray),
                   this,
                   new DexInstruction_BinaryOp(code, state.getTaintRegister(regTo1), regArrayTaint, state.getTaintRegister(regIndex), Opcode_BinaryOp.OrInt)
                 });
  }

  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }
  
  @Override
  protected DexClassType[] throwsExceptions() {
	return getParentFile().getParsingCache().LIST_Error_Null_ArrayIndexOutOfBounds;
  }
  
}
