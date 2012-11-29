package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Format.Instruction23x;

import uk.ac.cam.db538.dexter.analysis.coloring.ColorRange;
import uk.ac.cam.db538.dexter.dex.DexAssemblingCache;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.type.UnknownTypeException;

public class DexInstruction_ArrayGet extends DexInstruction {

  @Getter private final DexRegister regTo;
  @Getter private final DexRegister regArray;
  @Getter private final DexRegister regIndex;
  @Getter private final Opcode_GetPut opcode;

  public DexInstruction_ArrayGet(DexCode methodCode, DexRegister to, DexRegister array, DexRegister index, Opcode_GetPut opcode) {
    super(methodCode);

    this.regTo = to;
    this.regArray = array;
    this.regIndex = index;
    this.opcode = opcode;
  }

  public DexInstruction_ArrayGet(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException, UnknownTypeException {
    super(methodCode);

    if (insn instanceof Instruction23x && Opcode_GetPut.convert_AGET(insn.opcode) != null) {

      val insnArrayGet = (Instruction23x) insn;
      regTo = parsingState.getRegister(insnArrayGet.getRegisterA());
      regArray = parsingState.getRegister(insnArrayGet.getRegisterB());
      regIndex = parsingState.getRegister(insnArrayGet.getRegisterC());
      opcode = Opcode_GetPut.convert_AGET(insn.opcode);

    } else
      throw new InstructionParsingException("Unknown instruction format or opcode");
  }

  @Override
  public String getOriginalAssembly() {
    return "aget-" + opcode.getAssemblyName() + " v" + regTo.getOriginalIndexString() + ", {v" + regArray.getOriginalIndexString() + "}[v" + regIndex.getOriginalIndexString() + "]";
  }

  @Override
  public Set<DexRegister> lvaReferencedRegisters() {
    val definedRegs = new HashSet<DexRegister>();
    definedRegs.add(regArray);
    definedRegs.add(regIndex);
    return definedRegs;
  }

  @Override
  public Set<DexRegister> lvaDefinedRegisters() {
    val definedRegs = new HashSet<DexRegister>();
    definedRegs.add(regTo);
    return definedRegs;
  }

  @Override
  public Set<GcRangeConstraint> gcRangeConstraints() {
    val constraints = new HashSet<GcRangeConstraint>();
    constraints.add(new GcRangeConstraint(regTo, ColorRange.RANGE_8BIT));
    constraints.add(new GcRangeConstraint(regArray, ColorRange.RANGE_8BIT));
    constraints.add(new GcRangeConstraint(regIndex, ColorRange.RANGE_8BIT));
    return constraints;
  }

  @Override
  protected DexCodeElement gcReplaceWithTemporaries(Map<DexRegister, DexRegister> mapping) {
    return new DexInstruction_ArrayGet(getMethodCode(), mapping.get(regTo), mapping.get(regArray), mapping.get(regIndex), opcode);
  }

  @Override
  public Instruction[] assembleBytecode(Map<DexRegister, Integer> regAlloc, DexAssemblingCache cache) {
    int rTo = regAlloc.get(regTo);
    int rArray = regAlloc.get(regArray);
    int rIndex = regAlloc.get(regIndex);

    if (fitsIntoBits_Unsigned(rTo, 8) && fitsIntoBits_Unsigned(rArray, 8) && fitsIntoBits_Unsigned(rIndex, 8)) {
      return new Instruction[] {
               new Instruction23x(Opcode_GetPut.convert_AGET(opcode), (short) rTo, (short) rArray, (short) rIndex)
             };
    } else
      return throwCannotAssembleException("No suitable instruction format found");
  }
}
