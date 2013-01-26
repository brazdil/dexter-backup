package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.Arrays;
import java.util.Map;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction11x;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.elem.DexLabel;
import uk.ac.cam.db538.dexter.dex.code.insn.pseudo.DexPseudoinstruction_PrintStringConst;
import uk.ac.cam.db538.dexter.dex.method.DexPrototype;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexVoid;

import lombok.Getter;
import lombok.val;

public class DexInstruction_ReturnWide extends DexInstruction {

  @Getter private final DexRegister regFrom1;
  @Getter private final DexRegister regFrom2;

  public DexInstruction_ReturnWide(DexCode methodCode, DexRegister from1, DexRegister from2) {
    super(methodCode);
    regFrom1 = from1;
    regFrom2 = from2;
  }

  public DexInstruction_ReturnWide(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException {
    super(methodCode);

    if (insn instanceof Instruction11x && insn.opcode == Opcode.RETURN_WIDE) {

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
  protected DexCodeElement gcReplaceWithTemporaries(Map<DexRegister, DexRegister> mapping) {
    return new DexInstruction_ReturnWide(getMethodCode(), mapping.get(regFrom1), mapping.get(regFrom2));
  }

  @Override
  public void instrument(DexCode_InstrumentationState state) {
    val dex = getParentFile();
    val parentMethod = getParentMethod();
    val code = getMethodCode();

    val regResSemaphore = new DexRegister();

    val insnPrintDebug = new DexPseudoinstruction_PrintStringConst(
      code,
      "$# exiting method " +
      getParentClass().getType().getPrettyName() +
      "->" + getParentMethod().getName(),
      true);
    val insnGetSRES = new DexInstruction_StaticGet(code, regResSemaphore, dex.getMethodCallHelper_SRes());
    val insnAcquireSRES = new DexInstruction_Invoke(
      code,
      (DexClassType) dex.getMethodCallHelper_SRes().getType(),
      "acquire",
      new DexPrototype(DexVoid.parse("V", null), null),
      Arrays.asList(regResSemaphore),
      Opcode_Invoke.Virtual);
    val insnSetRES = new DexInstruction_StaticPut(code, state.getTaintRegister(regFrom1), dex.getMethodCallHelper_Res());

    if (parentMethod.isVirtual()) {
      val labelSkipTaintPassing = new DexLabel(code);

      val insnTestInternalClassAnnotation = new DexInstruction_IfTestZero(
        code,
        state.getInternalClassAnnotationRegister(),
        labelSkipTaintPassing,
        Opcode_IfTestZero.eqz);

      code.replace(this, new DexCodeElement[] {
                     insnTestInternalClassAnnotation,
                     insnGetSRES,
                     insnAcquireSRES,
                     insnSetRES,
                     labelSkipTaintPassing,
                     insnPrintDebug,
                     this
                   });
    } else
      code.replace(this, new DexCodeElement[] {insnGetSRES, insnAcquireSRES, insnSetRES, insnPrintDebug, this});
  }
}
