package uk.ac.cam.db538.dexter.dex.code.insn;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Format.Instruction22t;

import uk.ac.cam.db538.dexter.dex.code.DexLabel;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.reg.DexRegister;

import lombok.Getter;
import lombok.val;

public class DexInstruction_IfTest extends DexInstruction {

  @Getter private final DexRegister RegA;
  @Getter private final DexRegister RegB;
  @Getter private final DexLabel Target;
  @Getter private final Opcode_IfTest InsnOpcode;

  public DexInstruction_IfTest(DexRegister regA, DexRegister regB, DexLabel target, Opcode_IfTest opcode) {
    RegA = regA;
    RegB = regB;
    Target = target;
    InsnOpcode = opcode;
  }

  public DexInstruction_IfTest(Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException {
    if (insn instanceof Instruction22t && Opcode_IfTest.convert(insn.opcode) != null) {

      val insnIfTest = (Instruction22t) insn;
      RegA = parsingState.getRegister(insnIfTest.getRegisterA());
      RegB = parsingState.getRegister(insnIfTest.getRegisterB());
      Target = parsingState.getLabel(insnIfTest.getTargetAddressOffset());
      InsnOpcode = Opcode_IfTest.convert(insn.opcode);

    } else
      throw new InstructionParsingException("Unknown instruction format or opcode");
  }

  @Override
  public String getOriginalAssembly() {
    return "if-" + InsnOpcode.name() + " v" + RegA.getId() +
           ", v" + RegB.getId() + ", L" + Target.getOriginalAbsoluteOffset();
  }
}
