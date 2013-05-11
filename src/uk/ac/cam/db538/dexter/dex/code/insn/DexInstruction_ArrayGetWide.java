package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction23x;

import uk.ac.cam.db538.dexter.analysis.coloring.ColorRange;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_AssemblingState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.insn.macro.DexMacro_GetObjectTaint;
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
  public gcRegType gcReferencedRegisterType(DexRegister reg) {
    if (reg.equals(regArray))
      return gcRegType.Object;
    else if (reg.equals(regIndex))
      return gcRegType.PrimitiveSingle;
    else
      return super.gcReferencedRegisterType(reg);
  }

  @Override
  public gcRegType gcDefinedRegisterType(DexRegister reg) {
    if (reg.equals(regTo1))
      return gcRegType.PrimitiveWide_High;
    else if (reg.equals(regTo2))
      return gcRegType.PrimitiveWide_Low;
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
  public Set<GcFollowConstraint> gcFollowConstraints() {
    return createSet(new GcFollowConstraint(regTo1, regTo2));
  }

  @Override
  public Set<GcRangeConstraint> gcRangeConstraints() {
    return createSet(
             new GcRangeConstraint(regTo1, ColorRange.RANGE_8BIT),
             new GcRangeConstraint(regArray, ColorRange.RANGE_8BIT),
             new GcRangeConstraint(regIndex, ColorRange.RANGE_8BIT));
  }

  @Override
  protected DexCodeElement gcReplaceWithTemporaries(Map<DexRegister, DexRegister> mapping, boolean toRefs, boolean toDefs) {
    val newTo1 = (toDefs) ? mapping.get(regTo1) : regTo1;
    val newTo2 = (toDefs) ? mapping.get(regTo2) : regTo2;
    val newArray = (toRefs) ? mapping.get(regArray) : regArray;
    val newIndex = (toRefs) ? mapping.get(regIndex) : regIndex;
    return new DexInstruction_ArrayGetWide(getMethodCode(), newTo1, newTo2, newArray, newIndex);
  }

  @Override
  public Instruction[] assembleBytecode(DexCode_AssemblingState state) {
    val regAlloc = state.getRegisterAllocation();
    int rTo1 = regAlloc.get(regTo1);
    int rTo2 = regAlloc.get(regTo2);
    int rArray = regAlloc.get(regArray);
    int rIndex = regAlloc.get(regIndex);

    if (!formWideRegister(rTo1, rTo2))
      return throwWideRegistersExpected();

    if (fitsIntoBits_Unsigned(rTo1, 8) && formWideRegister(rTo1, rTo2) && fitsIntoBits_Unsigned(rArray, 8) && fitsIntoBits_Unsigned(rIndex, 8)) {
      return new Instruction[] {
               new Instruction23x(Opcode.AGET_WIDE, (short) rTo1, (short) rArray, (short) rIndex)
             };
    } else
      return throwNoSuitableFormatFound();
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
}
