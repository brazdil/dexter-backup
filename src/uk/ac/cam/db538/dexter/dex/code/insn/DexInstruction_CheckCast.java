package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.TypeIdItem;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction21c;

import uk.ac.cam.db538.dexter.dex.code.CodeParserState;
import uk.ac.cam.db538.dexter.dex.code.reg.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.reg.DexSingleRegister;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexReferenceType;
import uk.ac.cam.db538.dexter.hierarchy.RuntimeHierarchy;

import com.google.common.collect.Sets;

public class DexInstruction_CheckCast extends DexInstruction {

  @Getter private final DexSingleRegister regObject;
  @Getter private final DexReferenceType value;

  public DexInstruction_CheckCast(DexSingleRegister object, DexReferenceType value, RuntimeHierarchy hierarchy) {
    super(hierarchy);

    this.regObject = object;
    this.value = value;
  }

  public static DexInstruction_CheckCast parse(Instruction insn, CodeParserState parsingState) {
    if (insn instanceof Instruction21c && insn.opcode == Opcode.CHECK_CAST) {

      val hierarchy = parsingState.getHierarchy();
    	
      val insnCheckCast = (Instruction21c) insn;
      return new DexInstruction_CheckCast(
    		  parsingState.getSingleRegister(insnCheckCast.getRegisterA()),
    		  DexReferenceType.parse(
                     ((TypeIdItem) insnCheckCast.getReferencedItem()).getTypeDescriptor(),
                     hierarchy.getTypeCache()),
              hierarchy);

    } else
      throw FORMAT_EXCEPTION;
  }

  @Override
  public String toString() {
    return "check-cast " + regObject.toString() + ", " + value.getDescriptor();
  }

  @Override
  public Set<? extends DexRegister> lvaReferencedRegisters() {
    return Sets.newHashSet(regObject);
  }

  @Override
  public Set<? extends DexRegister> lvaDefinedRegisters() {
    // it defines it, because the object gets its type changed inside the VM
    return Sets.newHashSet(regObject);
  }

  @Override
  public void instrument() {
//    val code = getMethodCode();
//
//    val regException = new DexRegister();
//    val regTaint = new DexRegister();
//    val getObjTaint = new DexMacro_GetObjectTaint(code, regTaint, this.regObject);
//    val setExTaint = new DexMacro_SetObjectTaint(code, regException, regTaint);
//
//    code.replace(this, throwingInsn_GenerateSurroundingCatchBlock(
//                   new DexCodeElement[] { this },
//                   new DexCodeElement[] { getObjTaint, setExTaint },
//                   regException));
  }

  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }

  @Override
  protected DexClassType[] throwsExceptions() {
	return this.hierarchy.getTypeCache().LIST_Error_ClassCastException;
  }
  
}
