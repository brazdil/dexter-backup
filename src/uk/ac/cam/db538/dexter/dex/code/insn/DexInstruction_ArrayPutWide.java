package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction23x;

import uk.ac.cam.db538.dexter.analysis.coloring.ColorRange;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_AssemblingState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.type.UnknownTypeException;

public class DexInstruction_ArrayPutWide extends DexInstruction {

  @Getter private final DexRegister regFrom1;
  @Getter private final DexRegister regFrom2;
  @Getter private final DexRegister regArray;
  @Getter private final DexRegister regIndex;

  public DexInstruction_ArrayPutWide(DexCode methodCode, DexRegister from1, DexRegister from2, DexRegister array, DexRegister index) {
    super(methodCode);

    this.regFrom1 = from1;
    this.regFrom2 = from2;
    this.regArray = array;
    this.regIndex = index;
  }

  public DexInstruction_ArrayPutWide(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException, UnknownTypeException {
    super(methodCode);

    if (insn instanceof Instruction23x && insn.opcode == Opcode.APUT_WIDE) {

      val insnArrayPutWide = (Instruction23x) insn;
      regFrom1 = parsingState.getRegister(insnArrayPutWide.getRegisterA());
      regFrom2 = parsingState.getRegister(insnArrayPutWide.getRegisterA() + 1);
      regArray = parsingState.getRegister(insnArrayPutWide.getRegisterB());
      regIndex = parsingState.getRegister(insnArrayPutWide.getRegisterC());
    } else
      throw new InstructionParsingException("Unknown instruction format or opcode");
  }

  @Override
  public String getOriginalAssembly() {
    return "aput-wide v" + regFrom1.getOriginalIndexString() + ", {v" + regArray.getOriginalIndexString() + "}[v" + regIndex.getOriginalIndexString() + "]";
  }

  @Override
  public Set<DexRegister> lvaReferencedRegisters() {
    val referencedRegs = new HashSet<DexRegister>();
    referencedRegs.add(regFrom1);
    referencedRegs.add(regFrom2);
    referencedRegs.add(regArray);
    referencedRegs.add(regIndex);
    return referencedRegs;
  }

  @Override
  public Set<GcFollowConstraint> gcFollowConstraints() {
    val constraints = new HashSet<GcFollowConstraint>();
    constraints.add(new GcFollowConstraint(regFrom1, regFrom2));
    return constraints;
  }

  @Override
  public Set<GcRangeConstraint> gcRangeConstraints() {
    val constraints = new HashSet<GcRangeConstraint>();
    constraints.add(new GcRangeConstraint(regFrom1, ColorRange.RANGE_8BIT));
    constraints.add(new GcRangeConstraint(regArray, ColorRange.RANGE_8BIT));
    constraints.add(new GcRangeConstraint(regIndex, ColorRange.RANGE_8BIT));
    return constraints;
  }

  @Override
  protected DexCodeElement gcReplaceWithTemporaries(Map<DexRegister, DexRegister> mapping) {
    return new DexInstruction_ArrayPutWide(getMethodCode(), mapping.get(regFrom1), mapping.get(regFrom2), mapping.get(regArray), mapping.get(regIndex));
  }

  @Override
  public Instruction[] assembleBytecode(DexCode_AssemblingState state) {
    val regAlloc = state.getRegisterAllocation();
    int rFrom1 = regAlloc.get(regFrom1);
    int rFrom2 = regAlloc.get(regFrom2);
    int rArray = regAlloc.get(regArray);
    int rIndex = regAlloc.get(regIndex);

    if (!formWideRegister(rFrom1, rFrom2))
      return throwWideRegistersExpected();

    if (fitsIntoBits_Unsigned(rFrom1, 8) && fitsIntoBits_Unsigned(rArray, 8) && fitsIntoBits_Unsigned(rIndex, 8)) {
      return new Instruction[] {
               new Instruction23x(Opcode.APUT_WIDE, (short) rFrom1, (short) rArray, (short) rIndex)
             };
    } else
      return throwNoSuitableFormatFound();
  }
}
