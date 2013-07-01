package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.val;

import org.jf.dexlib.Code.Instruction;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_AssemblingState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCatchAll;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.elem.DexLabel;
import uk.ac.cam.db538.dexter.dex.code.elem.DexTryBlockEnd;
import uk.ac.cam.db538.dexter.dex.code.elem.DexTryBlockStart;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;

public abstract class DexInstruction extends DexCodeElement {

  public DexInstruction(DexCode methodCode) {
    super(methodCode);
  }

  // PARSING

  protected static final InstructionParsingException FORMAT_EXCEPTION = new InstructionParsingException("Unknown instruction format or opcode");

  // INSTRUCTION INSTRUMENTATION

  public void instrument(DexCode_InstrumentationState state) {
    throw new UnsupportedOperationException("Instruction " + this.getClass().getSimpleName() + " doesn't have instrumentation implemented");
  }

  // ASSEMBLING

  public Instruction[] assembleBytecode(DexCode_AssemblingState state) {
    throw new UnsupportedOperationException("Instruction " + this.getClass().getSimpleName() + " doesn't have assembling implemented");
  }

  public DexCodeElement[] fixLongJump() {
    throw new UnsupportedOperationException("Instruction " + this.getClass().getSimpleName() + " doesn't have long jump fix implemented");
  }

  protected final Instruction[] throwCannotAssembleException(String reason) {
    throw new InstructionAssemblyException("Instruction " + this.getClass().getSimpleName() + " couldn't be assembled (" + reason + ")");
  }

  protected final Instruction[] throwNoSuitableFormatFound() {
    return throwCannotAssembleException("No suitable format of instruction found");
  }

  protected final Instruction[] throwWideRegistersExpected() {
    throw new InstructionAssemblyException("Wide registers badly aligned with instruction: " + getOriginalAssembly());
  }

  // THROWING INSTRUCTIONS

  @Override
  public final boolean cfgExitsMethod() {
    val exceptions = this.throwsExceptions();
    if (exceptions != null)
    	for (val exception : exceptions)
    		if (throwingInsn_CanExitMethod(exception))
    			return true;
    return false;
  }
  
//  protected final boolean throwingInsn_CanExitMethod() {
//    return throwingInsn_CanExitMethod(
//             DexClassType.parse("Ljava/lang/Throwable;",
//                                getParentFile().getParsingCache()));
//  }

  protected final boolean throwingInsn_CanExitMethod(DexClassType thrownExceptionType) {
    val code = getMethodCode();
    val classHierarchy = getParentFile().getClassHierarchy();

    for (val tryBlockEnd : code.getTryBlocks()) {
      val tryBlockStart = tryBlockEnd.getBlockStart();

      // check that the instruction is in this try block
      if (code.isBetween(tryBlockStart, tryBlockEnd, this)) {

        // if the block has CatchAll handler, it can't exit the method
        if (tryBlockStart.getCatchAllHandler() != null)
          return false;

        // if there is a catch block catching the exception or its ancestor,
        // it can't exit the method either
        for (val catchBlock : tryBlockStart.getCatchHandlers())
          if (classHierarchy.isAncestor(thrownExceptionType, catchBlock.getExceptionType()))
            return false;
      }
    }

    return true;
  }

  protected final Set<DexCodeElement> throwingInsn_CatchHandlers(DexClassType thrownExceptionType) {
    val set = new HashSet<DexCodeElement>();

    val code = getMethodCode();
    val classHierarchy = getParentFile().getClassHierarchy();

    for (val tryBlockEnd : code.getTryBlocks()) {
      val tryBlockStart = tryBlockEnd.getBlockStart();

      // check that the instruction is in this try block
      if (code.isBetween(tryBlockStart, tryBlockEnd, this)) {

        // if the block has CatchAll handler, it can jump to it
        val catchAllHandler = tryBlockStart.getCatchAllHandler();
        if (catchAllHandler != null)
          set.add(catchAllHandler);

        // similarly, add all catch blocks as possible successors
        // if they catch the given exception type or its ancestor
        for (val catchBlock : tryBlockStart.getCatchHandlers())
          if (thrownExceptionType == null || classHierarchy.isAncestor(thrownExceptionType, catchBlock.getExceptionType()))
            set.add(catchBlock);
      }
    }

    return set;
  }

  protected final Set<DexCodeElement> throwingInsn_CatchHandlers() {
    return throwingInsn_CatchHandlers(null);
  }

