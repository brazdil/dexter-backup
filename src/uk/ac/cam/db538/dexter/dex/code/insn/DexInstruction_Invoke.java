package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.MethodIdItem;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Format.Instruction35c;
import org.jf.dexlib.Code.Format.Instruction3rc;

import uk.ac.cam.db538.dexter.analysis.coloring.ColorRange;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_AssemblingState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.method.DexDirectMethod;
import uk.ac.cam.db538.dexter.dex.method.DexPrototype;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;

public class DexInstruction_Invoke extends DexInstruction {

  @Getter private final DexClassType classType;
  @Getter private final String methodName;
  @Getter private final DexPrototype methodPrototype;
  private final List<DexRegister> argumentRegisters;
  @Getter private final Opcode_Invoke callType;

  public DexInstruction_Invoke(DexCode methodCode, DexClassType classType, String methodName, DexPrototype prototype, List<DexRegister> argumentRegisters, Opcode_Invoke callType) {
    super(methodCode);

    this.classType = classType;
    this.methodName = methodName;
    this.methodPrototype = prototype;
    this.argumentRegisters = argumentRegisters == null ? new LinkedList<DexRegister>() : argumentRegisters;
    this.callType = callType;

    checkArguments();
  }

  public DexInstruction_Invoke(DexCode methodCode, DexDirectMethod method, List<DexRegister> argumentRegisters) {
    this(methodCode,
         method.getParentClass().getType(),
         method.getName(),
         method.getPrototype(),
         argumentRegisters,
         method.getCallType());
  }

  public DexInstruction_Invoke(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) {
    super(methodCode);

    val cache = parsingState.getCache();

    MethodIdItem methodInfo;
    argumentRegisters = new LinkedList<DexRegister>();

    if (insn instanceof Instruction35c && Opcode_Invoke.convert(insn.opcode) != null) {

      val insnInvoke = (Instruction35c) insn;
      methodInfo = (MethodIdItem) insnInvoke.getReferencedItem();

      switch (insnInvoke.getRegCount()) {
      case 5:
        argumentRegisters.add(0, parsingState.getRegister(insnInvoke.getRegisterA()));
      case 4:
        argumentRegisters.add(0, parsingState.getRegister(insnInvoke.getRegisterG()));
      case 3:
        argumentRegisters.add(0, parsingState.getRegister(insnInvoke.getRegisterF()));
      case 2:
        argumentRegisters.add(0, parsingState.getRegister(insnInvoke.getRegisterE()));
      case 1:
        argumentRegisters.add(0, parsingState.getRegister(insnInvoke.getRegisterD()));
      case 0:
        break;
      default:
        throw new InstructionParsingException("Unexpected number of method argument registers");
      }

    } else if (insn instanceof Instruction3rc && Opcode_Invoke.convert(insn.opcode) != null) {

      val insnInvokeRange = (Instruction3rc) insn;
      methodInfo = (MethodIdItem) insnInvokeRange.getReferencedItem();

      val startRegister = insnInvokeRange.getStartRegister();
      for (int i = 0; i < insnInvokeRange.getRegCount(); ++i)
        argumentRegisters.add(parsingState.getRegister(startRegister + i));

    } else
      throw FORMAT_EXCEPTION;

    classType = DexClassType.parse(methodInfo.getContainingClass().getTypeDescriptor(), parsingState.getCache());

    methodName = methodInfo.getMethodName().getStringValue();
    methodPrototype = new DexPrototype(methodInfo.getPrototype(), cache);

    callType = Opcode_Invoke.convert(insn.opcode);

    checkArguments();
  }

  public DexInstruction_Invoke(DexInstruction_Invoke toClone) {
    this(toClone.getMethodCode(),
         toClone.classType,
         toClone.methodName,
         toClone.methodPrototype,
         toClone.argumentRegisters,
         toClone.callType);

    this.setOriginalElement(toClone.isOriginalElement());
  }

  private void checkArguments() {
    // check that the number of registers is correct
    int expectedRegisterCount = (callType == Opcode_Invoke.Static) ? 0 : 1;
    for (val argType : methodPrototype.getParameterTypes())
      expectedRegisterCount += argType.getRegisters();
    if (expectedRegisterCount != argumentRegisters.size())
      throw new InstructionArgumentException("Wrong number of arguments given to a method call");

    if (argumentRegisters.size() > 255)
      throw new InstructionArgumentException("Too many argument registers given to a method call");
  }

