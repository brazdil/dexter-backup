package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.TypeIdItem;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction21c;

import uk.ac.cam.db538.dexter.dex.code.CodeParserState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.reg.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.reg.DexSingleRegister;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexReferenceType;
import uk.ac.cam.db538.dexter.dex.type.UnknownTypeException;
import uk.ac.cam.db538.dexter.hierarchy.RuntimeHierarchy;

import com.google.common.collect.Sets;

public class DexInstruction_ConstClass extends DexInstruction {

  @Getter private final DexSingleRegister regTo;
  @Getter private final DexReferenceType value;

  public DexInstruction_ConstClass(DexSingleRegister to, DexReferenceType value, RuntimeHierarchy hierarchy) {
    super(hierarchy);

    this.regTo = to;
    this.value = value;
  }

  public DexInstruction_ConstClass(Instruction insn, CodeParserState parsingState) throws InstructionParseError, UnknownTypeException {
    super(parsingState.getHierarchy());

    if (insn instanceof Instruction21c && insn.opcode == Opcode.CONST_CLASS) {

      val insnConstClass = (Instruction21c) insn;
      regTo = parsingState.getSingleRegister(insnConstClass.getRegisterA());
      value = DexReferenceType.parse(
                ((TypeIdItem) insnConstClass.getReferencedItem()).getTypeDescriptor(),
                this.hierarchy.getTypeCache());

    } else
      throw FORMAT_EXCEPTION;
  }

  @Override
  public String toString() {
    return "const-class " + regTo.toString() + ", " + value.getDescriptor();
  }

  @Override
  public void instrument(DexCode_InstrumentationState state) {  }

  @Override
  public Set<? extends DexRegister> lvaDefinedRegisters() {
    return Sets.newHashSet(regTo);
  }

  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }
  
  @Override
  protected DexClassType[] throwsExceptions() {
	return this.hierarchy.getTypeCache().LIST_Error;
  }
}
