package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.FieldIdItem;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Format.Instruction22c;

import uk.ac.cam.db538.dexter.dex.code.CodeParserState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.reg.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.reg.DexSingleRegister;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexFieldId;
import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;
import uk.ac.cam.db538.dexter.hierarchy.InstanceFieldDefinition;
import uk.ac.cam.db538.dexter.hierarchy.RuntimeHierarchy;

import com.google.common.collect.Sets;

public class DexInstruction_InstanceGet extends DexInstruction {

  @Getter private final DexRegister regTo;
  @Getter private final DexSingleRegister regObject;
  @Getter private final InstanceFieldDefinition fieldDef;
  @Getter private final Opcode_GetPut opcode;

  public DexInstruction_InstanceGet(DexRegister to, DexSingleRegister obj, InstanceFieldDefinition fieldDef, Opcode_GetPut opcode, RuntimeHierarchy hierarchy) {
    super(hierarchy);

    this.regTo = to;
    this.regObject = obj;
    this.fieldDef = fieldDef;
    this.opcode = opcode;
    
    Opcode_GetPut.checkTypeAgainstOpcode(this.fieldDef.getFieldId().getType(), this.opcode);
  }

  public static DexInstruction_InstanceGet parse(Instruction insn, CodeParserState parsingState) {
    val opcode = Opcode_GetPut.convert_IGET(insn.opcode);
    
	if (insn instanceof Instruction22c && opcode != null) {

      val hierarchy = parsingState.getHierarchy();
    	
      val insnInstanceGet = (Instruction22c) insn;
      val refItem = (FieldIdItem) insnInstanceGet.getReferencedItem();
      
      DexRegister regTo;
      if (opcode == Opcode_GetPut.Wide)
    	  regTo = parsingState.getWideRegister(insnInstanceGet.getRegisterA());
      else
    	  regTo = parsingState.getSingleRegister(insnInstanceGet.getRegisterA());
      val regObj = parsingState.getSingleRegister(insnInstanceGet.getRegisterB());
      
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
      
      return new DexInstruction_InstanceGet(regTo, regObj, fieldDef, opcode, hierarchy);

    } else
      throw FORMAT_EXCEPTION;
  }

  @Override
  public String toString() {
    return "iget" + opcode.getAsmSuffix() + " " + regTo.toString() + ", {" + regObject.toString() + "}" + fieldDef.toString();
  }

  @Override
  public Set<? extends DexRegister> lvaReferencedRegisters() {
    return Sets.newHashSet(regObject);
  }

  @Override
  public Set<? extends DexRegister> lvaDefinedRegisters() {
    return Sets.newHashSet(regTo);
  }

  @Override
  public void instrument(DexCode_InstrumentationState state) {
//    val code = getMethodCode();
//    val classHierarchy = getParentFile().getHierarchy();
//
//    if (opcode != Opcode_GetPut.Object) {
//      val regValueTaint = state.getTaintRegister(regTo);
//
//      val defClass = classHierarchy.getClassDefinition(fieldClass);
//      val defField = defClass.getAccessedInstanceField(new DexFieldId(fieldName, fieldType));
//
//      if (defField == null)
//        System.err.println("warning: cannot find accessed instance field " + fieldClass.getPrettyName() + "." + fieldName);
//
//      val fieldDeclaringClass = defField.getParentClass();
//      if (fieldDeclaringClass.isInternal()) {
//        // FIELD OF PRIMITIVE TYPE DEFINED INTERNALLY
//        // combine the taint stored in adjoined field with the taint of the object
//        val field = DexUtils.getInstanceField(getParentFile(), fieldDeclaringClass.getType(), fieldName, fieldType);
//        val regObjectTaint = (regTo == regObject) ? new DexRegister() : state.getTaintRegister(regObject);
//        code.replace(this,
//                     new DexCodeElement[] {
//                       // must get the object taint BEFORE the original instruction, or the object reference could be overwritten
//                       new DexMacro_GetObjectTaint(code, regObjectTaint, regObject),
//                       new DexInstruction_InstanceGet(code, regValueTaint, regObject, state.getCache().getTaintField(field)),
//                       new DexInstruction_BinaryOp(code, regValueTaint, regValueTaint, regObjectTaint, Opcode_BinaryOp.OrInt),
//                       this
//                     });
//
//      } else
//        // FIELD OF PRIMITIVE TYPE DEFINED EXTERNALLY
//        // assign the same taint as the containing object has
//        code.replace(this,
//                     new DexCodeElement[] {
//                       new DexMacro_GetObjectTaint(code, regValueTaint, regObject),
//                       this
//                     });
//
//    } else {
//      // FIELD OF REFERENCE TYPE
//      // the object itself has taint, but the taint of the object must be added
//      val regObjectTaint = new DexRegister();
//      code.replace(this,
//                   new DexCodeElement[] {
//                     this,
//                     new DexMacro_GetObjectTaint(code, regObjectTaint, regObject),
//                     new DexMacro_SetObjectTaint(code, regTo, regObjectTaint)
//                   });
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
