package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.HashSet;
import java.util.Set;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction10t;
import org.jf.dexlib.Code.Format.Instruction20t;
import org.jf.dexlib.Code.Format.Instruction30t;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.DexCode_AssemblingState;
import uk.ac.cam.db538.dexter.dex.code.DexLabel;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;

import lombok.Getter;
import lombok.val;

public class DexInstruction_Goto extends DexInstruction {

  @Getter private final DexLabel target;

  public DexInstruction_Goto(DexCode methodCode, DexLabel target) {
    super(methodCode);

    this.target = target;
  }

  public DexInstruction_Goto(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException {
    super(methodCode);

    long targetOffset;
    if (insn instanceof Instruction10t && insn.opcode == Opcode.GOTO) {
      targetOffset = ((Instruction10t) insn).getTargetAddressOffset();
    } else if (insn instanceof Instruction20t && insn.opcode == Opcode.GOTO_16) {
      targetOffset = ((Instruction20t) insn).getTargetAddressOffset();
    } else if (insn instanceof Instruction30t && insn.opcode == Opcode.GOTO_32) {
      targetOffset = ((Instruction30t) insn).getTargetAddressOffset();
    } else
      throw new InstructionParsingException("Unknown instruction format or opcode");

    target = parsingState.getLabel(targetOffset);
  }

  @Override
  public String getOriginalAssembly() {
    return "goto L" + target.getOriginalAbsoluteOffset();
  }

  @Override
  public boolean cfgEndsBasicBlock() {
    return true;
  }

  @Override
  public Set<DexCodeElement> cfgGetSuccessors() {
    val set = new HashSet<DexCodeElement>();
    set.add(target);
    return set;
  }

  @Override
  public Instruction[] assembleBytecode(DexCode_AssemblingState state) {
    long offset = computeRelativeOffset(target, state);

    if (fitsIntoBits_Signed(offset, 8))
      return new Instruction[] {
               new Instruction10t(Opcode.GOTO, (int) offset)
             };
    else if (fitsIntoBits_Signed(offset, 16))
      return new Instruction[] {
               new Instruction20t(Opcode.GOTO_16, (int) offset)
             };
    else if (fitsIntoBits_Signed(offset, 32))
      return new Instruction[] {
               new Instruction30t(Opcode.GOTO_32, (int) offset)
             };
    else
      return throwNoSuitableFormatFound();
  }
}