  protected final DexTryBlockEnd getSurroundingTryBlock() {
    val code = getMethodCode();
    for (val tryBlockEnd : code.getTryBlocks())
      // check that the instruction is in this try block
      if (code.isBetween(tryBlockEnd.getBlockStart(), tryBlockEnd, this))
        return tryBlockEnd;
    return null;
  }

  protected final List<DexCodeElement> throwingInsn_GenerateSurroundingCatchBlock(DexCodeElement[] tryBlockCode, DexCodeElement[] catchBlockCode, DexRegister regException) {
    val code = getMethodCode();

    val catchAll = new DexCatchAll(code);
    val tryStart = new DexTryBlockStart(code);
    tryStart.setCatchAllHandler(catchAll);
    val tryEnd = new DexTryBlockEnd(code, tryStart);

    val labelSucc = new DexLabel(code);
    val gotoSucc = new DexInstruction_Goto(code, labelSucc);

    val moveException = new DexInstruction_MoveException(code, regException);
    val throwException = new DexInstruction_Throw(code, regException);

    val instrumentedCode = new ArrayList<DexCodeElement>();
    instrumentedCode.add(tryStart);
    instrumentedCode.addAll(Arrays.asList(tryBlockCode));
    instrumentedCode.add(gotoSucc);
    instrumentedCode.add(tryEnd);
    instrumentedCode.add(catchAll);
    instrumentedCode.add(moveException);
    instrumentedCode.addAll(Arrays.asList(catchBlockCode));
    instrumentedCode.add(throwException);
    instrumentedCode.add(labelSucc);

    return instrumentedCode;
  }

  static boolean fitsIntoBits_Signed(long value, int bits) {
    assert bits > 0;
    assert bits <= 64;

    long upperBound = 1L << bits - 1;
    return (value < upperBound) && (value >= -upperBound);
  }

  static boolean fitsIntoBits_Unsigned(long value, int bits) {
    assert bits > 0;
    assert bits <= 64;

    long mask = 0L;
    for (int i = bits; i < 64; ++i)
      mask |= 1L << i;
    return (value & mask) == 0L;
  }

  static boolean fitsIntoHighBits_Signed(long value, int bitsUsedWidth, int bitsBottomEmpty) {
    assert bitsUsedWidth > 0;
    assert bitsUsedWidth <= 64;
    assert bitsBottomEmpty > 0;
    assert bitsBottomEmpty <= 64;
    assert bitsUsedWidth + bitsBottomEmpty <= 64;

    // check that the bottom bits are zero
    // and then that it fits in the sum of bit arguments
    long mask = 0L;
    for (int i = 0; i < bitsBottomEmpty; ++i)
      mask |= 1L << i;
    return ((value & mask) == 0L) &&
           fitsIntoBits_Signed(value, bitsBottomEmpty + bitsUsedWidth);
  }

  static boolean formWideRegister(int reg1, int reg2) {
    return (reg1 + 1 == reg2);
  }

  protected final long computeRelativeOffset(DexCodeElement origin, DexCodeElement target, DexCode_AssemblingState state) {
    long offsetOrigin = state.getElementOffsets().get(origin);
    long offsetTarget = state.getElementOffsets().get(target);
    long offset = offsetTarget - offsetOrigin;

    if (offset == 0)
      throw new InstructionAssemblyException("Cannot have zero offset");

    return offset;
  }

  protected final long computeRelativeOffset(DexCodeElement target, DexCode_AssemblingState state) {
    return computeRelativeOffset(this, target, state);
  }
  
  abstract public void accept(DexInstructionVisitor visitor);
  
  // Subclasses should overrides this if the instruction may throw exceptions during execution.
  protected DexClassType[] throwsExceptions() {
	return null;  
  }
  
  @Override
  public boolean cfgEndsBasicBlock() {
	DexClassType[] exceptions = throwsExceptions();
	return  exceptions != null && exceptions.length > 0;
  }

  @Override
  public final Set<DexCodeElement> cfgGetSuccessors() {
	  // uses the DexCodeElement definition of cfgGetSuccessors
	  // (non-throwing semantics) but adds behavior after exceptions
	  // are thrown
	  
	  val set = super.cfgGetSuccessors();
	  
	  DexClassType[] exceptions = throwsExceptions();
	  if (exceptions != null) {
		  for(DexClassType exception : exceptions)
			  set.addAll(throwingInsn_CatchHandlers(exception));
	  }
	  
	  return set;
  }
  
}
