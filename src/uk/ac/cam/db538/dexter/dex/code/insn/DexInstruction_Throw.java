package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.Collections;
import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction11x;

import uk.ac.cam.db538.dexter.dex.code.CodeParserState;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.reg.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.reg.DexSingleRegister;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.hierarchy.RuntimeHierarchy;

import com.google.common.collect.Sets;

public class DexInstruction_Throw extends DexInstruction {

  @Getter private final DexSingleRegister regFrom;

  public DexInstruction_Throw(DexSingleRegister from, RuntimeHierarchy hierarchy) {
    super(hierarchy);
    this.regFrom = from;
  }

  public static DexInstruction_Throw parse(Instruction insn, CodeParserState parsingState) {
    if (insn instanceof Instruction11x && insn.opcode == Opcode.THROW) {

      val insnThrow = (Instruction11x) insn;
      return new DexInstruction_Throw(
    		  parsingState.getSingleRegister(insnThrow.getRegisterA()),
    		  parsingState.getHierarchy());

    } else
      throw FORMAT_EXCEPTION;
  }

  @Override
  public String toString() {
    return "throw " + regFrom.toString();
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
  public void instrument(DexCode_InstrumentationState state) { }

  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }

  @Override
  protected DexClassType[] throwsExceptions() {
    return new DexClassType[] { DexClassType.parse("Ljava/lang/Throwable;", this.hierarchy.getTypeCache()) };
  }

  @Override
  protected Set<? extends DexCodeElement> cfgJumpTargets(DexCode code) {
	  return Collections.emptySet();
  }
}
