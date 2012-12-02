package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Format.Instruction23x;

import uk.ac.cam.db538.dexter.analysis.coloring.ColorRange;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.DexCode_AssemblingState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.type.UnknownTypeException;

public class DexInstruction_ArrayPut extends DexInstruction {

  @Getter private final DexRegister regFrom;
  @Getter private final DexRegister regArray;
  @Getter private final DexRegister regIndex;
  @Getter private final Opcode_GetPut opcode;

  public DexInstruction_ArrayPut(DexCode methodCode, DexRegister from, DexRegister array, DexRegister index, Opcode_GetPut opcode) {
    super(methodCode);

    this.regFrom = from;
    this.regArray = array;
    this.regIndex = index;
    this.opcode = opcode;
  }

  public DexInstruction_ArrayPut(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException, UnknownTypeException {
    super(methodCode);

    if (insn instanceof Instruction23x && Opcode_GetPut.convert_APUT(insn.opcode) != null) {

      val insnStaticPut = (Instruction23x) insn;
      regFrom = parsingState.getRegister(insnStaticPut.getRegisterA());
      regArray = parsingState.getRegister(insnStaticPut.getRegisterB());
      regIndex = parsingState.getRegister(insnStaticPut.getRegisterC());
      opcode = Opcode_GetPut.convert_APUT(insn.opcode);

    } else
      throw new InstructionParsingException("Unknown instruction format or opcode");
  }

  @Override
  public String getOriginalAssembly() {
    return "aput-" + opcode.getAssemblyName() + " v" + regFrom.getOriginalIndexString() + ", {v" + regArray.getOriginalIndexString() + "}[v" + regIndex.getOriginalIndexString() + "]";
  }

  @Override
  public Set<DexRegister> lvaReferencedRegisters() {
    val referencedRegs = new HashSet<DexRegister>();
    referencedRegs.add(regFrom);
    referencedRegs.add(regArray);
    referencedRegs.add(regIndex);
    return referencedRegs;
  }

  @Override
  public Set<GcRangeConstraint> gcRangeConstraints() {
    val constraints = new HashSet<GcRangeConstraint>();
    constraints.add(new GcRangeConstraint(regFrom, ColorRange.RANGE_8BIT));
    constraints.add(new GcRangeConstraint(regArray, ColorRange.RANGE_8BIT));
    constraints.add(new GcRangeConstraint(regIndex, ColorRange.RANGE_8BIT));
    return constraints;
  }

  @Override
  protected DexCodeElement gcReplaceWithTemporaries(Map<DexRegister, DexRegister> mapping) {
    return new DexInstruction_ArrayPut(getMethodCode(), mapping.get(regFrom), mapping.get(regArray), mapping.get(regIndex), opcode);
  }

  @Override
  public Instruction[] assembleBytecode(DexCode_AssemblingState state) {
	val regAlloc = state.getRegisterAllocation();
    int rFrom = regAlloc.get(regFrom);
    int rArray = regAlloc.get(regArray);
    int rIndex = regAlloc.get(regIndex);

    if (fitsIntoBits_Unsigned(rFrom, 8) && fitsIntoBits_Unsigned(rArray, 8) && fitsIntoBits_Unsigned(rIndex, 8)) {
      return new Instruction[] {
               new Instruction23x(Opcode_GetPut.convert_APUT(opcode), (short) rFrom, (short) rArray, (short) rIndex)
             };
    } else
        return throwNoSuitableFormatFound();
  }
}
