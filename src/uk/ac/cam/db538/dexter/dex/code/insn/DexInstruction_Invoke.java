package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.MethodIdItem;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Format.Instruction35c;
import org.jf.dexlib.Code.Format.Instruction3rc;

import uk.ac.cam.db538.dexter.dex.code.CodeParserState;
import uk.ac.cam.db538.dexter.dex.code.reg.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.reg.DexStandardRegister;
import uk.ac.cam.db538.dexter.dex.method.DexMethod;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexMethodId;
import uk.ac.cam.db538.dexter.dex.type.DexPrototype;
import uk.ac.cam.db538.dexter.dex.type.DexReferenceType;
import uk.ac.cam.db538.dexter.hierarchy.RuntimeHierarchy;

public class DexInstruction_Invoke extends DexInstruction {

  @Getter private final DexReferenceType classType;
  @Getter private final DexMethodId methodId;
  @Getter private final List<DexStandardRegister> argumentRegisters;
  @Getter private final Opcode_Invoke callType;

  public DexInstruction_Invoke(DexReferenceType classType, DexMethodId methodId, List<DexStandardRegister> argumentRegisters, Opcode_Invoke callType, RuntimeHierarchy hierarchy) {
    super(hierarchy);

    this.classType = classType;
    this.methodId = methodId;
    if (argumentRegisters == null)
    	this.argumentRegisters = Collections.emptyList();
    else
    	this.argumentRegisters = Collections.unmodifiableList(new ArrayList<DexStandardRegister>(argumentRegisters));
    this.callType = callType;

    checkArguments();
  }

  private void checkArguments() {
	val isStatic = this.callType.isStatic();
	val argCount = argumentRegisters.size();
	val prototype = this.methodId.getPrototype();
	
	if (prototype.countParamWords(isStatic) > 255)
		throw new Error("Too many argument registers given to a method call");

	
	if (prototype.getParameterCount(isStatic) != argCount)
		throw new Error("Number of argument registers does not match the prototype of the invoked method");

	for (int i = 0; i < argCount; ++i) {
		val argType = prototype.getParameterType(i, isStatic, classType);
		val argReg = argumentRegisters.get(i);
		if (!argReg.canStoreType(argType))
			throw new Error("Type of an argument does not match the corresponding register");
	}
  }
  
  public DexInstruction_Invoke(DexMethod method, List<DexStandardRegister> argumentRegisters, RuntimeHierarchy hierarchy) {
    this(method.getParentClass().getClassDef().getType(),
         method.getMethodDef().getMethodId(),
         argumentRegisters,
         getCallType(method),
         hierarchy);
  }
  
  public DexInstruction_Invoke(DexInstruction_Invoke toClone) {
    this(toClone.classType,
         toClone.methodId,
         toClone.argumentRegisters,
         toClone.callType,
         toClone.hierarchy);
  }

  private static Opcode_Invoke getCallType(DexMethod method) {
	  /*
	   * IGNORES SUPER CALLS
	   */
	  if (method.getMethodDef().isStatic())
		  return Opcode_Invoke.Static;
	  else if (method.getMethodDef().isDirect())
		  return Opcode_Invoke.Direct;
	  else if (method.getParentClass().getClassDef().isInterface())
		  return Opcode_Invoke.Interface;
	  else
		  return Opcode_Invoke.Virtual;
  }

