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
    return "return-wide v" + regFrom1.getOriginalIndexString();
  }

  @Override
  protected DexCodeElement gcReplaceWithTemporaries(Map<DexRegister, DexRegister> mapping) {
    return new DexInstruction_ReturnWide(getMethodCode(), mapping.get(regFrom1), mapping.get(regFrom2));
  }

  @Override
  public void instrument(DexCode_InstrumentationState state) {
    if (state.isNeedsCallInstrumentation()) {
      val dex = getParentFile();
      val regResSemaphore = new DexRegister();
      val regTaint = new DexRegister();

      getMethodCode().replace(this,
                              new DexCodeElement[] {
                                new DexInstruction_StaticGet(getMethodCode(), regResSemaphore, dex.getMethodCallHelper_SRes()),
                                new DexInstruction_Invoke(
                                  getMethodCode(),
                                  (DexClassType) dex.getMethodCallHelper_SRes().getType(),
                                  "acquire",
                                  new DexPrototype(DexVoid.parse("V", null), null),
                                  Arrays.asList(regResSemaphore),
                                  Opcode_Invoke.Virtual),
                                new DexInstruction_BinaryOp(getMethodCode(), regTaint, state.getTaintRegister(regFrom1), state.getTaintRegister(regFrom2), Opcode_BinaryOp.OrInt),
                                new DexInstruction_StaticPut(getMethodCode(), regTaint, dex.getMethodCallHelper_Res()),
                                this
                              });
    }
  }
}
