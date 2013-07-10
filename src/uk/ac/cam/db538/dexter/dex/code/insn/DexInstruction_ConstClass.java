package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.TypeIdItem;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction21c;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexReferenceType;
import uk.ac.cam.db538.dexter.dex.type.UnknownTypeException;

public class DexInstruction_ConstClass extends DexInstruction {

  @Getter private final DexRegister regTo;
  @Getter private final DexReferenceType value;

  public DexInstruction_ConstClass(DexCode methodCode, DexRegister to, DexReferenceType value) {
    super(methodCode);

    this.regTo = to;
    this.value = value;
  }

  public DexInstruction_ConstClass(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException, UnknownTypeException {
    super(methodCode);

    if (insn instanceof Instruction21c && insn.opcode == Opcode.CONST_CLASS) {

      val insnConstClass = (Instruction21c) insn;
      regTo = parsingState.getRegister(insnConstClass.getRegisterA());
      value = DexReferenceType.parse(
                ((TypeIdItem) insnConstClass.getReferencedItem()).getTypeDescriptor(),
                parsingState.getCache());

    } else
      throw FORMAT_EXCEPTION;
  }

  @Override
  public String getOriginalAssembly() {
    return "const-class " + regTo.getOriginalIndexString() + ", " + value.getDescriptor();
  }

  @Override
  public void instrument(DexCode_InstrumentationState state) {  }

  @Override
  public Set<DexRegister> lvaDefinedRegisters() {
    return createSet(regTo);
  }

  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }
  
  @Override
  protected DexClassType[] throwsExceptions() {
	return getParentFile().getTypeCache().LIST_Error;
  }
  
}
