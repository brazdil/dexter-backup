package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction12x;

import uk.ac.cam.db538.dexter.dex.code.CodeParserState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.reg.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.reg.DexSingleRegister;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.UnknownTypeException;
import uk.ac.cam.db538.dexter.hierarchy.RuntimeHierarchy;

import com.google.common.collect.Sets;

public class DexInstruction_ArrayLength extends DexInstruction {

  @Getter private final DexSingleRegister regTo;
  @Getter private final DexSingleRegister regArray;

  public DexInstruction_ArrayLength(DexSingleRegister to, DexSingleRegister array, RuntimeHierarchy hierarchy) {
	super(hierarchy);
	
    this.regTo = to;
    this.regArray = array;
  }

  public static DexInstruction_ArrayLength parse(Instruction insn, CodeParserState parsingState) throws InstructionParseError, UnknownTypeException {
    if (insn instanceof Instruction12x && insn.opcode == Opcode.ARRAY_LENGTH) {

      val insnInstanceOf = (Instruction12x) insn;
      return new DexInstruction_ArrayLength(
    		  parsingState.getSingleRegister(insnInstanceOf.getRegisterA()),
    		  parsingState.getSingleRegister(insnInstanceOf.getRegisterB()),
    		  parsingState.getHierarchy());

    } else
      throw FORMAT_EXCEPTION;
  }

  @Override
  public String toString() {
    return "array-length " + regTo.toString() + ", {" + regArray.toString() + "}";
  }

  @Override
  public Set<? extends DexRegister> lvaDefinedRegisters() {
    return Sets.newHashSet(regTo);
  }

  @Override
  public Set<? extends DexRegister> lvaReferencedRegisters() {
    return Sets.newHashSet(regArray);
  }

  @Override
  public void instrument(DexCode_InstrumentationState state) {
//    // length needs to carry the taint of the array object
//    val code = getMethodCode();
//    code.replace(this,
//                 new DexCodeElement[] {
//                   new DexMacro_GetObjectTaint(code, state.getTaintRegister(regTo), regArray),
//                   this
//                 });
  }

  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }
  
  @Override
  protected DexClassType[] throwsExceptions() {
	return this.hierarchy.getTypeCache().LIST_Error_NullPointerException;
  }
  
}
