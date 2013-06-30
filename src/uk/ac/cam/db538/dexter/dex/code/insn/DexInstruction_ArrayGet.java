package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Format.Instruction23x;

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
import uk.ac.cam.db538.dexter.dex.type.UnknownTypeException;

public class DexInstruction_ArrayGet extends DexInstruction {

  @Getter private final DexRegister regTo;
  @Getter private final DexRegister regArray;
  @Getter private final DexRegister regIndex;
  @Getter private final Opcode_GetPut opcode;

  public DexInstruction_ArrayGet(DexCode methodCode, DexRegister to, DexRegister array, DexRegister index, Opcode_GetPut opcode) {
    super(methodCode);

    this.regTo = to;
    this.regArray = array;
    this.regIndex = index;
    this.opcode = opcode;
  }

  public DexInstruction_ArrayGet(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException, UnknownTypeException {
    super(methodCode);

    if (insn instanceof Instruction23x && Opcode_GetPut.convert_AGET(insn.opcode) != null) {

      val insnArrayGet = (Instruction23x) insn;
      regTo = parsingState.getRegister(insnArrayGet.getRegisterA());
      regArray = parsingState.getRegister(insnArrayGet.getRegisterB());
      regIndex = parsingState.getRegister(insnArrayGet.getRegisterC());
      opcode = Opcode_GetPut.convert_AGET(insn.opcode);

    } else
      throw FORMAT_EXCEPTION;
  }

  @Override
  public String getOriginalAssembly() {
    return "aget-" + opcode.getAssemblyName() + " " + regTo.getOriginalIndexString() + ", {" + regArray.getOriginalIndexString() + "}[" + regIndex.getOriginalIndexString() + "]";
  }

  @Override
  public Set<DexRegister> lvaReferencedRegisters() {
    return createSet(regArray, regIndex);
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
    if (reg.equals(regTo))
      return (opcode == Opcode_GetPut.Object) ? gcRegType.Object : gcRegType.PrimitiveSingle;
    else
      return super.gcDefinedRegisterType(reg);
  }

  @Override
  public Set<DexRegister> lvaDefinedRegisters() {
    return createSet(regTo);
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
  public Set<GcRangeConstraint> gcRangeConstraints() {
    return createSet(
             new GcRangeConstraint(regTo, ColorRange.RANGE_8BIT),
             new GcRangeConstraint(regArray, ColorRange.RANGE_8BIT),
             new GcRangeConstraint(regIndex, ColorRange.RANGE_8BIT));
  }

  @Override
  protected DexCodeElement gcReplaceWithTemporaries(Map<DexRegister, DexRegister> mapping, boolean toRefs, boolean toDefs) {
    val newTo = (toDefs) ? mapping.get(regTo) : regTo;
    val newArray = (toRefs) ? mapping.get(regArray) : regArray;
    val newIndex = (toRefs) ? mapping.get(regIndex) : regIndex;
    return new DexInstruction_ArrayGet(getMethodCode(), newTo, newArray, newIndex, opcode);
  }

  @Override
  public Instruction[] assembleBytecode(DexCode_AssemblingState state) {
    val regAlloc = state.getRegisterAllocation();
    int rTo = regAlloc.get(regTo);
    int rArray = regAlloc.get(regArray);
    int rIndex = regAlloc.get(regIndex);

    if (fitsIntoBits_Unsigned(rTo, 8) && fitsIntoBits_Unsigned(rArray, 8) && fitsIntoBits_Unsigned(rIndex, 8)) {
      return new Instruction[] {
               new Instruction23x(Opcode_GetPut.convert_AGET(opcode), (short) rTo, (short) rArray, (short) rIndex)
             };
    } else
      return throwNoSuitableFormatFound();
  }

  @Override
  public void instrument(DexCode_InstrumentationState state) {
    // need to combine the taint of the array object and the index
    val code = getMethodCode();
    val regArrayTaint = (regTo == regArray) ? new DexRegister() : state.getTaintRegister(regArray);
    if (opcode != Opcode_GetPut.Object) {
      code.replace(this,
                   new DexCodeElement[] {
                     new DexMacro_GetObjectTaint(code, regArrayTaint, regArray),
                     this,
                     new DexInstruction_BinaryOp(code, state.getTaintRegister(regTo), regArrayTaint, state.getTaintRegister(regIndex), Opcode_BinaryOp.OrInt)
                   });
    } else {
      val regTotalTaint = new DexRegister();
      code.replace(this,
                   new DexCodeElement[] {
                     new DexMacro_GetObjectTaint(code, regArrayTaint, regArray),
                     new DexInstruction_BinaryOp(code, regTotalTaint, regArrayTaint, state.getTaintRegister(regIndex), Opcode_BinaryOp.OrInt),
                     this,
                     new DexMacro_SetObjectTaint(code, regTo, regTotalTaint)
                   });
    }
  }

  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }
  
  
  @Override
  protected DexClassType[] throwsExceptions() {
	return getParentFile().getParsingCache().LIST_Error_Null_ArrayIndexOutOfBounds;
  }
  
}
