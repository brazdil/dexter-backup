package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.Collections;
import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction11x;

import uk.ac.cam.db538.dexter.dex.code.CodeParserState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.reg.DexRegister;
import uk.ac.cam.db538.dexter.hierarchy.RuntimeHierarchy;

import com.google.common.collect.Sets;

public class DexInstruction_Return extends DexInstruction {

  @Getter private final DexRegister regFrom;
  @Getter private final Opcode_Move opcode;

  public DexInstruction_Return(DexRegister from, Opcode_Move opcode, RuntimeHierarchy hierarchy) {
	super(hierarchy);
	
    this.regFrom = from;
    this.opcode = opcode;
    
    this.opcode.checkRegisterType(this.regFrom);
  }

  public DexInstruction_Return(Instruction insn, CodeParserState parsingState) {
	super(parsingState.getHierarchy());
	
    if (insn instanceof Instruction11x &&
        (insn.opcode == Opcode.RETURN || insn.opcode == Opcode.RETURN_WIDE || insn.opcode == Opcode.RETURN_OBJECT)) {

      val insnReturn = (Instruction11x) insn;
      this.opcode = Opcode_Move.convert(insn.opcode);
      if (this.opcode == Opcode_Move.Wide)
    	  this.regFrom = parsingState.getWideRegister(insnReturn.getRegisterA());
      else
    	  this.regFrom = parsingState.getSingleRegister(insnReturn.getRegisterA());

    } else
      throw FORMAT_EXCEPTION;
  }

  @Override
  public String toString() {
	  return opcode.getAssemblyName_Result() + " " + regFrom.toString();
  }

  @Override
  public boolean cfgEndsBasicBlock() {
    return true;
  }

  @Override
  public Set<? extends DexRegister> lvaReferencedRegisters() {
    return Sets.newHashSet(regFrom);
  }
  
  @Override
  public Set<DexCodeElement> cfgJumpTargets() {
    return Collections.emptySet();
  }

  @Override
  public void instrument(DexCode_InstrumentationState state) {
//    val printDebug = state.getCache().isInsertDebugLogging();
//
//    val code = getMethodCode();
//    val insnPrintDebug = new DexMacro_PrintStringConst(
//      code,
//      "$# exiting method " +
//      getParentMethod().getMethodDef().toString(),
//      true);
//
//    if (!objectMoving) {
//      val dex = getParentFile();
//      val parentMethod = getParentMethod();
//
//      val regResSemaphore = new DexRegister();
//
//      val insnGetSRES = new DexInstruction_StaticGet(code, regResSemaphore, dex.getAuxiliaryDex().getField_CallResultSemaphore());
//      val insnAcquireSRES = new DexInstruction_Invoke(
//        code,
//        (DexClassType) dex.getAuxiliaryDex().getField_CallResultSemaphore().getFieldDef().getFieldId().getType(),
//        "acquire",
//        new DexPrototype(DexVoid.parse("V", getParentFile().getTypeCache()), null),
//        Arrays.asList(regResSemaphore),
//        Opcode_Invoke.Virtual);
//      val insnSetRES = new DexInstruction_StaticPut(code, state.getTaintRegister(regFrom), dex.getAuxiliaryDex().getField_CallResultTaint());
//
//      if (parentMethod.isVirtual()) {
//        val labelSkipTaintPassing = new DexLabel(code);
//
//        val insnTestInternalClassAnnotation = new DexInstruction_IfTestZero(
//          code,
//          state.getInternalClassAnnotationRegister(),
//          labelSkipTaintPassing,
//          Opcode_IfTestZero.eqz);
//
//        if (printDebug) {
//          code.replace(this, new DexCodeElement[] {
//                         insnTestInternalClassAnnotation,
//                         insnGetSRES,
//                         insnAcquireSRES,
//                         insnSetRES,
//                         labelSkipTaintPassing,
//                         insnPrintDebug,
//                         this
//                       });
//        } else {
//          code.replace(this, new DexCodeElement[] {
//                         insnTestInternalClassAnnotation,
//                         insnGetSRES,
//                         insnAcquireSRES,
//                         insnSetRES,
//                         labelSkipTaintPassing,
//                         this
//                       });
//        }
//      } else {
//        if (printDebug) {
//          code.replace(this, new DexCodeElement[] {insnGetSRES, insnAcquireSRES, insnSetRES, insnPrintDebug, this});
//        } else {
//          code.replace(this, new DexCodeElement[] {insnGetSRES, insnAcquireSRES, insnSetRES, this});
//        }
//      }
//    } else {
//      if (printDebug) {
//        code.replace(this, new DexCodeElement[] {insnPrintDebug, this});
//      }
//    }
  }

  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }
}
