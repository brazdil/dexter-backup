package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.TypeIdItem;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction21c;

import com.google.common.collect.Sets;

import uk.ac.cam.db538.dexter.dex.code.CodeParserState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.reg.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.reg.DexSingleRegister;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.hierarchy.RuntimeHierarchy;

public class DexInstruction_NewInstance extends DexInstruction {

  @Getter private final DexSingleRegister regTo;
  @Getter private final DexClassType value;

  public DexInstruction_NewInstance(DexSingleRegister to, DexClassType value, RuntimeHierarchy hierarchy) {
	super(hierarchy);
    this.regTo = to;
    this.value = value;
  }

  public static DexInstruction_NewInstance parse(Instruction insn, CodeParserState parsingState) {
    if (insn instanceof Instruction21c && insn.opcode == Opcode.NEW_INSTANCE) {

      val hierarchy = parsingState.getHierarchy();
    	
      val insnNewInstance = (Instruction21c) insn;
      return new DexInstruction_NewInstance(
    		  parsingState.getSingleRegister(insnNewInstance.getRegisterA()),
    		  DexClassType.parse(
                ((TypeIdItem) insnNewInstance.getReferencedItem()).getTypeDescriptor(),
                hierarchy.getTypeCache()),
    		  hierarchy);

    } else
      throw FORMAT_EXCEPTION;
  }

  @Override
  public String toString() {
    return "new-instance " + regTo.toString() + ", " + value.getDescriptor();
  }

  @Override
  public Set<? extends DexRegister> lvaDefinedRegisters() {
    return Sets.newHashSet(regTo);
  }

  @Override
  public void instrument(DexCode_InstrumentationState state) { }

  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }
  
  @Override
  protected DexClassType[] throwsExceptions() {
	return this.hierarchy.getTypeCache().LIST_Error;
  }
  
}
