package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.FieldIdItem;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction21c;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.field.DexField;
import uk.ac.cam.db538.dexter.dex.field.DexStaticField;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
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

    if (!(field instanceof DexStaticField))
      throw new InstructionArgumentException("Expected static field");

    this.regFrom1 = from1;
    this.regFrom2 = from2;
    this.fieldClass = field.getParentClass().getClassDef().getType();
    this.fieldType = field.getFieldDef().getFieldId().getType();
    this.fieldName = field.getFieldDef().getFieldId().getName();

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
  public void instrument(DexCode_InstrumentationState state) {
//    val code = getMethodCode();
//    val classHierarchy = getParentFile().getHierarchy();
//
//    val defClass = classHierarchy.getBaseClassDefinition(fieldClass);
//    val defField = defClass.getAccessedStaticField(new DexFieldId(fieldName, fieldType));
//
//    if (defField == null)
//      System.err.println("warning: cannot find accessed static field " + fieldClass.getPrettyName() + "." + fieldName);
//
//    val fieldDeclaringClass = defField.getParentClass();
//
//    if (fieldDeclaringClass.isInternal()) {
//      // FIELD OF PRIMITIVE TYPE DEFINED INTERNALLY
//      // store the taint to the taint field
//      val field = DexUtils.getStaticField(getParentFile(), fieldDeclaringClass.getType(), fieldName, fieldType);
//      code.replace(this,
//                   new DexCodeElement[] {
//                     this,
//                     new DexInstruction_StaticPut(code, state.getTaintRegister(regFrom1), state.getCache().getTaintField(field)),
//                   });
//
//    } else
//      // FIELD OF PRIMITIVE TYPE DEFINED EXTERNALLY
//      // store the taint to the adjoined field in special global class
//      code.replace(this,
//                   new DexCodeElement[] {
//                     this,
//                     new DexInstruction_StaticPut(
//                       code,
//                       state.getTaintRegister(regFrom1),
//                       state.getCache().getTaintField_ExternalStatic(fieldClass, (DexPrimitiveType) fieldType, fieldName))
//                   });
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
