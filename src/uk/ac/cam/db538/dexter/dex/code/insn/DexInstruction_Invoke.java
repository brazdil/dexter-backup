package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.MethodIdItem;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Format.Instruction35c;
import org.jf.dexlib.Code.Format.Instruction3rc;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.CodeParserState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.method.DexDirectMethod;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexPrototype;
import uk.ac.cam.db538.dexter.dex.type.DexReferenceType;

public class DexInstruction_Invoke extends DexInstruction {

  @Getter private final DexReferenceType classType;
  @Getter private final String methodName;
  @Getter private final DexPrototype methodPrototype;
  private final List<DexRegister> argumentRegisters;
  @Getter private final Opcode_Invoke callType;

  public DexInstruction_Invoke(DexCode methodCode, DexReferenceType classType, String methodName, DexPrototype prototype, List<DexRegister> argumentRegisters, Opcode_Invoke callType) {
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
         method.getParentClass().getClassDef().getType(),
         method.getMethodDef().getMethodId().getName(),
         method.getMethodDef().getMethodId().getPrototype(),
         argumentRegisters,
         method.getCallType());
  }

  public DexInstruction_Invoke(DexCode methodCode, Instruction insn, CodeParserState parsingState) {
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
        throw new InstructionParseError("Unexpected number of method argument registers");
      }

    } else if (insn instanceof Instruction3rc && Opcode_Invoke.convert(insn.opcode) != null) {

      val insnInvokeRange = (Instruction3rc) insn;
      methodInfo = (MethodIdItem) insnInvokeRange.getReferencedItem();

      val startRegister = insnInvokeRange.getStartRegister();
      for (int i = 0; i < insnInvokeRange.getRegCount(); ++i)
        argumentRegisters.add(parsingState.getRegister(startRegister + i));

    } else
      throw FORMAT_EXCEPTION;

    classType = DexReferenceType.parse(methodInfo.getContainingClass().getTypeDescriptor(), parsingState.getCache());

    methodName = methodInfo.getMethodName().getStringValue();
    methodPrototype = DexPrototype.parse(methodInfo.getPrototype(), cache);

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
        str.append(reg.getOriginalIndexString());
      }
      str.append(")");

    } else {
      str.append("{");

      boolean first = true;
      boolean second = false;
      for (val reg : argumentRegisters) {
        if (second) second = false;
        else if (!first) str.append(", ");

        str.append(reg.getOriginalIndexString());

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

  @Override
  public Set<? extends uk.ac.cam.db538.dexter.dex.code.reg.DexRegister> lvaReferencedRegisters() {
    return new HashSet<DexRegister>(argumentRegisters);
  }

  @Override
  public boolean cfgEndsBasicBlock() {
    return true;
  }

  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }
  
  @Override
  protected DexClassType[] throwsExceptions() {
	return getParentFile().getTypeCache().LIST_Throwable;
  }
}
