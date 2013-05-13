package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.FieldIdItem;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction22c;

import uk.ac.cam.db538.dexter.analysis.coloring.ColorRange;
import uk.ac.cam.db538.dexter.dex.DexField;
import uk.ac.cam.db538.dexter.dex.DexUtils;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_AssemblingState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.insn.pseudo.DexPseudoinstruction_GetObjectTaint;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;
import uk.ac.cam.db538.dexter.dex.type.UnknownTypeException;

public class DexInstruction_InstanceGetWide extends DexInstruction {

  @Getter private final DexRegister regTo1;
  @Getter private final DexRegister regTo2;
  @Getter private final DexRegister regObject;
  @Getter private final DexClassType fieldClass;
  @Getter private final DexRegisterType fieldType;
  @Getter private final String fieldName;

  public DexInstruction_InstanceGetWide(DexCode methodCode, DexRegister to1, DexRegister to2, DexRegister obj, DexClassType fieldClass, DexRegisterType fieldType, String fieldName) {
    super(methodCode);

    this.regTo1 = to1;
    this.regTo2 = to2;
    this.regObject = obj;
    this.fieldClass = fieldClass;
    this.fieldType = fieldType;
    this.fieldName = fieldName;

    Opcode_GetPutWide.checkTypeIsWide(this.fieldType);
  }

  public DexInstruction_InstanceGetWide(DexCode methodCode, DexRegister to1, DexRegister to2, DexRegister obj, DexField field) {
    super(methodCode);

    if (field.isStatic())
      throw new InstructionArgumentException("Expected instance field");

    this.regTo1 = to1;
    this.regTo2 = to2;
    this.regObject = obj;
    this.fieldClass = field.getParentClass().getType();
    this.fieldType = field.getType();
    this.fieldName = field.getName();

    Opcode_GetPutWide.checkTypeIsWide(this.fieldType);
  }

  public DexInstruction_InstanceGetWide(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException, UnknownTypeException {
    super(methodCode);

    if (insn instanceof Instruction22c && insn.opcode == Opcode.IGET_WIDE) {

      val insnStaticGet = (Instruction22c) insn;
      val refItem = (FieldIdItem) insnStaticGet.getReferencedItem();
      regTo1 = parsingState.getRegister(insnStaticGet.getRegisterA());
      regTo2 = parsingState.getRegister(insnStaticGet.getRegisterA() + 1);
      regObject = parsingState.getRegister(insnStaticGet.getRegisterB());
      fieldClass = DexClassType.parse(
                     refItem.getContainingClass().getTypeDescriptor(),
                     parsingState.getCache());
      fieldType = DexRegisterType.parse(
                    refItem.getFieldType().getTypeDescriptor(),
                    parsingState.getCache());
      fieldName = refItem.getFieldName().getStringValue();

    } else
      throw FORMAT_EXCEPTION;

    Opcode_GetPutWide.checkTypeIsWide(this.fieldType);
  }

  @Override
  public String getOriginalAssembly() {
    return "iget-wide " + regTo1.getOriginalIndexString() + "|" + regTo2.getOriginalIndexString()
           + ", {" + regObject.getOriginalIndexString() + "}" + fieldClass.getPrettyName() + "." + fieldName;
  }

  @Override
  public Set<DexRegister> lvaDefinedRegisters() {
    return createSet(regTo1, regTo2);
  }

  @Override
  public Set<DexRegister> lvaReferencedRegisters() {
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
             new GcRangeConstraint(regTo1, ColorRange.RANGE_4BIT),
             new GcRangeConstraint(regObject, ColorRange.RANGE_4BIT));
  }

  @Override
  protected DexCodeElement gcReplaceWithTemporaries(Map<DexRegister, DexRegister> mapping, boolean toRefs, boolean toDefs) {
    val newTo1 = (toDefs) ? mapping.get(regTo1) : regTo1;
    val newTo2 = (toDefs) ? mapping.get(regTo2) : regTo2;
    val newObject = (toRefs) ? mapping.get(regObject) : regObject;
    return new DexInstruction_InstanceGetWide(getMethodCode(), newTo1, newTo2, newObject, fieldClass, fieldType, fieldName);
  }

  @Override
  public Instruction[] assembleBytecode(DexCode_AssemblingState state) {
    val regAlloc = state.getRegisterAllocation();
    int rTo1 = regAlloc.get(regTo1);
    int rTo2 = regAlloc.get(regTo2);
    int rObject = regAlloc.get(regObject);

    if (fitsIntoBits_Unsigned(rTo1, 4) && rTo1 + 1 == rTo2 && fitsIntoBits_Unsigned(rObject, 4)) {
      return new Instruction[] {
               new Instruction22c(Opcode.IGET_WIDE, (byte) rTo1, (byte) rObject, state.getCache().getField(fieldClass, fieldType, fieldName))
             };
    } else
      return throwNoSuitableFormatFound();
  }

  @Override
  public void instrument(DexCode_InstrumentationState state) {
    // same as regular InstanceGet, just need to copy the taint to the second result register as well
    val code = getMethodCode();
    val classHierarchy = getParentFile().getClassHierarchy();

    val regValueTaint = state.getTaintRegister(regTo1);
    val fieldDeclaringClass = classHierarchy.getAccessedFieldDeclaringClass(fieldClass, fieldName, fieldType, false);
    if (fieldDeclaringClass.isDefinedInternally()) {
      // FIELD OF PRIMITIVE TYPE DEFINED INTERNALLY
      // combine the taint stored in adjoined field with the taint of the object
      val field = DexUtils.getField(getParentFile(), fieldDeclaringClass, fieldName, fieldType);
      val regObjectTaint = (regTo1 == regObject) ? new DexRegister() : state.getTaintRegister(regObject);
      code.replace(this,
                   new DexCodeElement[] {
                     new DexPseudoinstruction_GetObjectTaint(code, regObjectTaint, regObject),
                     new DexInstruction_InstanceGet(code, regValueTaint, regObject, state.getCache().getTaintField(field)),
                     new DexInstruction_BinaryOp(code, regValueTaint, regValueTaint, regObjectTaint, Opcode_BinaryOp.OrInt),
                     this
                   });

    } else
      // FIELD OF PRIMITIVE TYPE DEFINED EXTERNALLY
      // assign the same taint as the containing object has
      code.replace(this,
                   new DexCodeElement[] {
                     new DexPseudoinstruction_GetObjectTaint(code, regValueTaint, regObject),
                     this
                   });
  }

  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }
}
