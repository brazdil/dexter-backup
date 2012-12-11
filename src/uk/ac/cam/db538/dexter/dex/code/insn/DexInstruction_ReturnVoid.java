package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.Collections;
import java.util.Set;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction10x;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.DexCode_AssemblingState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;

public class DexInstruction_ReturnVoid extends DexInstruction {

  public DexInstruction_ReturnVoid(DexCode methodCode) {
    super(methodCode);
  }

  public DexInstruction_ReturnVoid(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException {
    super(methodCode);

    if (!(insn instanceof Instruction10x) || insn.opcode != Opcode.RETURN_VOID)
      throw new InstructionParsingException("Unknown instruction format or opcode");
  }

  @Override
  public String getOriginalAssembly() {
    return "return-void";
  }

  @Override
  public DexCodeElement[] instrument(DexCode_InstrumentationState mapping) {
    return new DexCodeElement[] { this };
  }

  @Override
  public Instruction[] assembleBytecode(DexCode_AssemblingState state) {
    return new Instruction[] {
             new Instruction10x(Opcode.RETURN_VOID)
           };
  }

  @Override
  public boolean cfgExitsMethod() {
    return true;
  }

  @Override
  public Set<DexCodeElement> cfgGetSuccessors() {
    return Collections.emptySet();
  }
}
