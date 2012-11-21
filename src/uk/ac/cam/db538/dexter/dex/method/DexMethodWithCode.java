package uk.ac.cam.db538.dexter.dex.method;

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
import uk.ac.cam.db538.dexter.dex.DexClass;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.insn.InstructionParsingException;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;
import uk.ac.cam.db538.dexter.dex.type.DexType;
import uk.ac.cam.db538.dexter.dex.type.UnknownTypeException;

public abstract class DexMethodWithCode extends DexMethod {

  @Getter private DexCode Code;
  @Getter private final boolean Direct;

  public DexMethodWithCode(DexClass parent, String name, Set<AccessFlags> accessFlags,
                           DexType returnType, List<DexRegisterType> parameterTypes,
                           DexCode code, boolean direct) {
    super(parent, name, accessFlags, returnType, parameterTypes);
    Code = code;
    Direct = direct;
  }

  public DexMethodWithCode(DexClass parent, EncodedMethod methodInfo) throws UnknownTypeException, InstructionParsingException {
    super(parent, methodInfo);
    if (methodInfo.codeItem == null)
      Code = new DexCode(parent.getParentFile().getParsingCache());
    else
      Code = new DexCode(methodInfo.codeItem.getInstructions(), parent.getParentFile().getParsingCache());
    Direct = methodInfo.isDirect();
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
  protected CodeItem generateCodeItem(DexFile outFile) {
	  // do register allocation
	  // note that this changes the code itself
	  // (adds temporaries, inserts move instructions)
	  val codeColoring = new GraphColoring(Code);
	  val modifiedCode = codeColoring.getModifiedCode();
	  val registerAllocation = codeColoring.getColoring();
	  val registerCount = codeColoring.getNumberOfColorsUsed();
	  
    int inWords = 0;
    if (!isStatic())
      inWords += DexClassType.NumberOfRegisters;
    for (val param : this.getArgumentTypes())
      inWords += param.getRegisters();

    int outWords = 0; // TODO: finish (max inWords of methods called inside the code)

    DebugInfoItem debugInfo = null;

    List<Instruction> instructions = modifiedCode.assembleBytecode(registerAllocation);

    List<TryItem> tries = null;

    List<EncodedCatchHandler> encodedCatchHandlers = null;

    System.out.println("Creating code item for " + this.getName());
    System.out.println("  regCount = " + registerCount);
    System.out.println("  inWords = " + inWords);
    System.out.println("  outWords = " + outWords);
    
    return CodeItem.internCodeItem(outFile, registerCount, inWords, outWords, debugInfo, instructions, tries, encodedCatchHandlers);
  }
}
