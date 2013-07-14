package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.FieldIdItem;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Format.Instruction22c;

import uk.ac.cam.db538.dexter.dex.code.CodeParserState;
import uk.ac.cam.db538.dexter.dex.code.reg.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.reg.DexSingleRegister;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexFieldId;
import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;
import uk.ac.cam.db538.dexter.hierarchy.InstanceFieldDefinition;
import uk.ac.cam.db538.dexter.hierarchy.RuntimeHierarchy;

import com.google.common.collect.Sets;

public class DexInstruction_InstancePut extends DexInstruction {

  @Getter private final DexRegister regFrom;
  @Getter private final DexSingleRegister regObject;
  @Getter private final InstanceFieldDefinition fieldDef;
  @Getter private final Opcode_GetPut opcode;

  public DexInstruction_InstancePut(DexRegister from, DexSingleRegister obj, InstanceFieldDefinition fieldDef, Opcode_GetPut opcode, RuntimeHierarchy hierarchy) {
    super(hierarchy);

    this.regFrom = from;
    this.regObject = obj;
    this.fieldDef = fieldDef;
    this.opcode = opcode;
    
    Opcode_GetPut.checkTypeAgainstOpcode(this.fieldDef.getFieldId().getType(), this.opcode);
  }

  public static DexInstruction_InstancePut parse(Instruction insn, CodeParserState parsingState) {
    val opcode = Opcode_GetPut.convert_IPUT(insn.opcode);
    
	if (insn instanceof Instruction22c && opcode != null) {

      val hierarchy = parsingState.getHierarchy();
    	
      val insnInstancePut = (Instruction22c) insn;
      val refItem = (FieldIdItem) insnInstancePut.getReferencedItem();
      
      DexRegister regFrom;
      if (opcode == Opcode_GetPut.Wide)
    	  regFrom = parsingState.getWideRegister(insnInstancePut.getRegisterA());
      else
    	  regFrom = parsingState.getSingleRegister(insnInstancePut.getRegisterA());
      val regObj = parsingState.getSingleRegister(insnInstancePut.getRegisterB());
      
      InstanceFieldDefinition fieldDef = hierarchy
    		 .getClassDefinition(
    		  	DexClassType.parse(
    				  refItem.getContainingClass().getTypeDescriptor(),
    				  hierarchy.getTypeCache()))
    		 .getAccessedInstanceField(
    		    DexFieldId.parseFieldId(
		    		refItem.getFieldName().getStringValue(),
		    		DexRegisterType.parse(
		    				refItem.getFieldType().getTypeDescriptor(),
		    				hierarchy.getTypeCache()),
		    		hierarchy.getTypeCache()));
      
      return new DexInstruction_InstancePut(regFrom, regObj, fieldDef, opcode, hierarchy);

    } else
      throw FORMAT_EXCEPTION;
  }

  @Override
  public String toString() {
    return "iput" + opcode.getAsmSuffix() + " " + regFrom.toString() + ", {" + regObject.toString() + "}" + fieldDef.toString();
  }

  @Override
  public Set<? extends DexRegister> lvaReferencedRegisters() {
    return Sets.newHashSet(regFrom, regObject);
  }

  @Override
  public void instrument() {
//    val code = getMethodCode();
//    val classHierarchy = getParentFile().getHierarchy();
//
//    val defClass = classHierarchy.getClassDefinition(fieldClass);
//    val defField = defClass.getAccessedInstanceField(new DexFieldId(fieldName, fieldType));
//
//    if (defField == null)
//      System.err.println("warning: cannot find accessed instance field " + fieldClass.getPrettyName() + "." + fieldName);
//
//    val fieldDeclaringClass = defField.getParentClass();
//
//    if (opcode != Opcode_GetPut.Object) {
//      val regValueTaint = state.getTaintRegister(regFrom);
//
//      if (fieldDeclaringClass.isInternal()) {
//        // FIELD OF PRIMITIVE TYPE DEFINED INTERNALLY
//        // store the taint to the taint field
//        val field = DexUtils.getInstanceField(getParentFile(), fieldDeclaringClass.getType(), fieldName, fieldType);
//        code.replace(this,
//                     new DexCodeElement[] {
//                       this,
//                       new DexInstruction_InstancePut(code, regValueTaint, regObject, state.getCache().getTaintField(field)),
//                     });
//
//      } else
//        // FIELD OF PRIMITIVE TYPE DEFINED EXTERNALLY
//        // assign the same taint to the object
//        code.replace(this,
//                     new DexCodeElement[] {
//                       this,
//                       new DexMacro_SetObjectTaint(code, regObject, regValueTaint)
//                     });
//
//    } else {
//      if (!fieldDeclaringClass.isInternal()) {
//        // FIELD OF REFERENCE TYPE DEFINED EXTERNALLY
//        // need to copy the taint of the field value to the containing object
//        val regValueTaint = new DexRegister();
//        code.replace(this,
//                     new DexCodeElement[] {
//                       this,
//                       new DexMacro_GetObjectTaint(code, regValueTaint, regFrom),
//                       new DexMacro_SetObjectTaint(code, regObject, regValueTaint)
//                     });
//      }
//
//      // FIELD OF REFERENCE TYPE DEFINED INTERNALLY
//      // no need to do anything
//    }
  }

  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }

  @Override
  protected DexClassType[] throwsExceptions() {
	return this.hierarchy.getTypeCache().LIST_Error_NullPointerException;
  }
  
}
