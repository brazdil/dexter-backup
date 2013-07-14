package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.TypeIdItem;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction22c;

import uk.ac.cam.db538.dexter.dex.code.CodeParserState;
import uk.ac.cam.db538.dexter.dex.code.reg.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.reg.DexSingleRegister;
import uk.ac.cam.db538.dexter.dex.type.DexArrayType;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.hierarchy.RuntimeHierarchy;

import com.google.common.collect.Sets;

public class DexInstruction_NewArray extends DexInstruction {

  @Getter private final DexSingleRegister regTo;
  @Getter private final DexSingleRegister regSize;
  @Getter private final DexArrayType value;

  public DexInstruction_NewArray(DexSingleRegister to, DexSingleRegister size, DexArrayType value, RuntimeHierarchy hierarchy) {
	super(hierarchy);
    this.regTo = to;
    this.regSize = size;
    this.value = value;
  }

  public static DexInstruction_NewArray parse(Instruction insn, CodeParserState parsingState) {
    if (insn instanceof Instruction22c && insn.opcode == Opcode.NEW_ARRAY) {
    	
      val hierarchy = parsingState.getHierarchy();
    
      val insnNewArray = (Instruction22c) insn;
      return new DexInstruction_NewArray(
    		  parsingState.getSingleRegister(insnNewArray.getRegisterA()),
    		  parsingState.getSingleRegister(insnNewArray.getRegisterB()),
    		  DexArrayType.parse(
                ((TypeIdItem) insnNewArray.getReferencedItem()).getTypeDescriptor(),
                hierarchy.getTypeCache()),
              hierarchy);

    } else
      throw FORMAT_EXCEPTION;
  }

  @Override
  public String toString() {
    return "new-array " + regTo.toString() + ", [" + regSize.toString() + "], " + value.getDescriptor();
  }

  @Override
  public Set<? extends DexRegister> lvaDefinedRegisters() {
    return Sets.newHashSet(regTo);
  }

  @Override
  public Set<? extends DexRegister> lvaReferencedRegisters() {
    return Sets.newHashSet(regSize);
  }

  @Override
  public void instrument() { }

  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }
  
  @Override
  protected DexClassType[] throwsExceptions() {
	return this.hierarchy.getTypeCache().LIST_Error_NegativeArraySizeException;
  }
  
}
