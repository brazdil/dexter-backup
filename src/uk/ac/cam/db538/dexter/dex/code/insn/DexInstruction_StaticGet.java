package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.FieldIdItem;
import org.jf.dexlib.Code.Instruction;
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

public class DexInstruction_StaticGet extends DexInstruction {

  @Getter private final DexRegister regTo;
  @Getter private final DexClassType fieldClass;
  @Getter private final DexRegisterType fieldType;
  @Getter private final String fieldName;
  @Getter private final Opcode_GetPut opcode;

  public DexInstruction_StaticGet(DexCode methodCode, DexRegister to, DexClassType fieldClass, DexRegisterType fieldType, String fieldName, Opcode_GetPut opcode) {
    super(methodCode);

    this.regTo = to;
    this.fieldClass = fieldClass;
    this.fieldType = fieldType;
    this.fieldName = fieldName;
    this.opcode = opcode;

    Opcode_GetPut.checkTypeAgainstOpcode(this.fieldType, this.opcode);
  }

  public DexInstruction_StaticGet(DexCode methodCode, DexRegister to, DexField field) {
    super(methodCode);

    if (!(field instanceof DexStaticField))
      throw new InstructionArgumentException("Expected static field");

    this.regTo = to;
    this.fieldClass = field.getParentClass().getClassDef().getType();
    this.fieldType = field.getFieldDef().getFieldId().getType();
    this.fieldName = field.getFieldDef().getFieldId().getName();
    this.opcode = Opcode_GetPut.getOpcodeFromType(this.fieldType);
  }

  public DexInstruction_StaticGet(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException, UnknownTypeException {
    super(methodCode);

    if (insn instanceof Instruction21c && Opcode_GetPut.convert_SGET(insn.opcode) != null) {

      val insnStaticGet = (Instruction21c) insn;
      val refItem = (FieldIdItem) insnStaticGet.getReferencedItem();
      regTo = parsingState.getRegister(insnStaticGet.getRegisterA());
      fieldClass = DexClassType.parse(
                     refItem.getContainingClass().getTypeDescriptor(),
                     parsingState.getCache());
      fieldType = DexRegisterType.parse(
                    refItem.getFieldType().getTypeDescriptor(),
                    parsingState.getCache());
      fieldName = refItem.getFieldName().getStringValue();
      opcode = Opcode_GetPut.convert_SGET(insn.opcode);

    } else
      throw FORMAT_EXCEPTION;

    Opcode_GetPut.checkTypeAgainstOpcode(this.fieldType, this.opcode);
  }

  @Override
  public String getOriginalAssembly() {
    return "sget-" + opcode.getAssemblyName() + " " + regTo.getOriginalIndexString() + ", " + fieldClass.getPrettyName() + "." + fieldName;
  }

  @Override
  public Set<DexRegister> lvaDefinedRegisters() {
    return createSet(regTo);
  }

  @Override
  public void instrument(DexCode_InstrumentationState state) {
//    val code = getMethodCode();
//    val classHierarchy = getParentFile().getHierarchy();
//
//    if (opcode != Opcode_GetPut.Object) {
//      val defClass = classHierarchy.getBaseClassDefinition(fieldClass);
//      val defField = defClass.getAccessedStaticField(new DexFieldId(fieldName, fieldType));
//
//      if (defField == null)
//        System.err.println("warning: cannot find accessed static field " + fieldClass.getPrettyName() + "." + fieldName);
//
//      val fieldDeclaringClass = defField.getParentClass();
//      if (fieldDeclaringClass != null && fieldDeclaringClass.isInternal()) {
//        // FIELD OF PRIMITIVE TYPE DEFINED INTERNALLY
//        // retrieve taint from the adjoined field
//        val field = DexUtils.getStaticField(getParentFile(), fieldDeclaringClass.getType(), fieldName, fieldType);
//
//        code.replace(this,
//                     new DexCodeElement[] {
//                       this,
//                       new DexInstruction_StaticGet(code, state.getTaintRegister(regTo), state.getCache().getTaintField(field))
//                     });
//
//      } else {
//        // FIELD OF PRIMITIVE TYPE DEFINED EXTERNALLY
//        // OR NOT FOUND
//        // get the taint from adjoined field in special global class
//        code.replace(this,
//                     new DexCodeElement[] {
//                       this,
//                       new DexInstruction_StaticGet(
//                         code,
//                         state.getTaintRegister(regTo),
//                         state.getCache().getTaintField_ExternalStatic(fieldClass, (DexPrimitiveType) fieldType, fieldName))
//                     });
//      }
//    } else {
//      // FIELD OF REFERENCE TYPE
//      // the object itself has taint, no need to do anything
//    }
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
