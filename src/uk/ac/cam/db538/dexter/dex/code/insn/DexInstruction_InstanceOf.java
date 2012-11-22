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
import uk.ac.cam.db538.dexter.dex.code.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.type.DexReferenceType;
import uk.ac.cam.db538.dexter.dex.type.UnknownTypeException;

import lombok.Getter;
import lombok.val;

public class DexInstruction_InstanceOf extends DexInstruction {

  @Getter private final DexRegister RegTo;
  @Getter private final DexRegister RegFrom;
  @Getter private final DexReferenceType Value;

  // CAREFUL: likely to throw exception

  public DexInstruction_InstanceOf(DexCode methodCode, DexRegister to, DexRegister from, DexReferenceType value) {
    super(methodCode);

    RegTo = to;
    RegFrom = from;
    Value = value;
  }

  public DexInstruction_InstanceOf(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException, UnknownTypeException {
    super(methodCode);

    if (insn instanceof Instruction22c && insn.opcode == Opcode.INSTANCE_OF) {

      val insnInstanceOf = (Instruction22c) insn;
      RegTo = parsingState.getRegister(insnInstanceOf.getRegisterA());
      RegFrom = parsingState.getRegister(insnInstanceOf.getRegisterB());
      Value = DexReferenceType.parse(
                ((TypeIdItem) insnInstanceOf.getReferencedItem()).getTypeDescriptor(),
                parsingState.getCache());

    } else
      throw new InstructionParsingException("Unknown instruction format or opcode");
  }

  @Override
  public String getOriginalAssembly() {
    return "instance-of v" + RegTo.getId() + ", v" + RegFrom.getId() +
           ", " + Value.getDescriptor();
  }

  @Override
  public Set<GcRangeConstraint> gcRangeConstraints() {
    val set = new HashSet<GcRangeConstraint>();
    set.add(new GcRangeConstraint(RegTo, ColorRange.Range_0_15));
    set.add(new GcRangeConstraint(RegFrom, ColorRange.Range_0_15));
    return set;
  }

  @Override
  public DexCodeElement gcReplaceWithTemporaries(Map<DexRegister, DexRegister> mapping) {
    return new DexInstruction_InstanceOf(
             getMethodCode(),
             mapping.get(RegTo),
             mapping.get(RegFrom),
             Value);
  }

  @Override
  public Set<DexRegister> lvaDefinedRegisters() {
    val set = new HashSet<DexRegister>();
    set.add(RegTo);
    return set;
  }

  @Override
  public Set<DexRegister> lvaReferencedRegisters() {
    val set = new HashSet<DexRegister>();
    set.add(RegFrom);
    return set;
  }


}
