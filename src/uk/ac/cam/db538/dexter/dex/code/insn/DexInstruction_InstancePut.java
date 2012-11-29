package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.FieldIdItem;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Format.Instruction22c;

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

public class DexInstruction_InstancePut extends DexInstruction {

  @Getter private final DexRegister regFrom;
  @Getter private final DexRegister regObject;
  @Getter private final DexClassType fieldClass;
  @Getter private final DexRegisterType fieldType;
  @Getter private final String fieldName;
  @Getter private final Opcode_GetPut opcode;

  public DexInstruction_InstancePut(DexCode methodCode, DexRegister from, DexRegister obj, DexClassType fieldClass, DexRegisterType fieldType, String fieldName, Opcode_GetPut opcode) {
    super(methodCode);

    this.regFrom = from;
    this.regObject = obj;
    this.fieldClass = fieldClass;
    this.fieldType = fieldType;
    this.fieldName = fieldName;
    this.opcode = opcode;

    Opcode_GetPut.checkTypeAgainstOpcode(this.fieldType, this.opcode);
  }

  public DexInstruction_InstancePut(DexCode methodCode, DexRegister from, DexRegister obj, DexField field) {
    super(methodCode);

    if (field.isStatic())
      throw new InstructionArgumentException("Expected instance field");

    this.regFrom = from;
    this.regObject = obj;
    this.fieldClass = field.getParentClass().getType();
    this.fieldType = field.getType();
    this.fieldName = field.getName();
    this.opcode = Opcode_GetPut.getOpcodeFromType(field.getType());
  }

  public DexInstruction_InstancePut(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException, UnknownTypeException {
    super(methodCode);

    if (insn instanceof Instruction22c && Opcode_GetPut.convert_IPUT(insn.opcode) != null) {

      val insnStaticPut = (Instruction22c) insn;
      val refItem = (FieldIdItem) insnStaticPut.getReferencedItem();
      regFrom = parsingState.getRegister(insnStaticPut.getRegisterA());
      regObject = parsingState.getRegister(insnStaticPut.getRegisterB());
      fieldClass = DexClassType.parse(
                     refItem.getContainingClass().getTypeDescriptor(),
                     parsingState.getCache());
      fieldType = DexRegisterType.parse(
                    refItem.getFieldType().getTypeDescriptor(),
                    parsingState.getCache());
      fieldName = refItem.getFieldName().getStringValue();
      opcode = Opcode_GetPut.convert_IPUT(insn.opcode);

    } else
      throw new InstructionParsingException("Unknown instruction format or opcode");

    Opcode_GetPut.checkTypeAgainstOpcode(this.fieldType, this.opcode);
  }

  @Override
  public String getOriginalAssembly() {
    return "iput-" + opcode.getAssemblyName() + " v" + regFrom.getOriginalIndexString() + ", {v" + regObject.getOriginalIndexString() + "}" + fieldClass.getPrettyName() + "." + fieldName;
  }

  @Override
  public Set<DexRegister> lvaReferencedRegisters() {
    val referencedRegs = new HashSet<DexRegister>();
    referencedRegs.add(regFrom);
    referencedRegs.add(regObject);
    return referencedRegs;
  }

  @Override
  public Set<GcRangeConstraint> gcRangeConstraints() {
    val constraints = new HashSet<GcRangeConstraint>();
    constraints.add(new GcRangeConstraint(regFrom, ColorRange.RANGE_4BIT));
    constraints.add(new GcRangeConstraint(regObject, ColorRange.RANGE_4BIT));
    return constraints;
  }

  @Override
  protected DexCodeElement gcReplaceWithTemporaries(Map<DexRegister, DexRegister> mapping) {
    return new DexInstruction_InstancePut(getMethodCode(), mapping.get(regFrom), mapping.get(regObject), fieldClass, fieldType, fieldName, opcode);
  }

  @Override
  public Instruction[] assembleBytecode(Map<DexRegister, Integer> regAlloc, DexAssemblingCache cache) {
    int rFrom = regAlloc.get(regFrom);
    int rObject = regAlloc.get(regObject);

    if (fitsIntoBits_Unsigned(rFrom, 4) && fitsIntoBits_Unsigned(rObject, 4)) {
      return new Instruction[] {
               new Instruction22c(Opcode_GetPut.convert_IPUT(opcode), (byte) rFrom, (byte) rObject, cache.getField(fieldClass, fieldType, fieldName))
             };
    } else
      return throwCannotAssembleException("No suitable instruction format found");
  }
}
