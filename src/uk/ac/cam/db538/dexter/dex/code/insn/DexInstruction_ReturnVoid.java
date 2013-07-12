package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.Collections;
import java.util.Set;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction10x;

import uk.ac.cam.db538.dexter.dex.code.CodeParserState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.hierarchy.RuntimeHierarchy;

public class DexInstruction_ReturnVoid extends DexInstruction {

  public DexInstruction_ReturnVoid(RuntimeHierarchy hierarchy) { 
	super(hierarchy);
  }

  public DexInstruction_ReturnVoid(Instruction insn, CodeParserState parsingState) {
	super(parsingState.getHierarchy());
	
    if (!(insn instanceof Instruction10x) || insn.opcode != Opcode.RETURN_VOID)
      throw FORMAT_EXCEPTION;
  }

  @Override
  public String toString() {
    return "return-void";
  }

  @Override
  public void instrument(DexCode_InstrumentationState state) {
//    val printDebug = state.getCache().isInsertDebugLogging();
//
//    if (printDebug) {
//      val code = getMethodCode();
//
//      val insnPrintDebug = new DexMacro_PrintStringConst(
//        code,
//        "$# exiting method " +
//        getParentMethod().getMethodDef().toString(),
//        true);
//
//      code.replace(this, new DexCodeElement[] { insnPrintDebug, this });
//    }
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
