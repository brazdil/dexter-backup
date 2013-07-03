package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.FieldIdItem;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction21c;

import uk.ac.cam.db538.dexter.dex.DexField;
import uk.ac.cam.db538.dexter.dex.DexUtils;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.type.DexType_Class;
import uk.ac.cam.db538.dexter.dex.type.DexType_Primitive;
import uk.ac.cam.db538.dexter.dex.type.DexType_Register;
import uk.ac.cam.db538.dexter.dex.type.UnknownTypeException;

public class DexInstruction_StaticGetWide extends DexInstruction {

  @Getter private final DexRegister regTo1;
  @Getter private final DexRegister regTo2;
  @Getter private final DexType_Class fieldClass;
  @Getter private final DexType_Register fieldType;
  @Getter private final String fieldName;

  public DexInstruction_StaticGetWide(DexCode methodCode, DexRegister to1, DexRegister to2, DexType_Class fieldClass, DexType_Register fieldType, String fieldName) {
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

    if (!field.isStatic())
      throw new InstructionArgumentException("Expected static field");

    this.regTo1 = to1;
    this.regTo2 = to2;
    this.fieldClass = field.getParentClass().getType();
    this.fieldType = field.getType();
    this.fieldName = field.getName();

    Opcode_GetPutWide.checkTypeIsWide(this.fieldType);
  }

  public DexInstruction_StaticGetWide(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException, UnknownTypeException {
    super(methodCode);

    if (insn instanceof Instruction21c && insn.opcode == Opcode.SGET_WIDE) {

      val insnStaticGet = (Instruction21c) insn;
      val refItem = (FieldIdItem) insnStaticGet.getReferencedItem();
      regTo1 = parsingState.getRegister(insnStaticGet.getRegisterA());
      regTo2 = parsingState.getRegister(insnStaticGet.getRegisterA() + 1);
      fieldClass = DexType_Class.parse(
                     refItem.getContainingClass().getTypeDescriptor(),
                     parsingState.getCache());
      fieldType = DexType_Register.parse(
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
//    val code = getMethodCode();
//    val classHierarchy = getParentFile().getClassHierarchy();
//
//    val fieldDeclaringClass = classHierarchy.getAccessedFieldDeclaringClass(fieldClass, fieldName, fieldType, true);
//
//    if (fieldDeclaringClass.isDefinedInternally()) {
//      // FIELD OF PRIMITIVE TYPE DEFINED INTERNALLY
//      // retrieve taint from the adjoined field
//      val field = DexUtils.getField(getParentFile(), fieldDeclaringClass, fieldName, fieldType);
//      code.replace(this,
//                   new DexCodeElement[] {
//                     this,
//                     new DexInstruction_StaticGet(code, state.getTaintRegister(regTo1), state.getCache().getTaintField(field))
//                   });
//    } else {
//      // FIELD OF PRIMITIVE TYPE DEFINED EXTERNALLY
//      // get the taint from adjoined field in special global class
//      code.replace(this,
//                   new DexCodeElement[] {
//                     this,
//                     new DexInstruction_StaticGet(
//                       code,
//                       state.getTaintRegister(regTo1),
//                       state.getCache().getTaintField_ExternalStatic(fieldClass, (DexType_Primitive) fieldType, fieldName))
//                   });
//    }
  }

  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }
  
  @Override
  protected DexType_Class[] throwsExceptions() {
	return getParentFile().getParsingCache().LIST_Error;
  }
  
}
