package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.TypeIdItem;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction22c;

import uk.ac.cam.db538.dexter.analysis.coloring.ColorRange;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_AssemblingState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.insn.pseudo.DexPseudoinstruction_GetObjectTaint;
import uk.ac.cam.db538.dexter.dex.type.DexReferenceType;
import uk.ac.cam.db538.dexter.dex.type.UnknownTypeException;

public class DexInstruction_InstanceOf extends DexInstruction {

  @Getter private final DexRegister regTo;
  @Getter private final DexRegister regObject;
  @Getter private final DexReferenceType value;

  // CAREFUL: likely to throw exception

  public DexInstruction_InstanceOf(DexCode methodCode, DexRegister to, DexRegister object, DexReferenceType value) {
    super(methodCode);

    this.regTo = to;
    this.regObject = object;
    this.value = value;
  }

  public DexInstruction_InstanceOf(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException, UnknownTypeException {
    super(methodCode);

    if (insn instanceof Instruction22c && insn.opcode == Opcode.INSTANCE_OF) {

      val insnInstanceOf = (Instruction22c) insn;
      regTo = parsingState.getRegister(insnInstanceOf.getRegisterA());
      regObject = parsingState.getRegister(insnInstanceOf.getRegisterB());
      value = DexReferenceType.parse(
                ((TypeIdItem) insnInstanceOf.getReferencedItem()).getTypeDescriptor(),
                parsingState.getCache());

    } else
      throw FORMAT_EXCEPTION;
  }

  @Override
  public String getOriginalAssembly() {
    return "instance-of " + regTo.getOriginalIndexString() + ", " + regObject.getOriginalIndexString() +
           ", " + value.getDescriptor();
  }

  @Override
  public Set<GcRangeConstraint> gcRangeConstraints() {
    return createSet(
             new GcRangeConstraint(regTo, ColorRange.RANGE_4BIT),
             new GcRangeConstraint(regObject, ColorRange.RANGE_4BIT));
  }

  @Override
  protected DexCodeElement gcReplaceWithTemporaries(Map<DexRegister, DexRegister> mapping) {
    return new DexInstruction_InstanceOf(
             getMethodCode(),
             mapping.get(regTo),
             mapping.get(regObject),
             value);
  }

  @Override
  public Set<DexRegister> lvaDefinedRegisters() {
    return createSet(regTo);
  }

  @Override
  public Set<DexRegister> lvaReferencedRegisters() {
    return createSet(regObject);
  }

  @Override
  public void instrument(DexCode_InstrumentationState state) {
    // copy the taint across
    val code = getMethodCode();
    code.replace(this,
                 new DexCodeElement[] {
                   new DexPseudoinstruction_GetObjectTaint(code, state.getTaintRegister(regTo), regObject),
                   this
                 });
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
  public Instruction[] assembleBytecode(DexCode_AssemblingState state) {
    val regAlloc = state.getRegisterAllocation();
    int rTo = regAlloc.get(regTo);
    int rObject = regAlloc.get(regObject);

    if (fitsIntoBits_Unsigned(rTo, 4) && fitsIntoBits_Unsigned(rObject, 4))
      return new Instruction[] { new Instruction22c(Opcode.INSTANCE_OF, (byte) rTo, (byte) rObject, state.getCache().getType(value)) };
    else
      return throwNoSuitableFormatFound();
  }
}
