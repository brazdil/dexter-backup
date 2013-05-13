package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.Map;
import java.util.Set;

import lombok.Getter;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction10t;
import org.jf.dexlib.Code.Format.Instruction20t;
import org.jf.dexlib.Code.Format.Instruction30t;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_AssemblingState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.elem.DexLabel;

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
      throw FORMAT_EXCEPTION;

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
    return createSet((DexCodeElement) target);
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

  @Override
  protected DexCodeElement gcReplaceWithTemporaries(Map<DexRegister, DexRegister> mapping, boolean toRefs, boolean toDefs) {
    return this;
  }

  @Override
  public void instrument(DexCode_InstrumentationState state) { }

  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }
}