  public static DexInstruction_Invoke parse(Instruction insn, CodeParserState parsingState) {
	val hierarchy = parsingState.getHierarchy();
    val cache = hierarchy.getTypeCache();

    MethodIdItem methodItem;
    int insnRegCount;
    val opcode = Opcode_Invoke.convert(insn.opcode);
    
    // acquire necessary data
    
    if (insn instanceof Instruction35c && opcode != null) {

      val insnInvoke = (Instruction35c) insn;
      methodItem = (MethodIdItem) insnInvoke.getReferencedItem();
      insnRegCount = insnInvoke.getRegCount();
      
    } else if (insn instanceof Instruction3rc && opcode != null) {

      val insnInvokeRange = (Instruction3rc) insn;
      methodItem = (MethodIdItem) insnInvokeRange.getReferencedItem();
      insnRegCount = insnInvokeRange.getRegCount();
      
    } else
      throw FORMAT_EXCEPTION;

    val isStatic = opcode.isStatic();
    
    // parse referenced class and the method id
    
    val classType = DexReferenceType.parse(methodItem.getContainingClass().getTypeDescriptor(), cache);
    val methodId = DexMethodId.parseMethodId(
		    methodItem.getMethodName().getStringValue(),
		    DexPrototype.parse(methodItem.getPrototype(), cache),
		    cache);
    
    // check number of registers for arguments matches the prototype
  
    val prototype = methodId.getPrototype();
    val argRegCount = prototype.countParamWords(isStatic);
    if (argRegCount != insnRegCount)
    	throw new InstructionParseError("Number of registers in parsed Invoke instruction does not match the prototype");
  
    // generate a register for each parameter
    
    val argCount = prototype.getParameterCount(isStatic); 
    val argRegs = new ArrayList<DexStandardRegister>(argCount);

    for (int i = 0, j = 0; i < argCount; i++) {
    	val argType = prototype.getParameterType(i, isStatic, classType);
    	if (argType.isWide()) {
    		val reg1 = getRegId(insn, j);
    		val reg2 = getRegId(insn, j + 1);
    		
    		if (reg1 + 1 != reg2)
    			throw new InstructionParseError("Inconsistency in method argument registers: wide register not formed of two consecutive registers");
    		
    		argRegs.add(parsingState.getWideRegister(reg1));
    	} else
    		argRegs.add(parsingState.getSingleRegister(getRegId(insn, j)));
    	
    	j += argType.getRegisters();
    }

    // constructor should check everything once over
    
    return new DexInstruction_Invoke(classType, methodId, argRegs, opcode, hierarchy);
  }
  
  /*
   * Returns the identifier of a register referenced in an invoke instruction.
   */
  private static int getRegId(Instruction insn, int index) {
	  if (insn instanceof Instruction35c) {
		  
		  val insn35c = (Instruction35c) insn;
		  if (index < 0 || index >= insn35c.getRegCount())
			  throw new IndexOutOfBoundsException();
		  
		  switch(index) {
		  case 0:
			  return insn35c.getRegisterD();
		  case 1:
			  return insn35c.getRegisterE();
		  case 2:
			  return insn35c.getRegisterF();
		  case 3:
			  return insn35c.getRegisterG();
		  case 4:
			  return insn35c.getRegisterA();
		  default:
			  throw new Error();
		  }
		  
	  } else if (insn instanceof Instruction3rc) {
		  
		  val insn3rc = (Instruction3rc) insn;
		  if (index < 0 || index >= insn3rc.getRegCount())
			  throw new IndexOutOfBoundsException();
		  return insn3rc.getStartRegister() + index;
		  
	  } else
		  throw new Error();
  }

  @Override
  public String toString() {
    val str = new StringBuilder();
    str.append("invoke-");
    str.append(callType.name().toLowerCase());
    str.append(" ");
    str.append(classType.getPrettyName());
    str.append("->");
    str.append(methodId.getName());
    str.append(methodId.getPrototype().toString());
    str.append(" ");
    
    if (callType == Opcode_Invoke.Static) {

      str.append("(");
      boolean first = true;
      for (val reg : argumentRegisters) {
        if (first) first = false;
        else str.append(", ");
        str.append(reg.toString());
      }
      str.append(")");

    } else {
      str.append("{");

      boolean first = true;
      boolean second = false;
      for (val reg : argumentRegisters) {
        if (second) second = false;
        else if (!first) str.append(", ");

        str.append(reg.toString());

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
  public Set<? extends DexRegister> lvaReferencedRegisters() {
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
	return this.hierarchy.getTypeCache().LIST_Throwable;
  }
}
