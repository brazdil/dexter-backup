package uk.ac.cam.db538.dexter.dex.code.insn;

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
import uk.ac.cam.db538.dexter.dex.type.DexClassType;

public class DexInstruction_Monitor extends DexInstruction {

  @Getter private final DexRegister regMonitor;
  @Getter private final boolean enter;

  public DexInstruction_Monitor(DexCode methodCode, DexRegister reg, boolean entering) {
    super(methodCode);

    this.regMonitor = reg;
    this.enter = entering;
  }

  public DexInstruction_Monitor(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException {
    super(methodCode);

    if (insn instanceof Instruction11x &&
        (insn.opcode == Opcode.MONITOR_ENTER || insn.opcode == Opcode.MONITOR_EXIT)) {

      val insnMonitor = (Instruction11x) insn;
      regMonitor = parsingState.getRegister(insnMonitor.getRegisterA());
      enter = insn.opcode == Opcode.MONITOR_ENTER;

    } else
      throw FORMAT_EXCEPTION;
  }

  @Override
  public String getOriginalAssembly() {
    return "monitor-" + (enter ? "enter" : "exit") +
           " " + regMonitor.getOriginalIndexString();
  }

  @Override
  public Set<DexRegister> lvaReferencedRegisters() {
    return createSet(regMonitor);
  }

  @Override
  public void instrument(DexCode_InstrumentationState state) { }

  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }
  
  @Override
  protected DexClassType[] throwsExceptions() {
	if (enter)
		return getParentFile().getParsingCache().LIST_Error_NullPointerException;
	else
		return getParentFile().getParsingCache().LIST_Error_Null_IllegalMonitorStateException;
  }
  
}
