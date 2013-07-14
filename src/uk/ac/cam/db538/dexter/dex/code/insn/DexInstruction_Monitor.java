package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction11x;

import uk.ac.cam.db538.dexter.dex.code.CodeParserState;
import uk.ac.cam.db538.dexter.dex.code.reg.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.reg.DexSingleRegister;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.hierarchy.RuntimeHierarchy;

import com.google.common.collect.Sets;

public class DexInstruction_Monitor extends DexInstruction {

  @Getter private final DexSingleRegister regMonitor;
  @Getter private final boolean enter;

  public DexInstruction_Monitor(DexSingleRegister reg, boolean entering, RuntimeHierarchy hierarchy) {
    super(hierarchy);

    this.regMonitor = reg;
    this.enter = entering;
  }

  public static DexInstruction_Monitor parse(Instruction insn, CodeParserState parsingState) {
    if (insn instanceof Instruction11x &&
        (insn.opcode == Opcode.MONITOR_ENTER || insn.opcode == Opcode.MONITOR_EXIT)) {

      val insnMonitor = (Instruction11x) insn;
      return new DexInstruction_Monitor(
    		  parsingState.getSingleRegister(insnMonitor.getRegisterA()),
    		  insn.opcode == Opcode.MONITOR_ENTER,
    		  parsingState.getHierarchy());

    } else
      throw FORMAT_EXCEPTION;
  }

  @Override
  public String toString() {
    return "monitor-" + (enter ? "enter" : "exit") + " " + regMonitor.toString();
  }

  @Override
  public Set<? extends DexRegister> lvaReferencedRegisters() {
    return Sets.newHashSet(regMonitor);
  }

  @Override
  public void instrument() { }

  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }
  
  @Override
  protected DexClassType[] throwsExceptions() {
	if (enter)
		return this.hierarchy.getTypeCache().LIST_Error_NullPointerException;
	else
		return this.hierarchy.getTypeCache().LIST_Error_Null_IllegalMonitorStateException;
  }
  
}