  public List<DexRegister> getArgumentRegisters() {
    return Collections.unmodifiableList(argumentRegisters);
  }

  public boolean isStaticCall() {
    return this.callType == Opcode_Invoke.Static;
  }

  @Override
  public String getOriginalAssembly() {
    val str = new StringBuilder();
    str.append("invoke-");
    str.append(callType.name().toLowerCase());
    str.append(" ");
    str.append(classType.getPrettyName());
    str.append(".");
    str.append(methodName);

    if (callType == Opcode_Invoke.Static) {

      str.append("(");
      boolean first = true;
      for (val reg : argumentRegisters) {
        if (first) first = false;
        else str.append(", ");
        str.append("v" + reg.getOriginalIndexString());
      }
      str.append(")");

    } else {
      str.append("{");

      boolean first = true;
      boolean second = false;
      for (val reg : argumentRegisters) {
        if (second) second = false;
        else if (!first) str.append(", ");

        str.append("v" + reg.getOriginalIndexString());

        if (first) {
          first = false;
          second = true;
          str.append("}(");
        }
      }

      str.append(")");
    }

    return str.toString();
  }

  private boolean assemblesToRange() {
    return argumentRegisters.size() > 5;
  }

  @Override
  public Instruction[] assembleBytecode(DexCode_AssemblingState state) {
    val regAlloc = state.getRegisterAllocation();
    int[] r = new int[argumentRegisters.size()];
    for (int i = 0; i < r.length; ++i)
      r[i] = regAlloc.get(argumentRegisters.get(i));

    val methodItem = state.getCache().getMethod(classType, methodPrototype, methodName);

    if (assemblesToRange()) {
      if (!fitsIntoBits_Unsigned(r.length, 8))
        return throwCannotAssembleException("Too many argument registers");

      val firstReg = r[0];
      for (int i = 1; i < r.length; ++i)
        if (!(r[i] == r[i - 1] + 1))
          return throwCannotAssembleException("Argument registers don't form an interval");

      return new Instruction[] {
               new Instruction3rc(Opcode_Invoke.convertRange(callType),
                                  (short) r.length,
                                  firstReg,
                                  methodItem)
             };
    } else {
      for (int regNum : r)
        if (!fitsIntoBits_Unsigned(regNum, 4))
          return throwCannotAssembleException("Register numbers don't fit into 4 bits");

      return new Instruction[] {
               new Instruction35c(Opcode_Invoke.convertStandard(callType),
                                  r.length,
                                  (byte) ((r.length >= 1) ? r[0] : 0),
                                  (byte) ((r.length >= 2) ? r[1] : 0),
                                  (byte) ((r.length >= 3) ? r[2] : 0),
                                  (byte) ((r.length >= 4) ? r[3] : 0),
                                  (byte) ((r.length >= 5) ? r[4] : 0),
                                  methodItem)
             };
    }
  }

  @Override
  public Set<DexRegister> lvaReferencedRegisters() {
    return new HashSet<DexRegister>(argumentRegisters);
  }

  @Override
  public Set<GcRangeConstraint> gcRangeConstraints() {
    val set = new HashSet<GcRangeConstraint>();

    if (!assemblesToRange())
      for(val argReg : argumentRegisters)
        set.add(new GcRangeConstraint(argReg, ColorRange.RANGE_4BIT));

    return set;
  }

  @Override
  public Set<GcFollowConstraint> gcFollowConstraints() {
    val set = new HashSet<GcFollowConstraint>();

    if (assemblesToRange()) {
      DexRegister previous = null;
      for(val current : argumentRegisters) {
        if (previous != null)
          set.add(new GcFollowConstraint(previous, current));
        previous = current;
      }
    }

    return set;
  }

  @Override
  protected DexCodeElement gcReplaceWithTemporaries(
    Map<DexRegister, DexRegister> mapping) {
    val newArgRegs = new LinkedList<DexRegister>();
    for (val argReg : argumentRegisters) {
      val mapReg = mapping.get(argReg);
      newArgRegs.add(mapReg == null ? argReg : mapReg);
    }

    return new DexInstruction_Invoke(getMethodCode(), classType, methodName, methodPrototype, newArgRegs, callType);
  }
}
