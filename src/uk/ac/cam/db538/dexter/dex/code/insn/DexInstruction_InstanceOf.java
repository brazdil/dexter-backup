package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jf.dexlib.TypeIdItem;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction22c;

import uk.ac.cam.db538.dexter.analysis.coloring.ColorRange;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.type.DexReferenceType;
import uk.ac.cam.db538.dexter.dex.type.UnknownTypeException;

import lombok.Getter;
import lombok.val;

public class DexInstruction_InstanceOf extends DexInstruction {

  @Getter private final DexRegister regTo;
  @Getter private final DexRegister regFrom;
  @Getter private final DexReferenceType value;

  // CAREFUL: likely to throw exception

  public DexInstruction_InstanceOf(DexCode methodCode, DexRegister to, DexRegister from, DexReferenceType value) {
    super(methodCode);

    this.regTo = to;
    this.regFrom = from;
    this.value = value;
  }

  public DexInstruction_InstanceOf(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException, UnknownTypeException {
    super(methodCode);

    if (insn instanceof Instruction22c && insn.opcode == Opcode.INSTANCE_OF) {

      val insnInstanceOf = (Instruction22c) insn;
      regTo = parsingState.getRegister(insnInstanceOf.getRegisterA());
      regFrom = parsingState.getRegister(insnInstanceOf.getRegisterB());
      value = DexReferenceType.parse(
                ((TypeIdItem) insnInstanceOf.getReferencedItem()).getTypeDescriptor(),
                parsingState.getCache());

    } else
      throw new InstructionParsingException("Unknown instruction format or opcode");
  }

  @Override
  public String getOriginalAssembly() {
    return "instance-of v" + regTo.getOriginalIndexString() + ", v" + regFrom.getOriginalIndexString() +
           ", " + value.getDescriptor();
  }

  @Override
  public Set<GcRangeConstraint> gcRangeConstraints() {
    val set = new HashSet<GcRangeConstraint>();
    set.add(new GcRangeConstraint(regTo, ColorRange.RANGE_4BIT));
    set.add(new GcRangeConstraint(regFrom, ColorRange.RANGE_4BIT));
    return set;
  }

  @Override
  protected DexCodeElement gcReplaceWithTemporaries(Map<DexRegister, DexRegister> mapping) {
    return new DexInstruction_InstanceOf(
             getMethodCode(),
             mapping.get(regTo),
             mapping.get(regFrom),
             value);
  }

  @Override
  public Set<DexRegister> lvaDefinedRegisters() {
    val set = new HashSet<DexRegister>();
    set.add(regTo);
    return set;
  }

  @Override
  public Set<DexRegister> lvaReferencedRegisters() {
    val set = new HashSet<DexRegister>();
    set.add(regFrom);
    return set;
  }


}
