package uk.ac.cam.db538.dexter.dex.method;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.ClassDataItem.EncodedMethod;
import org.jf.dexlib.CodeItem;
import org.jf.dexlib.CodeItem.EncodedCatchHandler;
import org.jf.dexlib.CodeItem.TryItem;
import org.jf.dexlib.DebugInfoItem;
import org.jf.dexlib.DexFile;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Util.AccessFlags;

import uk.ac.cam.db538.dexter.analysis.coloring.GraphColoring;
import uk.ac.cam.db538.dexter.dex.DexAssemblingCache;
import uk.ac.cam.db538.dexter.dex.DexClass;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Move;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_MoveWide;
import uk.ac.cam.db538.dexter.dex.code.insn.InstructionParsingException;
import uk.ac.cam.db538.dexter.dex.type.DexReferenceType;
import uk.ac.cam.db538.dexter.dex.type.UnknownTypeException;
import uk.ac.cam.db538.dexter.utils.NoDuplicatesList;

public abstract class DexMethodWithCode extends DexMethod {

  @Getter private DexCode Code;
  @Getter private final DexCode ParameterMoveInstructions;
  @Getter private final NoDuplicatesList<DexRegister> ParameterRegisters;
  @Getter private final boolean Direct;

  public DexMethodWithCode(DexClass parent, String name, Set<AccessFlags> accessFlags,
                           DexPrototype prototype, DexCode code,
                           boolean direct) {
    super(parent, name, accessFlags, prototype);
    Code = code;
    Direct = direct;
    ParameterRegisters = this.getPrototype().generateParameterRegisters(this.isStatic());
    ParameterMoveInstructions = new DexCode();
  }

  public DexMethodWithCode(DexClass parent, EncodedMethod methodInfo) throws UnknownTypeException, InstructionParsingException {
    super(parent, methodInfo);
    if (methodInfo.codeItem == null)
      Code = new DexCode();
    else
      Code = new DexCode(methodInfo.codeItem, parent.getParentFile().getParsingCache());
    Direct = methodInfo.isDirect();
    ParameterRegisters = this.getPrototype().generateParameterRegisters(this.isStatic());
    ParameterMoveInstructions = new DexCode();

    val prototype = this.getPrototype();
    val isStatic = this.isStatic();
    val clazz = this.getParentClass();
    
    // create the parameter-register mappings
    val regCount = methodInfo.codeItem.getRegisterCount();
    val paramCount = prototype.getParameterCount(isStatic);
    for (int i = 0; i < paramCount; ++i) {
    	val paramRegId = prototype.getParameterRegisterId(i, regCount, isStatic, clazz);
  	  val paramType = prototype.getParameterType(i, isStatic, clazz);
	    switch (paramType.getTypeSize()) {
	    case SINGLE:
	      addParameterMapping_Single(i, Code.getRegisterByOriginalNumber(paramRegId));
	      break;
	    case WIDE:
	      addParameterMapping_Wide(i, Code.getRegisterByOriginalNumber(paramRegId), Code.getRegisterByOriginalNumber(paramRegId + 1));
	      break;
	    }
    }
  }


  
  public void addParameterMapping_Single(int paramIndex, DexRegister codeReg) {
	  val paramType = this.getPrototype().getParameterType(paramIndex, this.isStatic(), this.getParentClass());	  
	      val paramReg = ParameterRegisters.get(paramIndex);
	      ParameterMoveInstructions.add(new DexInstruction_Move(Code, codeReg, paramReg, paramType instanceof DexReferenceType));
	  }
  
  public void addParameterMapping_Wide(int paramIndex, DexRegister codeReg1, DexRegister codeReg2) {
      val paramReg1 = ParameterRegisters.get(paramIndex);
      val paramReg2 = ParameterRegisters.get(paramIndex + 1);

      ParameterMoveInstructions.add(new DexInstruction_MoveWide(Code, codeReg1, codeReg2, paramReg1, paramReg2));
  }

  @Override
  public boolean isVirtual() {
    return !Direct;
  }

  @Override
  public void instrument() {
    Code = Code.instrument();
  }

  @Override
  protected CodeItem generateCodeItem(DexFile outFile, DexAssemblingCache cache) {
    // do register allocation
    // note that this changes the code itself
    // (adds temporaries, inserts move instructions)
    val codeColoring = new GraphColoring(Code);
    val modifiedCode = codeColoring.getModifiedCode();
    
    // add parameter registers to the register allocation
    val registerAllocation = new HashMap<DexRegister, Integer>(codeColoring.getColoring());
    int registerCount = codeColoring.getColorsUsed();
    val inWords = ParameterRegisters.size();
    if (registerCount >= inWords) {
    	int startReg = registerCount - inWords;
    	for (int i = 0; i < inWords; ++i)    		
    		registerAllocation.put(ParameterRegisters.get(i), startReg + i);
    } else {
    	for (int i = 0; i < inWords; ++i)
    		registerAllocation.put(ParameterRegisters.get(i), i);
    	registerCount = inWords;
    }

    List<Instruction> instructions = new LinkedList<Instruction>();
    instructions.addAll(ParameterMoveInstructions.assembleBytecode(registerAllocation, cache));
    instructions.addAll(modifiedCode.assembleBytecode(registerAllocation, cache));

    List<TryItem> tries = null;

    List<EncodedCatchHandler> encodedCatchHandlers = null;

    int outWords = 0; // TODO: finish (max inWords of methods called inside the code)

    DebugInfoItem debugInfo = null;

    return CodeItem.internCodeItem(outFile, registerCount, inWords, outWords, debugInfo, instructions, tries, encodedCatchHandlers);
  }
}
