package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.FieldIdItem;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Format.Instruction21c;

import uk.ac.cam.db538.dexter.dex.code.CodeParserState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.reg.DexRegister;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexFieldId;
import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;
import uk.ac.cam.db538.dexter.hierarchy.FieldDefinition;
import uk.ac.cam.db538.dexter.hierarchy.RuntimeHierarchy;

import com.google.common.collect.Sets;

public class DexInstruction_StaticPut extends DexInstruction {

  @Getter private final DexRegister regFrom;
  @Getter private final FieldDefinition fieldDef; 
  @Getter private final Opcode_GetPut opcode;

  public DexInstruction_StaticPut(DexRegister to, FieldDefinition fieldDef, Opcode_GetPut opcode, RuntimeHierarchy hierarchy) {
    super(hierarchy);

    this.regFrom = to;
    this.fieldDef = fieldDef;
    this.opcode = opcode;

    Opcode_GetPut.checkTypeAgainstOpcode(this.fieldDef.getFieldId().getType(), this.opcode);
  }

  public static DexInstruction_StaticPut parse(Instruction insn, CodeParserState parsingState) {
    val opcode = Opcode_GetPut.convert_SPUT(insn.opcode);
    
	if (insn instanceof Instruction21c && opcode != null) {

      val hierarchy = parsingState.getHierarchy();
    	
      val insnStaticPut = (Instruction21c) insn;
      val refItem = (FieldIdItem) insnStaticPut.getReferencedItem();
      
      DexRegister regFrom;
      if (opcode == Opcode_GetPut.Wide)
    	  regFrom = parsingState.getWideRegister(insnStaticPut.getRegisterA());
      else
    	  regFrom = parsingState.getSingleRegister(insnStaticPut.getRegisterA());
      
      FieldDefinition fieldDef = hierarchy
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
      
      return new DexInstruction_StaticPut(regFrom, fieldDef, opcode, hierarchy);

    } else
      throw FORMAT_EXCEPTION;
  }

  @Override
  public String toString() {
    return "sput" + opcode.getAsmSuffix() + " " + regFrom.toString() + ", " + fieldDef.toString(); 
  }

  @Override
  public Set<? extends DexRegister> lvaReferencedRegisters() {
    return Sets.newHashSet(regFrom);
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
	return this.hierarchy.getTypeCache().LIST_Error;
  }
  
}
