package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.FieldIdItem;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Format.Instruction22c;

import uk.ac.cam.db538.dexter.dex.DexUtils;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.insn.macro.DexMacro_GetObjectTaint;
import uk.ac.cam.db538.dexter.dex.code.insn.macro.DexMacro_SetObjectTaint;
import uk.ac.cam.db538.dexter.dex.field.DexField;
import uk.ac.cam.db538.dexter.dex.field.DexStaticField;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexFieldId;
import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;
import uk.ac.cam.db538.dexter.dex.type.UnknownTypeException;

public class DexInstruction_InstanceGet extends DexInstruction {

  @Getter private final DexRegister regTo;
  @Getter private final DexRegister regObject;
  @Getter private final DexClassType fieldClass;
  @Getter private final DexRegisterType fieldType;
  @Getter private final String fieldName;
  @Getter private final Opcode_GetPut opcode;

  public DexInstruction_InstanceGet(DexCode methodCode, DexRegister to, DexRegister obj, DexClassType fieldClass, DexRegisterType fieldType, String fieldName, Opcode_GetPut opcode) {
    super(methodCode);

    this.regTo = to;
    this.regObject = obj;
    this.fieldClass = fieldClass;
    this.fieldType = fieldType;
    this.fieldName = fieldName;
    this.opcode = opcode;

    Opcode_GetPut.checkTypeAgainstOpcode(this.fieldType, this.opcode);
  }

  public DexInstruction_InstanceGet(DexCode methodCode, DexRegister to, DexRegister obj, DexField field) {
    super(methodCode);

    if (field instanceof DexStaticField)
      throw new InstructionArgumentException("Expected instance field");

    this.regTo = to;
    this.regObject = obj;
    this.fieldClass = field.getParentClass().getClassDef().getType();
    this.fieldType = field.getFieldDef().getFieldId().getType();
    this.fieldName = field.getFieldDef().getFieldId().getName();
    this.opcode = Opcode_GetPut.getOpcodeFromType(this.fieldType);
  }

  public DexInstruction_InstanceGet(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException, UnknownTypeException {
    super(methodCode);

    if (insn instanceof Instruction22c && Opcode_GetPut.convert_IGET(insn.opcode) != null) {

      val insnInstanceGet = (Instruction22c) insn;
      val refItem = (FieldIdItem) insnInstanceGet.getReferencedItem();
      regTo = parsingState.getRegister(insnInstanceGet.getRegisterA());
      regObject = parsingState.getRegister(insnInstanceGet.getRegisterB());
      fieldClass = DexClassType.parse(
                     refItem.getContainingClass().getTypeDescriptor(),
                     parsingState.getCache());
      fieldType = DexRegisterType.parse(
                    refItem.getFieldType().getTypeDescriptor(),
                    parsingState.getCache());
      fieldName = refItem.getFieldName().getStringValue();
      opcode = Opcode_GetPut.convert_IGET(insn.opcode);

    } else
      throw FORMAT_EXCEPTION;

    Opcode_GetPut.checkTypeAgainstOpcode(this.fieldType, this.opcode);
  }

  @Override
  public String getOriginalAssembly() {
    return "iget-" + opcode.getAssemblyName() + " " + regTo.getOriginalIndexString() + ", {" + regObject.getOriginalIndexString() + "}" + fieldClass.getPrettyName() + "." + fieldName;
  }

  @Override
  public Set<DexRegister> lvaReferencedRegisters() {
    return createSet(regObject);
  }

  @Override
  public Set<DexRegister> lvaDefinedRegisters() {
    return createSet(regTo);
  }

  @Override
  public void instrument(DexCode_InstrumentationState state) {
    val code = getMethodCode();
    val classHierarchy = getParentFile().getHierarchy();

    if (opcode != Opcode_GetPut.Object) {
      val regValueTaint = state.getTaintRegister(regTo);

      val defClass = classHierarchy.getClassDefinition(fieldClass);
      val defField = defClass.getAccessedInstanceField(new DexFieldId(fieldName, fieldType));

      if (defField == null)
        System.err.println("warning: cannot find accessed instance field " + fieldClass.getPrettyName() + "." + fieldName);

      val fieldDeclaringClass = defField.getParentClass();
      if (fieldDeclaringClass.isInternal()) {
        // FIELD OF PRIMITIVE TYPE DEFINED INTERNALLY
        // combine the taint stored in adjoined field with the taint of the object
        val field = DexUtils.getField(getParentFile(), fieldDeclaringClass.getType(), fieldName, fieldType);
        val regObjectTaint = (regTo == regObject) ? new DexRegister() : state.getTaintRegister(regObject);
        code.replace(this,
                     new DexCodeElement[] {
                       // must get the object taint BEFORE the original instruction, or the object reference could be overwritten
                       new DexMacro_GetObjectTaint(code, regObjectTaint, regObject),
                       new DexInstruction_InstanceGet(code, regValueTaint, regObject, state.getCache().getTaintField(field)),
                       new DexInstruction_BinaryOp(code, regValueTaint, regValueTaint, regObjectTaint, Opcode_BinaryOp.OrInt),
                       this
                     });

      } else
        // FIELD OF PRIMITIVE TYPE DEFINED EXTERNALLY
        // assign the same taint as the containing object has
        code.replace(this,
                     new DexCodeElement[] {
                       new DexMacro_GetObjectTaint(code, regValueTaint, regObject),
                       this
                     });

    } else {
      // FIELD OF REFERENCE TYPE
      // the object itself has taint, but the taint of the object must be added
      val regObjectTaint = new DexRegister();
      code.replace(this,
                   new DexCodeElement[] {
                     this,
                     new DexMacro_GetObjectTaint(code, regObjectTaint, regObject),
                     new DexMacro_SetObjectTaint(code, regTo, regObjectTaint)
                   });
    }
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
