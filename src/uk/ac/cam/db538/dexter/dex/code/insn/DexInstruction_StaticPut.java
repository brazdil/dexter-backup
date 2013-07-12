package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.FieldIdItem;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Format.Instruction21c;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.CodeParserState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.field.DexField;
import uk.ac.cam.db538.dexter.dex.field.DexStaticField;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;
import uk.ac.cam.db538.dexter.dex.type.UnknownTypeException;

public class DexInstruction_StaticPut extends DexInstruction {

  @Getter private final DexRegister regFrom;
  @Getter private final DexClassType fieldClass;
  @Getter private final DexRegisterType fieldType;
  @Getter private final String fieldName;
  @Getter private final Opcode_GetPut opcode;

  public DexInstruction_StaticPut(DexCode methodCode, DexRegister from, DexClassType fieldClass, DexRegisterType fieldType, String fieldName, Opcode_GetPut opcode) {
    super(methodCode);

    this.regFrom = from;
    this.fieldClass = fieldClass;
    this.fieldType = fieldType;
    this.fieldName = fieldName;
    this.opcode = opcode;

    Opcode_GetPut.checkTypeAgainstOpcode(this.fieldType, this.opcode);
  }

  public DexInstruction_StaticPut(DexCode methodCode, DexRegister from, DexField field) {
    super(methodCode);

    if (!(field instanceof DexStaticField))
      throw new InstructionArgumentException("Expected static field");

    this.regFrom = from;
    this.fieldClass = field.getParentClass().getClassDef().getType();
    this.fieldType = field.getFieldDef().getFieldId().getType();
    this.fieldName = field.getFieldDef().getFieldId().getName();
    this.opcode = Opcode_GetPut.getOpcodeFromType(this.fieldType);
  }

  public DexInstruction_StaticPut(DexCode methodCode, Instruction insn, CodeParserState parsingState) throws InstructionParseError, UnknownTypeException {
    super(methodCode);

    if (insn instanceof Instruction21c && Opcode_GetPut.convert_SPUT(insn.opcode) != null) {

      val insnStaticPut = (Instruction21c) insn;
      val refItem = (FieldIdItem) insnStaticPut.getReferencedItem();
      regFrom = parsingState.getRegister(insnStaticPut.getRegisterA());
      fieldClass = DexClassType.parse(
                     refItem.getContainingClass().getTypeDescriptor(),
                     parsingState.getCache());
      fieldType = DexRegisterType.parse(
                    refItem.getFieldType().getTypeDescriptor(),
                    parsingState.getCache());
      fieldName = refItem.getFieldName().getStringValue();
      opcode = Opcode_GetPut.convert_SPUT(insn.opcode);

    } else
      throw FORMAT_EXCEPTION;

    Opcode_GetPut.checkTypeAgainstOpcode(this.fieldType, this.opcode);
  }

  @Override
  public String getOriginalAssembly() {
    return "sput-" + opcode.getAssemblyName() + " " + regFrom.getOriginalIndexString() + ", " + fieldClass.getPrettyName() + "." + fieldName;
  }

  @Override
  public Set<? extends uk.ac.cam.db538.dexter.dex.code.reg.DexRegister> lvaReferencedRegisters() {
    return createSet(regFrom);
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
//    if (opcode != Opcode_GetPut.Object) {
//      if (fieldDeclaringClass.isInternal()) {
//        // FIELD OF PRIMITIVE TYPE DEFINED INTERNALLY
//        // store the taint to the taint field
//        val field = DexUtils.getStaticField(getParentFile(), fieldDeclaringClass.getType(), fieldName, fieldType);
//        code.replace(this,
//                     new DexCodeElement[] {
//                       this,
//                       new DexInstruction_StaticPut(code, state.getTaintRegister(regFrom), state.getCache().getTaintField(field)),
//                     });
//
//      } else
//        // FIELD OF PRIMITIVE TYPE DEFINED EXTERNALLY
//        // store the taint to the adjoined field in special global class
//        code.replace(this,
//                     new DexCodeElement[] {
//                       this,
//                       new DexInstruction_StaticPut(
//                         code,
//                         state.getTaintRegister(regFrom),
//                         state.getCache().getTaintField_ExternalStatic(fieldClass, (DexPrimitiveType) fieldType, fieldName))
//                     });
//
//    } else {
//      // FIELD OF REFERENCE TYPE
//      // no need to do anything
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
