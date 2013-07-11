package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.FieldIdItem;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction22c;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.field.DexField;
import uk.ac.cam.db538.dexter.dex.field.DexStaticField;
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

    if (field instanceof DexStaticField)
      throw new InstructionArgumentException("Expected instance field");

    this.regTo1 = to1;
    this.regTo2 = to2;
    this.regObject = obj;
    this.fieldClass = field.getParentClass().getClassDef().getType();
    this.fieldType = field.getFieldDef().getFieldId().getType();
    this.fieldName = field.getFieldDef().getFieldId().getName();

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
  public void instrument(DexCode_InstrumentationState state) {
//    // same as regular InstanceGet, just need to copy the taint to the second result register as well
//    val code = getMethodCode();
//    val classHierarchy = getParentFile().getHierarchy();
//
//    val regValueTaint = state.getTaintRegister(regTo1);
//    val defClass = classHierarchy.getClassDefinition(fieldClass);
//    val defField = defClass.getAccessedInstanceField(new DexFieldId(fieldName, fieldType));
//
//    if (defField == null)
//      System.err.println("warning: cannot find accessed instance field " + fieldClass.getPrettyName() + "." + fieldName);
//
//    val fieldDeclaringClass = defField.getParentClass();
//    if (fieldDeclaringClass.isInternal()) {
//      // FIELD OF PRIMITIVE TYPE DEFINED INTERNALLY
//      // combine the taint stored in adjoined field with the taint of the object
//      val field = DexUtils.getInstanceField(getParentFile(), fieldDeclaringClass.getType(), fieldName, fieldType);
//      val regObjectTaint = (regTo1 == regObject) ? new DexRegister() : state.getTaintRegister(regObject);
//      code.replace(this,
//                   new DexCodeElement[] {
//                     new DexMacro_GetObjectTaint(code, regObjectTaint, regObject),
//                     new DexInstruction_InstanceGet(code, regValueTaint, regObject, state.getCache().getTaintField(field)),
//                     new DexInstruction_BinaryOp(code, regValueTaint, regValueTaint, regObjectTaint, Opcode_BinaryOp.OrInt),
//                     this
//                   });
//
//    } else
//      // FIELD OF PRIMITIVE TYPE DEFINED EXTERNALLY
//      // assign the same taint as the containing object has
//      code.replace(this,
//                   new DexCodeElement[] {
//                     new DexMacro_GetObjectTaint(code, regValueTaint, regObject),
//                     this
//                   });
  }

  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }

  @Override
  protected DexClassType[] throwsExceptions() {
	return getParentFile().getTypeCache().LIST_Error_NullPointerException;
  }
  
}
