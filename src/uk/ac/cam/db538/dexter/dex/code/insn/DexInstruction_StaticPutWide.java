package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.FieldIdItem;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction21c;

import uk.ac.cam.db538.dexter.analysis.coloring.ColorRange;
import uk.ac.cam.db538.dexter.dex.DexField;
import uk.ac.cam.db538.dexter.dex.DexUtils;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_AssemblingState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexPrimitiveType;
import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;
import uk.ac.cam.db538.dexter.dex.type.UnknownTypeException;

public class DexInstruction_StaticPutWide extends DexInstruction {

  @Getter private final DexRegister regFrom1;
  @Getter private final DexRegister regFrom2;
  @Getter private final DexClassType fieldClass;
  @Getter private final DexRegisterType fieldType;
  @Getter private final String fieldName;

  public DexInstruction_StaticPutWide(DexCode methodCode, DexRegister from1, DexRegister from2, DexClassType fieldClass, DexRegisterType fieldType, String fieldName) {
    super(methodCode);

    this.regFrom1 = from1;
    this.regFrom2 = from2;
    this.fieldClass = fieldClass;
    this.fieldType = fieldType;
    this.fieldName = fieldName;

    Opcode_GetPutWide.checkTypeIsWide(this.fieldType);
  }

  public DexInstruction_StaticPutWide(DexCode methodCode, DexRegister from1, DexRegister from2, DexField field) {
    super(methodCode);

    if (!field.isStatic())
      throw new InstructionArgumentException("Expected static field");

    this.regFrom1 = from1;
    this.regFrom2 = from2;
    this.fieldClass = field.getParentClass().getType();
    this.fieldType = field.getType();
    this.fieldName = field.getName();

    Opcode_GetPutWide.checkTypeIsWide(this.fieldType);
  }

  public DexInstruction_StaticPutWide(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException, UnknownTypeException {
    super(methodCode);

    if (insn instanceof Instruction21c && insn.opcode == Opcode.SPUT_WIDE) {

      val insnStaticPut = (Instruction21c) insn;
      val refItem = (FieldIdItem) insnStaticPut.getReferencedItem();
      regFrom1 = parsingState.getRegister(insnStaticPut.getRegisterA());
      regFrom2 = parsingState.getRegister(insnStaticPut.getRegisterA() + 1);
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
    return "sput-wide " + regFrom1.getOriginalIndexString() + "|" + regFrom2.getOriginalIndexString()
           + ", " + fieldClass.getPrettyName() + "." + fieldName;
  }

  @Override
  public Set<DexRegister> lvaReferencedRegisters() {
    return createSet(regFrom1, regFrom2);
  }

  @Override
  public gcRegType gcReferencedRegisterType(DexRegister reg) {
    if (reg.equals(regFrom1))
      return gcRegType.PrimitiveWide_High;
    else if (reg.equals(regFrom2))
      return gcRegType.PrimitiveWide_Low;
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
  public Set<GcFollowConstraint> gcFollowConstraints() {
    return createSet(new GcFollowConstraint(regFrom1, regFrom2));
  }

  @Override
  public Set<GcRangeConstraint> gcRangeConstraints() {
    return createSet(new GcRangeConstraint(regFrom1, ColorRange.RANGE_8BIT));
  }

  @Override
  protected DexCodeElement gcReplaceWithTemporaries(Map<DexRegister, DexRegister> mapping, boolean toRefs, boolean toDefs) {
    val newFrom1 = (toRefs) ? mapping.get(regFrom1) : regFrom1;
    val newFrom2 = (toRefs) ? mapping.get(regFrom2) : regFrom2;
    return new DexInstruction_StaticPutWide(getMethodCode(), newFrom1, newFrom2, fieldClass, fieldType, fieldName);
  }

  @Override
  public Instruction[] assembleBytecode(DexCode_AssemblingState state) {
    val regAlloc = state.getRegisterAllocation();
    int rTo1 = regAlloc.get(regFrom1);
    int rTo2 = regAlloc.get(regFrom2);

    if (!formWideRegister(rTo1, rTo2))
      return throwWideRegistersExpected();

    if (fitsIntoBits_Unsigned(rTo1, 8)) {
      return new Instruction[] {
               new Instruction21c(Opcode.SPUT_WIDE, (short) rTo1, state.getCache().getField(fieldClass, fieldType, fieldName))
             };
    } else
      return throwNoSuitableFormatFound();
  }

  @Override
  public void instrument(DexCode_InstrumentationState state) {
    val code = getMethodCode();
    val classHierarchy = getParentFile().getClassHierarchy();

    val fieldDeclaringClass = classHierarchy.getAccessedFieldDeclaringClass(fieldClass, fieldName, fieldType, true);

    if (fieldDeclaringClass.isDefinedInternally()) {
      // FIELD OF PRIMITIVE TYPE DEFINED INTERNALLY
      // store the taint to the taint field
      val field = DexUtils.getField(getParentFile(), fieldDeclaringClass, fieldName, fieldType);
      code.replace(this,
                   new DexCodeElement[] {
                     this,
                     new DexInstruction_StaticPut(code, state.getTaintRegister(regFrom1), state.getCache().getTaintField(field)),
                   });

    } else
      // FIELD OF PRIMITIVE TYPE DEFINED EXTERNALLY
      // store the taint to the adjoined field in special global class
      code.replace(this,
                   new DexCodeElement[] {
                     this,
                     new DexInstruction_StaticPut(
                       code,
                       state.getTaintRegister(regFrom1),
                       state.getCache().getTaintField_ExternalStatic(fieldClass, (DexPrimitiveType) fieldType, fieldName))
                   });
  }

  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }
  
  @Override
  protected DexClassType[] throwsExceptions() {
	return getParentFile().getParsingCache().LIST_Error;
  }
  
}
