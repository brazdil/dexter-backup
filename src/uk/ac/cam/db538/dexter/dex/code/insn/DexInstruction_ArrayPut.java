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
import uk.ac.cam.db538.dexter.dex.code.insn.pseudo.DexPseudoinstruction_GetObjectTaint;
import uk.ac.cam.db538.dexter.dex.code.insn.pseudo.DexPseudoinstruction_SetObjectTaint;
import uk.ac.cam.db538.dexter.dex.type.UnknownTypeException;

public class DexInstruction_ArrayPut extends DexInstruction {

  @Getter private final DexRegister regFrom;
  @Getter private final DexRegister regArray;
  @Getter private final DexRegister regIndex;
  @Getter private final Opcode_GetPut opcode;

  public DexInstruction_ArrayPut(DexCode methodCode, DexRegister from, DexRegister array, DexRegister index, Opcode_GetPut opcode) {
    super(methodCode);

    this.regFrom = from;
    this.regArray = array;
    this.regIndex = index;
    this.opcode = opcode;
  }

  public DexInstruction_ArrayPut(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException, UnknownTypeException {
    super(methodCode);

    if (insn instanceof Instruction23x && Opcode_GetPut.convert_APUT(insn.opcode) != null) {

      val insnStaticPut = (Instruction23x) insn;
      regFrom = parsingState.getRegister(insnStaticPut.getRegisterA());
      regArray = parsingState.getRegister(insnStaticPut.getRegisterB());
      regIndex = parsingState.getRegister(insnStaticPut.getRegisterC());
      opcode = Opcode_GetPut.convert_APUT(insn.opcode);

    } else
      throw FORMAT_EXCEPTION;
  }

  @Override
  public String getOriginalAssembly() {
    return "aput-" + opcode.getAssemblyName() + " " + regFrom.getOriginalIndexString() + ", {" + regArray.getOriginalIndexString() + "}[" + regIndex.getOriginalIndexString() + "]";
  }

  @Override
  public Set<DexRegister> lvaReferencedRegisters() {
    return createSet(regFrom, regArray, regIndex);
  }

  @Override
  public gcRegType gcReferencedRegisterType(DexRegister reg) {
    if (reg.equals(regArray))
      return gcRegType.Object;
    else if (reg.equals(regIndex))
      return gcRegType.PrimitiveSingle;
    else if (reg.equals(regFrom))
      return (opcode == Opcode_GetPut.Object) ? gcRegType.Object : gcRegType.PrimitiveSingle;
    else
      return super.gcReferencedRegisterType(reg);
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
             new GcRangeConstraint(regFrom, ColorRange.RANGE_8BIT),
             new GcRangeConstraint(regArray, ColorRange.RANGE_8BIT),
             new GcRangeConstraint(regIndex, ColorRange.RANGE_8BIT));
  }

  @Override
  protected DexCodeElement gcReplaceWithTemporaries(Map<DexRegister, DexRegister> mapping, boolean toRefs, boolean toDefs) {
    val newFrom = (toRefs) ? mapping.get(regFrom) : regFrom;
    val newArray = (toRefs) ? mapping.get(regArray) : regArray;
    val newIndex = (toRefs) ? mapping.get(regIndex) : regIndex;
    return new DexInstruction_ArrayPut(getMethodCode(), newFrom, newArray, newIndex, opcode);
  }

  @Override
  public Instruction[] assembleBytecode(DexCode_AssemblingState state) {
    val regAlloc = state.getRegisterAllocation();
    int rFrom = regAlloc.get(regFrom);
    int rArray = regAlloc.get(regArray);
    int rIndex = regAlloc.get(regIndex);

    if (fitsIntoBits_Unsigned(rFrom, 8) && fitsIntoBits_Unsigned(rArray, 8) && fitsIntoBits_Unsigned(rIndex, 8)) {
      return new Instruction[] {
               new Instruction23x(Opcode_GetPut.convert_APUT(opcode), (short) rFrom, (short) rArray, (short) rIndex)
             };
    } else
      return throwNoSuitableFormatFound();
  }

  @Override
  public void instrument(DexCode_InstrumentationState state) {
    // primitives should copy the the taint to the array object
    // all types should copy the taint of the index to the array object
    val code = getMethodCode();
    val regTotalTaint = state.getTaintRegister(regArray);
    if (opcode != Opcode_GetPut.Object) {
      code.replace(this,
                   new DexCodeElement[] {
                     this,
                     new DexInstruction_BinaryOp(code, regTotalTaint, state.getTaintRegister(regFrom), state.getTaintRegister(regIndex), Opcode_BinaryOp.OrInt),
                     new DexPseudoinstruction_SetObjectTaint(code, regArray, regTotalTaint)
                   });
    } else {
      code.replace(this,
                   new DexCodeElement[] {
                     this,
                     new DexPseudoinstruction_GetObjectTaint(code, state.getTaintRegister(regFrom), regFrom),
                     new DexInstruction_BinaryOp(code, regTotalTaint, state.getTaintRegister(regFrom), state.getTaintRegister(regIndex), Opcode_BinaryOp.OrInt),
                     new DexPseudoinstruction_SetObjectTaint(code, regArray, state.getTaintRegister(regFrom))
                   });
    }
  }
}
