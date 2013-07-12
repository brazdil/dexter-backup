package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction11x;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.CodeParserState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.elem.DexLabel;
import uk.ac.cam.db538.dexter.dex.code.insn.macro.DexMacro_PrintStringConst;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexPrototype;
import uk.ac.cam.db538.dexter.dex.type.DexVoid;

public class DexInstruction_ReturnWide extends DexInstruction {

  @Getter private final DexRegister regFrom1;
  @Getter private final DexRegister regFrom2;

  public DexInstruction_ReturnWide(DexCode methodCode, DexRegister from1, DexRegister from2) {
    super(methodCode);
    regFrom1 = from1;
    regFrom2 = from2;
  }

  public DexInstruction_ReturnWide(DexCode methodCode, Instruction insn, CodeParserState parsingState) throws InstructionParsingException {
    super(methodCode);

    if (insn instanceof Instruction11x && ) {

      val insnReturnWide = (Instruction11x) insn;
      regFrom1 = parsingState.getRegister(insnReturnWide.getRegisterA());
      regFrom2 = parsingState.getRegister(insnReturnWide.getRegisterA() + 1);

    } else
      throw FORMAT_EXCEPTION;
  }

  @Override
  public String getOriginalAssembly() {
    return "return-wide " + regFrom1.getOriginalIndexString() + "|" + regFrom2.getOriginalIndexString();
  }

  @Override
  public void instrument(DexCode_InstrumentationState state) {
    val printDebug = state.getCache().isInsertDebugLogging();

    val dex = getParentFile();
    val parentMethod = getParentMethod();
    val code = getMethodCode();

    val regResSemaphore = new DexRegister();

    val insnPrintDebug = new DexMacro_PrintStringConst(
      code,
      "$# exiting method " +
      getParentMethod().getMethodDef().toString(),
      true);
    val insnGetSRES = new DexInstruction_StaticGet(code, regResSemaphore, dex.getAuxiliaryDex().getField_CallResultSemaphore());
    val insnAcquireSRES = new DexInstruction_Invoke(
      code,
      (DexClassType) dex.getAuxiliaryDex().getField_CallResultSemaphore().getFieldDef().getFieldId().getType(),
      "acquire",
      new DexPrototype(DexVoid.parse("V", getParentFile().getTypeCache()), null),
      Arrays.asList(regResSemaphore),
      Opcode_Invoke.Virtual);
    val insnSetRES = new DexInstruction_StaticPut(code, state.getTaintRegister(regFrom1), dex.getAuxiliaryDex().getField_CallResultTaint());

    if (parentMethod.isVirtual()) {
      val labelSkipTaintPassing = new DexLabel(code);

      val insnTestInternalClassAnnotation = new DexInstruction_IfTestZero(
        code,
        state.getInternalClassAnnotationRegister(),
        labelSkipTaintPassing,
        Opcode_IfTestZero.eqz);

      if (printDebug) {
        code.replace(this, new DexCodeElement[] {
                       insnTestInternalClassAnnotation,
                       insnGetSRES,
                       insnAcquireSRES,
                       insnSetRES,
                       labelSkipTaintPassing,
                       insnPrintDebug,
                       this
                     });
      } else {
        code.replace(this, new DexCodeElement[] {
                       insnTestInternalClassAnnotation,
                       insnGetSRES,
                       insnAcquireSRES,
                       insnSetRES,
                       labelSkipTaintPassing,
                       this
                     });
      }
    } else {
      if (printDebug) {
        code.replace(this, new DexCodeElement[] {insnGetSRES, insnAcquireSRES, insnSetRES, insnPrintDebug, this});
      } else {
        code.replace(this, new DexCodeElement[] {insnGetSRES, insnAcquireSRES, insnSetRES, this});
      }
    }
  }

  @Override
  public boolean cfgEndsBasicBlock() {
    return true;
  }

  @Override
  public Set<? extends uk.ac.cam.db538.dexter.dex.code.reg.DexRegister> lvaReferencedRegisters() {
    return createSet(regFrom1, regFrom2);
  }

  @Override
  public Set<? extends DexCodeElement> cfgJumpTargets(DexCode code) {
    return Collections.emptySet();
  }

  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }
}
