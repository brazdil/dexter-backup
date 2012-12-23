package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Format.Instruction23x;

import uk.ac.cam.db538.dexter.analysis.coloring.ColorRange;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_AssemblingState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
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
      throw FORMAT_EXCEPTION;
  }

  @Override
  public String getOriginalAssembly() {
    return "aget-" + opcode.getAssemblyName() + " v" + regTo.getOriginalIndexString() + ", {v" + regArray.getOriginalIndexString() + "}[v" + regIndex.getOriginalIndexString() + "]";
  }

  @Override
  public Set<DexRegister> lvaReferencedRegisters() {
    return createSet(regArray, regIndex);
  }

  @Override
  public Set<DexRegister> lvaDefinedRegisters() {
    return createSet(regTo);
  }

  @Override
  public Set<GcRangeConstraint> gcRangeConstraints() {
    return createSet(
             new GcRangeConstraint(regTo, ColorRange.RANGE_8BIT),
             new GcRangeConstraint(regArray, ColorRange.RANGE_8BIT),
             new GcRangeConstraint(regIndex, ColorRange.RANGE_8BIT));
  }

  @Override
  protected DexCodeElement gcReplaceWithTemporaries(Map<DexRegister, DexRegister> mapping) {
    return new DexInstruction_ArrayGet(getMethodCode(), mapping.get(regTo), mapping.get(regArray), mapping.get(regIndex), opcode);
  }

  @Override
  public Instruction[] assembleBytecode(DexCode_AssemblingState state) {
    val regAlloc = state.getRegisterAllocation();
    int rTo = regAlloc.get(regTo);
    int rArray = regAlloc.get(regArray);
    int rIndex = regAlloc.get(regIndex);

    if (fitsIntoBits_Unsigned(rTo, 8) && fitsIntoBits_Unsigned(rArray, 8) && fitsIntoBits_Unsigned(rIndex, 8)) {
      return new Instruction[] {
               new Instruction23x(Opcode_GetPut.convert_AGET(opcode), (short) rTo, (short) rArray, (short) rIndex)
             };
    } else
      return throwNoSuitableFormatFound();
  }
}
