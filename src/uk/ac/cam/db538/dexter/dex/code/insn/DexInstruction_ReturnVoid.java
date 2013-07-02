package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.Collections;
import java.util.Set;

import lombok.val;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction10x;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.insn.macro.DexMacro_PrintStringConst;

public class DexInstruction_ReturnVoid extends DexInstruction {

  public DexInstruction_ReturnVoid(DexCode methodCode) {
    super(methodCode);
  }

  public DexInstruction_ReturnVoid(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException {
    super(methodCode);

    if (!(insn instanceof Instruction10x) || insn.opcode != Opcode.RETURN_VOID)
      throw FORMAT_EXCEPTION;
  }

  @Override
  public String getOriginalAssembly() {
    return "return-void";
  }

  @Override
  public void instrument(DexCode_InstrumentationState state) {
    val printDebug = state.getCache().isInsertDebugLogging();

    if (printDebug) {
      val code = getMethodCode();

      val insnPrintDebug = new DexMacro_PrintStringConst(
        code,
        "$# exiting method " +
        getParentClass().getType().getPrettyName() +
        "->" + getParentMethod().getName(),
        true);

      code.replace(this, new DexCodeElement[] { insnPrintDebug, this });
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
