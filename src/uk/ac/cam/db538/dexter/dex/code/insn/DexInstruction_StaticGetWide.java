package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.FieldIdItem;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction21c;

import uk.ac.cam.db538.dexter.dex.DexField;
import uk.ac.cam.db538.dexter.dex.DexStaticField;
import uk.ac.cam.db538.dexter.dex.DexUtils;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexFieldId;
import uk.ac.cam.db538.dexter.dex.type.DexPrimitiveType;
import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;
import uk.ac.cam.db538.dexter.dex.type.UnknownTypeException;

public class DexInstruction_StaticGetWide extends DexInstruction {

  @Getter private final DexRegister regTo1;
  @Getter private final DexRegister regTo2;
  @Getter private final DexClassType fieldClass;
  @Getter private final DexRegisterType fieldType;
  @Getter private final String fieldName;

  public DexInstruction_StaticGetWide(DexCode methodCode, DexRegister to1, DexRegister to2, DexClassType fieldClass, DexRegisterType fieldType, String fieldName) {
    super(methodCode);

    this.regTo1 = to1;
    this.regTo2 = to2;
    this.fieldClass = fieldClass;
    this.fieldType = fieldType;
    this.fieldName = fieldName;

    Opcode_GetPutWide.checkTypeIsWide(this.fieldType);
  }

  public DexInstruction_StaticGetWide(DexCode methodCode, DexRegister to1, DexRegister to2, DexField field) {
    super(methodCode);

    if (!(field instanceof DexStaticField))
      throw new InstructionArgumentException("Expected static field");

    this.regTo1 = to1;
    this.regTo2 = to2;
    this.fieldClass = field.getParentClass().getClassDef().getType();
    this.fieldType = field.getFieldDef().getFieldId().getType();
    this.fieldName = field.getFieldDef().getFieldId().getName();

    Opcode_GetPutWide.checkTypeIsWide(this.fieldType);
  }

  public DexInstruction_StaticGetWide(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException, UnknownTypeException {
    super(methodCode);

    if (insn instanceof Instruction21c && insn.opcode == Opcode.SGET_WIDE) {

      val insnStaticGet = (Instruction21c) insn;
      val refItem = (FieldIdItem) insnStaticGet.getReferencedItem();
      regTo1 = parsingState.getRegister(insnStaticGet.getRegisterA());
      regTo2 = parsingState.getRegister(insnStaticGet.getRegisterA() + 1);
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
    return "sget-wide " + regTo1.getOriginalIndexString() + "|" + regTo2.getOriginalIndexString()
           + ", " + fieldClass.getPrettyName() + "." + fieldName;
  }

  @Override
  public Set<DexRegister> lvaDefinedRegisters() {
    return createSet(regTo1, regTo2);
  }

  @Override
  public void instrument(DexCode_InstrumentationState state) {
    val code = getMethodCode();
    val classHierarchy = getParentFile().getHierarchy();

    val defClass = classHierarchy.getBaseClassDefinition(fieldClass);
    val defField = defClass.getAccessedStaticField(new DexFieldId(fieldName, fieldType));

    if (defField == null)
      System.err.println("warning: cannot find accessed static field " + fieldClass.getPrettyName() + "." + fieldName);

    val fieldDeclaringClass = defField.getParentClass();

    if (fieldDeclaringClass.isInternal()) {
      // FIELD OF PRIMITIVE TYPE DEFINED INTERNALLY
      // retrieve taint from the adjoined field
      val field = DexUtils.getField(getParentFile(), fieldDeclaringClass.getType(), fieldName, fieldType);
      code.replace(this,
                   new DexCodeElement[] {
                     this,
                     new DexInstruction_StaticGet(code, state.getTaintRegister(regTo1), state.getCache().getTaintField(field))
                   });
    } else {
      // FIELD OF PRIMITIVE TYPE DEFINED EXTERNALLY
      // get the taint from adjoined field in special global class
      code.replace(this,
                   new DexCodeElement[] {
                     this,
                     new DexInstruction_StaticGet(
                       code,
                       state.getTaintRegister(regTo1),
                       state.getCache().getTaintField_ExternalStatic(fieldClass, (DexPrimitiveType) fieldType, fieldName))
                   });
    }
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
