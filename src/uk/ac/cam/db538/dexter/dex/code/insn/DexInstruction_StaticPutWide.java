package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.FieldIdItem;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction21c;

import uk.ac.cam.db538.dexter.analysis.coloring.ColorRange;
import uk.ac.cam.db538.dexter.dex.DexAssemblingCache;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;
import uk.ac.cam.db538.dexter.dex.type.UnknownTypeException;

public class DexInstruction_StaticPutWide extends DexInstruction {

  @Getter private final DexRegister regFrom1;
  @Getter private final DexRegister regFrom2;
  @Getter private final DexClassType fieldClass;
  @Getter private final DexRegisterType fieldType;
  @Getter private final String fieldName;

  public DexInstruction_StaticPutWide(DexCode methodCode, DexRegister from1, DexRegister from2, DexClassType fieldClass, DexRegisterType fieldType, String fieldName) {
    super(methodCode);

    this.regFrom1 = from1;
    this.regFrom2 = from2;
    this.fieldClass = fieldClass;
    this.fieldType = fieldType;
    this.fieldName = fieldName;

    Opcode_GetPutWide.checkTypeIsWide(this.fieldType);
  }

  public DexInstruction_StaticPutWide(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException, UnknownTypeException {
    super(methodCode);

    if (insn instanceof Instruction21c && insn.opcode == Opcode.SPUT_WIDE) {

      val insnStaticPut = (Instruction21c) insn;
      val refItem = (FieldIdItem) insnStaticPut.getReferencedItem();
      regFrom1 = parsingState.getRegister(insnStaticPut.getRegisterA());
      regFrom2 = parsingState.getRegister(insnStaticPut.getRegisterA() + 1);
      fieldClass = DexClassType.parse(
                     refItem.getContainingClass().getTypeDescriptor(),
                     parsingState.getCache());
      fieldType = DexRegisterType.parse(
                    refItem.getFieldType().getTypeDescriptor(),
                    parsingState.getCache());
      fieldName = refItem.getFieldName().getStringValue();

    } else
      throw new InstructionParsingException("Unknown instruction format or opcode");

    Opcode_GetPutWide.checkTypeIsWide(this.fieldType);
  }

  @Override
  public String getOriginalAssembly() {
    return "sput-wide v" + regFrom1.getOriginalIndexString() + ", " + fieldClass.getPrettyName() + "." + fieldName;
  }

  @Override
  public Set<DexRegister> lvaReferencedRegisters() {
    val referencedRegs = new HashSet<DexRegister>();
    referencedRegs.add(regFrom1);
    referencedRegs.add(regFrom2);
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
    return constraints;
  }

  @Override
  protected DexCodeElement gcReplaceWithTemporaries(Map<DexRegister, DexRegister> mapping) {
    return new DexInstruction_StaticPutWide(getMethodCode(), mapping.get(regFrom1), mapping.get(regFrom2), fieldClass, fieldType, fieldName);
  }

  @Override
  public Instruction[] assembleBytecode(Map<DexRegister, Integer> regAlloc, DexAssemblingCache cache) {
    int rFrom1 = regAlloc.get(regFrom1);
    int rFrom2 = regAlloc.get(regFrom2);

    if (fitsIntoBits_Unsigned(rFrom1, 8) && rFrom1 + 1 == rFrom2) {
      return new Instruction[] {
               new Instruction21c(Opcode.SPUT_WIDE, (short) rFrom1, cache.getField(fieldClass, fieldType, fieldName))
             };
    } else
      return throwCannotAssembleException("No suitable instruction format found");
  }
}
