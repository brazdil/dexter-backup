package uk.ac.cam.db538.dexter.dex.code.insn;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction11x;

import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.reg.DexRegister;

import lombok.Getter;
import lombok.val;

public class DexInstruction_Monitor extends DexInstruction {

  @Getter private final DexRegister Reg;
  @Getter private final boolean Enter;

  public DexInstruction_Monitor(DexRegister reg, boolean entering) {
    Reg = reg;
    Enter = entering;
  }

  public DexInstruction_Monitor(Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException {
    if (insn instanceof Instruction11x &&
        (insn.opcode == Opcode.MONITOR_ENTER || insn.opcode == Opcode.MONITOR_EXIT)) {

      val insnMonitor = (Instruction11x) insn;
      Reg = parsingState.getRegister(insnMonitor.getRegisterA());
      Enter = insn.opcode == Opcode.MONITOR_ENTER;

    } else
      throw new InstructionParsingException("Unknown instruction format or opcode");
  }

  @Override
  public String getOriginalAssembly() {
    return "monitor-" + (Enter ? "enter" : "exit") +
           " v" + Reg.getId();
  }
}
