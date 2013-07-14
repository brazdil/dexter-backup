package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.FieldIdItem;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Format.Instruction21c;

import uk.ac.cam.db538.dexter.dex.code.CodeParserState;
import uk.ac.cam.db538.dexter.dex.code.reg.DexRegister;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexFieldId;
import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;
import uk.ac.cam.db538.dexter.hierarchy.RuntimeHierarchy;
import uk.ac.cam.db538.dexter.hierarchy.StaticFieldDefinition;

import com.google.common.collect.Sets;

public class DexInstruction_StaticGet extends DexInstruction {

  @Getter private final DexRegister regTo;
  @Getter private final StaticFieldDefinition fieldDef; 
  @Getter private final Opcode_GetPut opcode;

  public DexInstruction_StaticGet(DexRegister to, StaticFieldDefinition fieldDef, Opcode_GetPut opcode, RuntimeHierarchy hierarchy) {
    super(hierarchy);

    this.regTo = to;
    this.fieldDef = fieldDef;
    this.opcode = opcode;
    
    Opcode_GetPut.checkTypeAgainstOpcode(this.fieldDef.getFieldId().getType(), this.opcode);
  }

  public static DexInstruction_StaticGet parse(Instruction insn, CodeParserState parsingState) {
    val opcode = Opcode_GetPut.convert_SGET(insn.opcode);
    
	if (insn instanceof Instruction21c && opcode != null) {

      val hierarchy = parsingState.getHierarchy();
    	
      val insnStaticGet = (Instruction21c) insn;
      val refItem = (FieldIdItem) insnStaticGet.getReferencedItem();
      
      DexRegister regTo;
      if (opcode == Opcode_GetPut.Wide)
    	  regTo = parsingState.getWideRegister(insnStaticGet.getRegisterA());
      else
    	  regTo = parsingState.getSingleRegister(insnStaticGet.getRegisterA());
      
      StaticFieldDefinition fieldDef = hierarchy
    		 .getBaseClassDefinition(
    		  	DexClassType.parse(
    				  refItem.getContainingClass().getTypeDescriptor(),
    				  hierarchy.getTypeCache()))
    		 .getAccessedStaticField(
    		    DexFieldId.parseFieldId(
		    		refItem.getFieldName().getStringValue(),
		    		DexRegisterType.parse(
		    				refItem.getFieldType().getTypeDescriptor(),
		    				hierarchy.getTypeCache()),
		    		hierarchy.getTypeCache()));
      
      return new DexInstruction_StaticGet(regTo, fieldDef, opcode, hierarchy);

    } else
      throw FORMAT_EXCEPTION;
  }

  @Override
  public String toString() {
    return "sget" + opcode.getAsmSuffix() + " " + regTo.toString() + ", " + fieldDef.toString(); 
  }

  @Override
  public Set<? extends DexRegister> lvaDefinedRegisters() {
    return Sets.newHashSet(regTo);
  }

  @Override
  public void instrument() {
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
	return this.hierarchy.getTypeCache().LIST_Error;
  }
  
}
