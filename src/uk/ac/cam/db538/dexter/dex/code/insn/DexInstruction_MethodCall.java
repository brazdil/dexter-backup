package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.LinkedList;
import java.util.List;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.MethodIdItem;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Format.Instruction35c;
import org.jf.dexlib.Code.Format.Instruction3rc;

import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.reg.DexRegister;
import uk.ac.cam.db538.dexter.dex.method.DexMethod;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;
import uk.ac.cam.db538.dexter.dex.type.DexType;

public class DexInstruction_MethodCall extends DexInstruction {

  @Getter private final DexClassType ClassType;
  @Getter private final String MethodName;
  @Getter private final DexType ReturnType;
  @Getter private final List<DexRegisterType> ArgumentTypes;
  @Getter private final List<DexRegister> ArgumentRegisters;
  @Getter private final Opcode_MethodCall CallType;

  public DexInstruction_MethodCall(DexClassType classType, String methodName, DexType returnType, List<DexRegisterType> argumentTypes, List<DexRegister> argumentRegisters, Opcode_MethodCall callType) {
    ClassType = classType;
    MethodName = methodName;
    ReturnType = returnType;
    ArgumentTypes = argumentTypes;
    ArgumentRegisters = argumentRegisters;
    CallType = callType;
  }

  public DexInstruction_MethodCall(Instruction insn, DexCode_ParsingState parsingState) {
    val cache = parsingState.getCache();

    MethodIdItem methodInfo;
    ArgumentRegisters = new LinkedList<DexRegister>();

    if (insn instanceof Instruction35c && Opcode_MethodCall.convert(insn.opcode) != null) {

      val insnInvoke = (Instruction35c) insn;
      methodInfo = (MethodIdItem) insnInvoke.getReferencedItem();

      switch (insnInvoke.getRegCount()) {
      case 5:
        ArgumentRegisters.add(0, parsingState.getRegister(insnInvoke.getRegisterA()));
      case 4:
        ArgumentRegisters.add(0, parsingState.getRegister(insnInvoke.getRegisterG()));
      case 3:
        ArgumentRegisters.add(0, parsingState.getRegister(insnInvoke.getRegisterF()));
      case 2:
        ArgumentRegisters.add(0, parsingState.getRegister(insnInvoke.getRegisterE()));
      case 1:
        ArgumentRegisters.add(0, parsingState.getRegister(insnInvoke.getRegisterD()));
      case 0:
        break;
      default:
        throw new InstructionParsingException("Unexpected number of method argument registers");
      }

    } else if (insn instanceof Instruction3rc && Opcode_MethodCall.convert(insn.opcode) != null) {

      val insnInvokeRange = (Instruction3rc) insn;
      methodInfo = (MethodIdItem) insnInvokeRange.getReferencedItem();

      val startRegister = insnInvokeRange.getStartRegister();
      for (int i = 0; i < insnInvokeRange.getRegCount(); ++i)
        ArgumentRegisters.add(parsingState.getRegister(startRegister + i));

    } else
      throw new InstructionParsingException("Unknown instruction format or opcode");

    ClassType = parsingState.getCache().getClassType(methodInfo.getContainingClass().getTypeDescriptor());

    MethodName = methodInfo.getMethodName().getStringValue();
    ReturnType = DexMethod.parseReturnType(methodInfo.getPrototype().getReturnType(), cache);
    ArgumentTypes = DexMethod.parseArgumentTypes(methodInfo.getPrototype().getParameters(), cache);

    CallType = Opcode_MethodCall.convert(insn.opcode);

    // check that the number of registers is correct
    int expectedRegisterCount = (CallType == Opcode_MethodCall.Static) ? 0 : 1;
    for (val argType : ArgumentTypes)
      expectedRegisterCount += argType.getRegisters();
    if (expectedRegisterCount != ArgumentRegisters.size())
      throw new InstructionParsingException("Wrong number of arguments given to a method call");
  }

  @Override
  public String getOriginalAssembly() {
    val str = new StringBuilder();
    str.append("invoke-");
    str.append(CallType.name().toLowerCase());
    str.append(" ");
    str.append(ClassType.getPrettyName());
    str.append(".");
    str.append(MethodName);

    if (CallType == Opcode_MethodCall.Static) {

      str.append("(");
      boolean first = true;
      for (val reg : ArgumentRegisters) {
        if (first) first = false;
        else str.append(", ");
        str.append("v" + reg.getId());
      }
      str.append(")");

    } else {
      str.append("{");

      boolean first = true;
      boolean second = false;
      for (val reg : ArgumentRegisters) {
        if (second) second = false;
        else if (!first) str.append(", ");

        str.append("v" + reg.getId());

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
}
