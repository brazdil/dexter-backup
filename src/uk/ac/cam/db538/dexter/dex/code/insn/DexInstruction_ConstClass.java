package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jf.dexlib.TypeIdItem;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction21c;

import uk.ac.cam.db538.dexter.analysis.coloring.ColorRange;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_AssemblingState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.type.DexReferenceType;
import uk.ac.cam.db538.dexter.dex.type.UnknownTypeException;

import lombok.Getter;
import lombok.val;

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
  protected DexCodeElement gcReplaceWithTemporaries(Map<DexRegister, DexRegister> mapping) {
    return new DexInstruction_ConstClass(getMethodCode(), mapping.get(regTo), value);
  }

  @Override
  public void instrument(DexCode_InstrumentationState state) {  }

  @Override
  public Instruction[] assembleBytecode(DexCode_AssemblingState state) {
    int rTo = state.getRegisterAllocation().get(regTo);

    if (fitsIntoBits_Unsigned(rTo, 8))
      return new Instruction[] {
               new Instruction21c(Opcode.CONST_CLASS, (short) rTo, state.getCache().getType(value))
             };
    else
      return throwNoSuitableFormatFound();
  }

  @Override
  public Set<DexRegister> lvaDefinedRegisters() {
    return createSet(regTo);
  }

  @Override
  public Set<GcRangeConstraint> gcRangeConstraints() {
    return createSet(new GcRangeConstraint(regTo, ColorRange.RANGE_8BIT));
  }

  @Override
  public boolean cfgExitsMethod() {
    return throwingInsn_CanExitMethod();
  }

  @Override
  public Set<DexCodeElement> cfgGetSuccessors() {
    val set = new HashSet<DexCodeElement>();
    set.add(getNextCodeElement());
    set.addAll(throwingInsn_CatchHandlers());
    return set;
  }

}
