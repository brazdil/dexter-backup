package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction12x;

import uk.ac.cam.db538.dexter.analysis.coloring.ColorRange;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_AssemblingState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.insn.pseudo.DexPseudoinstruction_GetObjectTaint;
import uk.ac.cam.db538.dexter.dex.type.UnknownTypeException;

public class DexInstruction_ArrayLength extends DexInstruction {

  @Getter private final DexRegister regTo;
  @Getter private final DexRegister regArray;

  public DexInstruction_ArrayLength(DexCode methodCode, DexRegister to, DexRegister array) {
    super(methodCode);

    this.regTo = to;
    this.regArray = array;
  }

  public DexInstruction_ArrayLength(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException, UnknownTypeException {
    super(methodCode);

    if (insn instanceof Instruction12x && insn.opcode == Opcode.ARRAY_LENGTH) {

      val insnInstanceOf = (Instruction12x) insn;
      regTo = parsingState.getRegister(insnInstanceOf.getRegisterA());
      regArray = parsingState.getRegister(insnInstanceOf.getRegisterB());

    } else
      throw FORMAT_EXCEPTION;
  }

  @Override
  public String getOriginalAssembly() {
    return "array-length " + regTo.getOriginalIndexString() + ", {" + regArray.getOriginalIndexString() + "}";
  }

  @Override
  public Set<GcRangeConstraint> gcRangeConstraints() {
    return createSet(
             new GcRangeConstraint(regTo, ColorRange.RANGE_4BIT),
             new GcRangeConstraint(regArray, ColorRange.RANGE_4BIT));
  }

  @Override
  protected DexCodeElement gcReplaceWithTemporaries(Map<DexRegister, DexRegister> mapping, boolean toRefs, boolean toDefs) {
    val newTo = (toDefs) ? mapping.get(regTo) : regTo;
    val newArray = (toRefs) ? mapping.get(regArray) : regArray;
    return new DexInstruction_ArrayLength(getMethodCode(), newTo, newArray);
  }

  @Override
  public Set<DexRegister> lvaDefinedRegisters() {
    return createSet(regTo);
  }

  @Override
  public Set<DexRegister> lvaReferencedRegisters() {
    return createSet(regArray);
  }

  @Override
  protected gcRegType gcReferencedRegisterType(DexRegister reg) {
    if (reg.equals(regArray))
      return gcRegType.Object;
    else
      return super.gcReferencedRegisterType(reg);
  }

  @Override
  protected gcRegType gcDefinedRegisterType(DexRegister reg) {
    if (reg.equals(regTo))
      return gcRegType.PrimitiveSingle;
    else
      return super.gcDefinedRegisterType(reg);
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

  @Override
  public void instrument(DexCode_InstrumentationState state) {
    // length needs to carry the taint of the array object
    val code = getMethodCode();
    code.replace(this,
                 new DexCodeElement[] {
                   new DexPseudoinstruction_GetObjectTaint(code, state.getTaintRegister(regTo), regArray),
                   this
                 });
  }

  @Override
  public Instruction[] assembleBytecode(DexCode_AssemblingState state) {
    val regAlloc = state.getRegisterAllocation();
    int rTo = regAlloc.get(regTo);
    int rArray = regAlloc.get(regArray);

    if (fitsIntoBits_Unsigned(rTo, 4) && fitsIntoBits_Unsigned(rArray, 4))
      return new Instruction[] { new Instruction12x(Opcode.ARRAY_LENGTH, (byte) rTo, (byte) rArray) };
    else
      return throwNoSuitableFormatFound();
  }
}
