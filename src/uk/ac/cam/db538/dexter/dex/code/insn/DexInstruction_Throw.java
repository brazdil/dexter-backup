package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.HashSet;
import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction11x;

import uk.ac.cam.db538.dexter.analysis.coloring.ColorRange;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.DexCode_AssemblingState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;

public class DexInstruction_Throw extends DexInstruction {

  @Getter private final DexRegister regFrom;

  public DexInstruction_Throw(DexCode methodCode, DexRegister from) {
    super(methodCode);
    regFrom = from;
  }

  public DexInstruction_Throw(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException {
    super(methodCode);

    if (insn instanceof Instruction11x && insn.opcode == Opcode.THROW) {

      val insnThrow = (Instruction11x) insn;
      regFrom = parsingState.getRegister(insnThrow.getRegisterA());

    } else
      throw new InstructionParsingException("Unknown instruction format or opcode");
  }

  @Override
  public String getOriginalAssembly() {
    return "throw v" + regFrom.getOriginalIndexString();
  }

  @Override
  public Instruction[] assembleBytecode(DexCode_AssemblingState state) {
    int rFrom = state.getRegisterAllocation().get(regFrom);

    if (fitsIntoBits_Unsigned(rFrom, 8))
      return new Instruction[] {
               new Instruction11x(Opcode.THROW, (short) rFrom)
             };
    else
      return throwNoSuitableFormatFound();
  }

  @Override
  public boolean cfgEndsBasicBlock() {
    return true;
  }

  @Override
  public Set<DexRegister> lvaReferencedRegisters() {
    val set = new HashSet<DexRegister>();
    set.add(regFrom);
    return set;
  }

  @Override
  public Set<GcRangeConstraint> gcRangeConstraints() {
    val set = new HashSet<GcRangeConstraint>();
    set.add(new GcRangeConstraint(regFrom, ColorRange.RANGE_8BIT));
    return set;
  }

  @Override
  public boolean cfgExitsMethod() {
    return throwingInsn_CanExitMethod();
  }

  @Override
  public Set<DexCodeElement> cfgGetSuccessors() {
    return throwingInsn_CatchHandlers();
  }
}
