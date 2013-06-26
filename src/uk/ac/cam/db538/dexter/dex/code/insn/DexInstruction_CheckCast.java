package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.val;

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
import uk.ac.cam.db538.dexter.dex.code.insn.macro.DexMacro_SetObjectTaint;
import uk.ac.cam.db538.dexter.dex.code.insn.macro.DexMacro_GetObjectTaint;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexReferenceType;
import uk.ac.cam.db538.dexter.dex.type.UnknownTypeException;

public class DexInstruction_CheckCast extends DexInstruction {

  @Getter private final DexRegister regObject;
  @Getter private final DexReferenceType value;

  private final DexClassType classCastException;

  // CAREFUL: likely to throw exception

  public DexInstruction_CheckCast(DexCode methodCode, DexRegister object, DexReferenceType value) {
    super(methodCode);

    this.regObject = object;
    this.value = value;

    this.classCastException = DexClassType.parse("Ljava/lang/ClassCastException;", getParentFile().getParsingCache());
  }

  public DexInstruction_CheckCast(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException, UnknownTypeException {
    super(methodCode);

    if (insn instanceof Instruction21c && insn.opcode == Opcode.CHECK_CAST) {

      val insnCheckCast = (Instruction21c) insn;
      this.regObject = parsingState.getRegister(insnCheckCast.getRegisterA());
      this.value = DexReferenceType.parse(
                     ((TypeIdItem) insnCheckCast.getReferencedItem()).getTypeDescriptor(),
                     parsingState.getCache());

      this.classCastException = DexClassType.parse("Ljava/lang/ClassCastException;", parsingState.getCache());

    } else
      throw FORMAT_EXCEPTION;
  }

  @Override
  public String getOriginalAssembly() {
    return "check-cast " + regObject.getOriginalIndexString() + ", " + value.getDescriptor();
  }

  @Override
  protected DexCodeElement gcReplaceWithTemporaries(Map<DexRegister, DexRegister> mapping, boolean toRefs, boolean toDefs) {
    val newObject = (toDefs || toRefs) ? mapping.get(regObject) : regObject;
    return new DexInstruction_CheckCast(getMethodCode(), newObject, value);
  }

  @Override
  public Instruction[] assembleBytecode(DexCode_AssemblingState state) {
    int rObj = state.getRegisterAllocation().get(regObject);

    if (fitsIntoBits_Unsigned(rObj, 8))
      return new Instruction[] { new Instruction21c(Opcode.CHECK_CAST, (short) rObj, state.getCache().getType(value)) };
    else
      return throwNoSuitableFormatFound();
  }

  @Override
  public boolean cfgExitsMethod() {
    return throwingInsn_CanExitMethod(classCastException);
  }

  @Override
  public boolean cfgEndsBasicBlock() {
    return true;
  }

  @Override
  public Set<DexCodeElement> cfgGetSuccessors() {
    val set = new HashSet<DexCodeElement>();
    set.add(getNextCodeElement());
    set.addAll(throwingInsn_CatchHandlers(classCastException));
    return set;
  }

  @Override
  public Set<DexRegister> lvaReferencedRegisters() {
    return createSet(regObject);
  }

  @Override
  public Set<DexRegister> lvaDefinedRegisters() {
    // it defines it, because the object gets its type changed inside the VM
    return createSet(regObject);
  }

  @Override
  public gcRegType gcReferencedRegisterType(DexRegister reg) {
    if (reg.equals(regObject))
      return gcRegType.Object;
    else
      return super.gcReferencedRegisterType(reg);
  }

  @Override
  public gcRegType gcDefinedRegisterType(DexRegister reg) {
    if (reg.equals(regObject))
      return gcRegType.Object;
    else
      return super.gcDefinedRegisterType(reg);
  }

  @Override
  public Set<GcRangeConstraint> gcRangeConstraints() {
    return createSet(new GcRangeConstraint(regObject, ColorRange.RANGE_8BIT));
  }

  @Override
  public void instrument(DexCode_InstrumentationState state) {
    val code = getMethodCode();

    val regException = new DexRegister();
    val regTaint = new DexRegister();
    val getObjTaint = new DexMacro_GetObjectTaint(code, regTaint, this.regObject);
    val setExTaint = new DexMacro_SetObjectTaint(code, regException, regTaint);

    code.replace(this, throwingInsn_GenerateSurroundingCatchBlock(
                   new DexCodeElement[] { this },
                   new DexCodeElement[] { getObjTaint, setExTaint },
                   regException));
  }

  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }
}
