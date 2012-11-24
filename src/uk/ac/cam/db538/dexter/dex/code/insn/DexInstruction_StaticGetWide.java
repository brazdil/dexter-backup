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
import uk.ac.cam.db538.dexter.dex.DexField;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;
import uk.ac.cam.db538.dexter.dex.type.UnknownTypeException;

public class DexInstruction_StaticGetWide extends DexInstruction {

  @Getter private final DexRegister regTo1;
  @Getter private final DexRegister regTo2;
  @Getter private final DexClassType fieldClass;
  @Getter private final DexRegisterType fieldType;
  @Getter private final String fieldName;

  public DexInstruction_StaticGetWide(DexCode methodCode, DexRegister to1, DexRegister to2, DexClassType fieldClass, DexRegisterType fieldType, String fieldName) {
    super(methodCode);

    this.regTo1 = to1;
    this.regTo2 = to2;
    this.fieldClass = fieldClass;
    this.fieldType = fieldType;
    this.fieldName = fieldName;

    Opcode_GetPutWide.checkTypeIsWide(this.fieldType);
  }

  public DexInstruction_StaticGetWide(DexCode methodCode, DexRegister to1, DexRegister to2, DexField field) {
    super(methodCode);

    if (!field.isStatic())
      throw new InstructionArgumentException("Expected static field");

    this.regTo1 = to1;
    this.regTo2 = to2;
    this.fieldClass = field.getParentClass().getType();
    this.fieldType = field.getType();
    this.fieldName = field.getName();

    Opcode_GetPutWide.checkTypeIsWide(this.fieldType);
  }

  public DexInstruction_StaticGetWide(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException, UnknownTypeException {
    super(methodCode);

    if (insn instanceof Instruction21c && insn.opcode == Opcode.SGET_WIDE) {

      val insnStaticGet = (Instruction21c) insn;
      val refItem = (FieldIdItem) insnStaticGet.getReferencedItem();
      regTo1 = parsingState.getRegister(insnStaticGet.getRegisterA());
      regTo2 = parsingState.getRegister(insnStaticGet.getRegisterA() + 1);
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
    return "sget-wide v" + regTo1.getOriginalIndexString() + ", " + fieldClass.getPrettyName() + "." + fieldName;
  }

  @Override
  public Set<DexRegister> lvaDefinedRegisters() {
    val definedRegs = new HashSet<DexRegister>();
    definedRegs.add(regTo1);
    definedRegs.add(regTo2);
    return definedRegs;
  }

  @Override
  public Set<GcFollowConstraint> gcFollowConstraints() {
    val constraints = new HashSet<GcFollowConstraint>();
    constraints.add(new GcFollowConstraint(regTo1, regTo2));
    return constraints;
  }

  @Override
  public Set<GcRangeConstraint> gcRangeConstraints() {
    val constraints = new HashSet<GcRangeConstraint>();
    constraints.add(new GcRangeConstraint(regTo1, ColorRange.RANGE_8BIT));
    return constraints;
  }

  @Override
  protected DexCodeElement gcReplaceWithTemporaries(Map<DexRegister, DexRegister> mapping) {
    return new DexInstruction_StaticGetWide(getMethodCode(), mapping.get(regTo1), mapping.get(regTo2), fieldClass, fieldType, fieldName);
  }

  @Override
  public Instruction[] assembleBytecode(Map<DexRegister, Integer> regAlloc, DexAssemblingCache cache) {
    int rTo1 = regAlloc.get(regTo1);
    int rTo2 = regAlloc.get(regTo2);

    if (fitsIntoBits_Unsigned(rTo1, 8) && rTo1 + 1 == rTo2) {
      return new Instruction[] {
               new Instruction21c(Opcode.SGET_WIDE, (short) rTo1, cache.getField(fieldClass, fieldType, fieldName))
             };
    } else
      return throwCannotAssembleException("No suitable instruction format found");
  }
}
