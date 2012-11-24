package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.HashSet;
import java.util.Set;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Format.Instruction22t;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.DexLabel;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;

import lombok.Getter;
import lombok.val;

public class DexInstruction_IfTest extends DexInstruction {

  @Getter private final DexRegister regA;
  @Getter private final DexRegister regB;
  @Getter private final DexLabel target;
  @Getter private final Opcode_IfTest insnOpcode;

  public DexInstruction_IfTest(DexCode methodCode, DexRegister regA, DexRegister regB, DexLabel target, Opcode_IfTest opcode) {
    super(methodCode);

    this.regA = regA;
    this.regB = regB;
    this.target = target;
    this.insnOpcode = opcode;
  }

  public DexInstruction_IfTest(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException {
    super(methodCode);

    if (insn instanceof Instruction22t && Opcode_IfTest.convert(insn.opcode) != null) {

      val insnIfTest = (Instruction22t) insn;
      regA = parsingState.getRegister(insnIfTest.getRegisterA());
      regB = parsingState.getRegister(insnIfTest.getRegisterB());
      target = parsingState.getLabel(insnIfTest.getTargetAddressOffset());
      insnOpcode = Opcode_IfTest.convert(insn.opcode);

    } else
      throw new InstructionParsingException("Unknown instruction format or opcode");
  }

  @Override
  public String getOriginalAssembly() {
    return "if-" + insnOpcode.name() + " v" + regA.getOriginalIndexString() +
           ", v" + regB.getOriginalIndexString() + ", L" + target.getOriginalAbsoluteOffset();
  }

  @Override
  public boolean cfgEndsBasicBlock() {
    return true;
  }

  @Override
  public DexCodeElement[] cfgGetSuccessors() {
    return new DexCodeElement[] {
             getNextCodeElement(),
             target
           };
  }

  @Override
  public Set<DexRegister> lvaReferencedRegisters() {
    val regs = new HashSet<DexRegister>();
    regs.add(regA);
    regs.add(regB);
    return regs;
  }
}
