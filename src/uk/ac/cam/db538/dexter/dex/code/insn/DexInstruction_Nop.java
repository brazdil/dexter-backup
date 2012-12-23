package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.Map;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.ArrayDataPseudoInstruction;
import org.jf.dexlib.Code.Format.Instruction10x;
import org.jf.dexlib.Code.Format.PackedSwitchDataPseudoInstruction;
import org.jf.dexlib.Code.Format.SparseSwitchDataPseudoInstruction;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_AssemblingState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;

public class DexInstruction_Nop extends DexInstruction {

  public static enum NopType {
    Standard,
    SparseSwitchData,
    PackedSwitchData,
    ArrayData
  }

  @Getter private final NopType instructionType;

  public DexInstruction_Nop(DexCode methodCode, NopType instructionType) {
    super(methodCode);
    this.instructionType = instructionType;
  }

  public DexInstruction_Nop(DexCode methodCode) {
    this(methodCode, NopType.Standard);
  }

  public DexInstruction_Nop(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException {
    super(methodCode);

    if (insn.opcode != Opcode.NOP)
      throw FORMAT_EXCEPTION;

    if (insn instanceof Instruction10x)
      this.instructionType = NopType.Standard;
    else if (insn instanceof SparseSwitchDataPseudoInstruction)
      this.instructionType = NopType.SparseSwitchData;
    else if (insn instanceof PackedSwitchDataPseudoInstruction)
      this.instructionType = NopType.PackedSwitchData;
    else if (insn instanceof ArrayDataPseudoInstruction)
      this.instructionType = NopType.ArrayData;
    else
      throw FORMAT_EXCEPTION;
  }

  @Override
  public String getOriginalAssembly() {
    return "nop";
  }

  @Override
  public Instruction[] assembleBytecode(DexCode_AssemblingState state) throws InstructionAssemblyException {
    return new Instruction[] {
             new Instruction10x(Opcode.NOP)
           };
  }

  @Override
  public DexCodeElement[] instrument(DexCode_InstrumentationState mapping) {
    return new DexCodeElement[] { this };
  }

  @Override
  protected DexCodeElement gcReplaceWithTemporaries(Map<DexRegister, DexRegister> mapping) {
    return this;
  }
}
