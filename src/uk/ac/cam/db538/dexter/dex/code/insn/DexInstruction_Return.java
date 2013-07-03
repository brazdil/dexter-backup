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
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.elem.DexLabel;
import uk.ac.cam.db538.dexter.dex.code.insn.macro.DexMacro_PrintStringConst;
import uk.ac.cam.db538.dexter.dex.method.DexPrototype;
import uk.ac.cam.db538.dexter.dex.type.DexType_Class;
import uk.ac.cam.db538.dexter.dex.type.DexType_Void;

public class DexInstruction_Return extends DexInstruction {

  @Getter private final DexRegister regFrom;
  @Getter private final boolean objectMoving;

  public DexInstruction_Return(DexCode methodCode, DexRegister from, boolean objectMoving) {
    super(methodCode);

    this.regFrom = from;
    this.objectMoving = objectMoving;
  }

  public DexInstruction_Return(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException {
    super(methodCode);

    if (insn instanceof Instruction11x &&
        (insn.opcode == Opcode.RETURN || insn.opcode == Opcode.RETURN_OBJECT)) {

      val insnReturn = (Instruction11x) insn;
      regFrom = parsingState.getRegister(insnReturn.getRegisterA());
      objectMoving = insn.opcode == Opcode.RETURN_OBJECT;

    } else
      throw FORMAT_EXCEPTION;
  }

  @Override
  public String getOriginalAssembly() {
    return "return" + (objectMoving ? "-object" : "") +
           " " + regFrom.getOriginalIndexString();
  }

  @Override
  public boolean cfgEndsBasicBlock() {
    return true;
  }

  @Override
  public Set<DexRegister> lvaReferencedRegisters() {
    return createSet(regFrom);
  }

  @Override
  public void instrument(DexCode_InstrumentationState state) {
    val printDebug = state.getCache().isInsertDebugLogging();

    val code = getMethodCode();
    val insnPrintDebug = new DexMacro_PrintStringConst(
      code,
      "$# exiting method " +
      getParentClass().getType().getPrettyName() +
      "->" + getParentMethod().getName(),
      true);

    if (!objectMoving) {
      val dex = getParentFile();
      val parentMethod = getParentMethod();

      val regResSemaphore = new DexRegister();

      val insnGetSRES = new DexInstruction_StaticGet(code, regResSemaphore, dex.getMethodCallHelper_SRes());
      val insnAcquireSRES = new DexInstruction_Invoke(
        code,
        (DexType_Class) dex.getMethodCallHelper_SRes().getType(),
        "acquire",
        new DexPrototype(DexType_Void.parse("V", null), null),
        Arrays.asList(regResSemaphore),
        Opcode_Invoke.Virtual);
      val insnSetRES = new DexInstruction_StaticPut(code, state.getTaintRegister(regFrom), dex.getMethodCallHelper_Res());

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
    } else {
      if (printDebug) {
        code.replace(this, new DexCodeElement[] {insnPrintDebug, this});
      }
    }
  }

  @Override
  public Set<DexCodeElement> cfgJumpTargets() {
    return Collections.emptySet();
  }

  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }
}
