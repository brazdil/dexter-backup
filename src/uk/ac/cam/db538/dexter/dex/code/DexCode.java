package uk.ac.cam.db538.dexter.dex.code;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;

import org.jf.dexlib.CodeItem;
import org.jf.dexlib.CodeItem.EncodedCatchHandler;
import org.jf.dexlib.CodeItem.EncodedTypeAddrPair;
import org.jf.dexlib.CodeItem.TryItem;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Format.ArrayDataPseudoInstruction;
import org.jf.dexlib.Code.Format.PackedSwitchDataPseudoInstruction;

import uk.ac.cam.db538.dexter.analysis.coloring.ColorRange;
import uk.ac.cam.db538.dexter.analysis.coloring.NodeRun;
import uk.ac.cam.db538.dexter.dex.DexAssemblingCache;
import uk.ac.cam.db538.dexter.dex.DexInstrumentationCache;
import uk.ac.cam.db538.dexter.dex.DexParsingCache;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement.GcFollowConstraint;
import uk.ac.cam.db538.dexter.dex.code.elem.DexTryBlockEnd;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ArrayGet;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ArrayGetWide;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ArrayPut;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ArrayPutWide;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_BinaryOp;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_BinaryOpLiteral;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_BinaryOpWide;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_CheckCast;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Const;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ConstClass;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ConstString;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ConstWide;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Convert;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ConvertFromWide;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ConvertToWide;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ConvertWide;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_FillArray;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_FillArrayData;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Goto;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_IfTest;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_IfTestZero;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_InstanceGet;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_InstanceGetWide;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_InstanceOf;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_InstancePut;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_InstancePutWide;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Invoke;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Monitor;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Move;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_MoveException;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_MoveResult;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_MoveResultWide;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_MoveWide;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_NewArray;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_NewInstance;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Nop;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_PackedSwitch;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_PackedSwitchData;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Return;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ReturnVoid;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ReturnWide;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_StaticGet;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_StaticGetWide;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_StaticPut;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_StaticPutWide;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Throw;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_UnaryOp;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_UnaryOpWide;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Unknown;
import uk.ac.cam.db538.dexter.dex.code.insn.InstructionAssemblyException;
import uk.ac.cam.db538.dexter.dex.code.insn.InstructionOffsetException;
import uk.ac.cam.db538.dexter.dex.code.insn.InstructionParsingException;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_GetPut;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_Invoke;
import uk.ac.cam.db538.dexter.dex.code.insn.pseudo.DexPseudoinstruction;
import uk.ac.cam.db538.dexter.dex.code.insn.pseudo.DexPseudoinstruction_Invoke;
import uk.ac.cam.db538.dexter.dex.code.insn.pseudo.DexPseudoinstruction_PrintInteger;
import uk.ac.cam.db538.dexter.dex.code.insn.pseudo.DexPseudoinstruction_PrintStringConst;
import uk.ac.cam.db538.dexter.dex.method.DexMethodWithCode;
import uk.ac.cam.db538.dexter.dex.method.DexPrototype;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexType;
import uk.ac.cam.db538.dexter.utils.NoDuplicatesList;
import uk.ac.cam.db538.dexter.utils.Pair;

public class DexCode {

  private final NoDuplicatesList<DexCodeElement> instructionList;
  private final Set<DexRegister> usedRegisters;
  private final Set<DexTryBlockEnd> tryBlocks;
  @Getter private final DexMethodWithCode parentMethod;

  // stores information about original register mapping
  // is null for run-time generated code
  private DexCode_ParsingState parsingInfo = null;

  @Getter private DexCode_InstrumentationState instrumentationState = null;

  // creates completely empty code
  public DexCode() {
    this(null);
  }

  public DexCode(DexMethodWithCode parentMethod) {
    this.instructionList = new NoDuplicatesList<DexCodeElement>();
    this.usedRegisters = new HashSet<DexRegister>();
    this.tryBlocks = new HashSet<DexTryBlockEnd>();
    this.parentMethod = parentMethod;
  }

  public DexCode(CodeItem methodInfo, DexMethodWithCode parentMethod, DexParsingCache cache) {
    this(parentMethod);
    parsingInfo = new DexCode_ParsingState(cache, this);
    parseInstructions(methodInfo.getInstructions(), methodInfo.getHandlers(), methodInfo.getTries(), parsingInfo);
  }

  public DexCode(CodeItem methodInfo, DexParsingCache cache) {
    this(methodInfo, null, cache);
  }

  // called internally and from tests
  // should not be called directly in real life
  DexCode(Instruction[] instructions, DexParsingCache cache) {
    this();
    parsingInfo = new DexCode_ParsingState(cache, this);
    parseInstructions(instructions, null, null, parsingInfo);
  }

  private void parseInstructions(Instruction[] instructions, EncodedCatchHandler[] catchHandlers, TryItem[] tries, DexCode_ParsingState parsingState) {
    // What happens here:
    // - each instruction is parsed
    //   - offset of each instruction is stored
    //   - labels created in jumping instructions are stored
    //     separately, together with desired offsets
    // - labels are placed in the right position inside
    //   the instruction list
    // - try/catch blocks are inserted in between the instructions

    for (val insn : instructions) {
      val parsedInsn = parseInstruction(insn, parsingState);
      parsingState.addInstruction(insn.getSize(0), parsedInsn);
    }

    parsingState.placeTries(tries);
    parsingState.placeCatches(catchHandlers);
    parsingState.placeLabels();

    parsingState.checkTryCatchBlocksPlaced();
  }

  public List<DexCodeElement> getInstructionList() {
    return Collections.unmodifiableList(instructionList);
  }

  public Set<DexRegister> getUsedRegisters() {
    return Collections.unmodifiableSet(usedRegisters);
  }

  public Set<DexTryBlockEnd> getTryBlocks() {
    return Collections.unmodifiableSet(tryBlocks);
  }

  public DexRegister getRegisterByOriginalNumber(int id) {
    if (parsingInfo != null)
      return parsingInfo.getRegister(id);
    else
      return null;
  }

  private int findElement(DexCodeElement elem) {
    int index = 0;
    boolean found = false;
    for (val e : instructionList) {
      if (e.equals(elem)) {
        found = true;
        break;
      }
      index++;
    }

    if (found)
      return index;
    else
      throw new NoSuchElementException();
  }

  private void addedNewElement(DexCodeElement elem) {
    usedRegisters.addAll(elem.lvaUsedRegisters());
    if (elem instanceof DexTryBlockEnd)
      tryBlocks.add((DexTryBlockEnd) elem);
  }

  public void add(DexCodeElement elem) {
    instructionList.add(elem);
    addedNewElement(elem);
  }

  public void addAll(DexCodeElement[] elems) {
    for (val elem : elems)
      add(elem);
  }

  public void addAll(List<DexCodeElement> elems) {
    for (val elem : elems)
      add(elem);
  }

  public void insertBefore(DexCodeElement elem, DexCodeElement before) {
    instructionList.add(findElement(before), elem);
    addedNewElement(elem);
  }

  public void insertAfter(DexCodeElement elem, DexCodeElement after) {
    instructionList.add(findElement(after) + 1, elem);
    addedNewElement(elem);
  }

  public boolean isBetween(DexCodeElement elemStart, DexCodeElement elemEnd, DexCodeElement elemSought) {
    boolean hitStart = false, hitEnd = false;

    for (val elem : instructionList) {
      if (elem == elemSought)
        return hitStart && !hitEnd;
      else if (elem == elemStart)
        hitStart = true;
      else if (elem == elemEnd)
        hitEnd = true;
    }

    return false;
  }

  private static Pair<DexInstruction, Integer> nextInstruction(List<DexCodeElement> instructionList, int index) {
    int len = instructionList.size();
    for (int i = index + 1; i < len; ++i) {
      val insn = instructionList.get(i);
      if (insn instanceof DexInstruction)
        return new Pair<DexInstruction, Integer>((DexInstruction) insn, i);
    }
    return null;
  }

  private void generatePseudoinstructions() {
    val insns = instructionList;
    val codeLength = insns.size();
    val newInsns = new NoDuplicatesList<DexCodeElement>(codeLength);

    for (int i = 0; i < codeLength; i++) {
      val thisInsn = insns.get(i);

      if (thisInsn instanceof DexInstruction_Invoke) {
        val nextInsnPair = nextInstruction(insns, i);

        // replace INVOKE & MOVE_RESULT pairs with single
        // Invoke pseudoinstruction
        if (nextInsnPair != null &&
            (nextInsnPair.getValA() instanceof DexInstruction_MoveResult) ||
            (nextInsnPair.getValA() instanceof DexInstruction_MoveResultWide)) {
          newInsns.add(new DexPseudoinstruction_Invoke(
                         this,
                         (DexInstruction_Invoke) thisInsn,
                         (DexInstruction) nextInsnPair.getValA()));
          i = nextInsnPair.getValB();

          // to conform, replace other INVOKEs as well
        } else
          newInsns.add(new DexPseudoinstruction_Invoke(
                         this,
                         (DexInstruction_Invoke) thisInsn));
      } else
        newInsns.add(thisInsn);
    }

    replaceInstructions(newInsns);
  }

  private void unwrapPseudoinstructions() {
    val insns = instructionList;
    val codeLength = insns.size();
    val newInsns = new NoDuplicatesList<DexCodeElement>(codeLength);

    for (val insn : insns)
      if (insn instanceof DexPseudoinstruction)
        newInsns.addAll(((DexPseudoinstruction) insn).unwrap());
      else
        newInsns.add(insn);

    replaceInstructions(newInsns);
  }

  public void instrument(DexInstrumentationCache cache) {
    generatePseudoinstructions();

    instrumentationState = new DexCode_InstrumentationState(this, cache);
    val instrumentedInsns = new NoDuplicatesList<DexCodeElement>(instructionList.size() * 2);

    for (val elem : instructionList) {
      if (elem instanceof DexInstruction) {
        val insn = (DexInstruction) elem;
        instrumentedInsns.addAll(insn.instrument(instrumentationState));
      } else
        instrumentedInsns.add(elem);
    }

    replaceInstructions(instrumentedInsns);

    if (instrumentationState.isNeedsCallInstrumentation() && parentMethod.getPrototype().hasPrimitiveArgument())
      insertPostCallHandling(cache);

    unwrapPseudoinstructions();
  }

  private void insertPostCallHandling(DexInstrumentationCache cache) {
    val codePostCall = new NoDuplicatesList<DexCodeElement>();
    val dex = parentMethod.getParentClass().getParentFile();

    val parsingCache = cache.getParsingCache();
    val semaphoreClass = DexClassType.parse("Ljava/util/concurrent/Semaphore;", parsingCache);

    val regArray = new DexRegister();
    val regIndex = new DexRegister();
    val regSemaphore = new DexRegister();

    val argTaintRegs = parentMethod.getPrototype().generateArgumentTaintStoringRegisters(
                         parentMethod.getParameterMappedRegisters(),
                         parentMethod.isStatic(),
                         instrumentationState);

    codePostCall.add(new DexPseudoinstruction_PrintStringConst(this,
                     "$$$ METHOD " + parentMethod.getParentClass().getType().getPrettyName() +
                     "..." + parentMethod.getName(),
                     true));

    codePostCall.add(new DexInstruction_StaticGet(this, regArray, dex.getMethodCallHelper_Arg()));

    int arrayIndex = 0;
    for (val argTaintReg : argTaintRegs) {
      if (argTaintReg != null) {
        codePostCall.add(new DexInstruction_Const(this, regIndex, arrayIndex));
        codePostCall.add(new DexInstruction_ArrayGet(this, argTaintReg, regArray, regIndex, Opcode_GetPut.IntFloat));
        codePostCall.add(new DexPseudoinstruction_PrintStringConst(this, "$$$  ARG[" + arrayIndex + "] = ", false));
        codePostCall.add(new DexPseudoinstruction_PrintInteger(this, argTaintReg, true));
      }
      arrayIndex++;
    }

    codePostCall.add(new DexInstruction_StaticGet(
                       this,
                       regSemaphore,
                       dex.getMethodCallHelper_SArg()));
    codePostCall.add(new DexInstruction_Invoke(
                       this,
                       semaphoreClass,
                       "release",
                       new DexPrototype(DexType.parse("V", null), null),
                       Arrays.asList(new DexRegister[] { regSemaphore }),
                       Opcode_Invoke.Virtual));

    codePostCall.addAll(instructionList);
    replaceInstructions(codePostCall);
  }

  public Map<DexRegister, ColorRange> getRangeConstraints() {
    val allConstraints = new HashMap<DexRegister, ColorRange>();

    for (val insn : instructionList) {
      val insnConstraints = insn.gcRangeConstraints();

      for (val constraint : insnConstraints) {
        val register = constraint.getValA();
        val range = constraint.getValB();

        val savedRange = allConstraints.get(register);
        if (savedRange == null || savedRange.ordinal() > range.ordinal())
          allConstraints.put(register, range);
      }
    }

    return allConstraints;
  }

  private static void processFollowConstraint(GcFollowConstraint constraint, Map<DexRegister, NodeRun> allConstraints) {
    // we've gotten a constraint...
    // this means: color(reg2) = color(reg1) + 1
    val reg1 = constraint.getValA();
    val reg2 = constraint.getValB();

    // find runs containing reg1 and reg2
    val run1 = allConstraints.get(reg1);
    val run2 = allConstraints.get(reg2);

    // if registers are in the same run, they must be following each other,
    // so the loop can continue
    if (run1 == run2) {
      val loc1 = run1.getIndexOf(reg1);
      val loc2 = run1.getIndexOf(reg2);

      if (loc1 + 1 == loc2)
        return;
      else
        throw new RuntimeException("Getting follow-constraints of code failed (inconsistent constraints)");
    }

    // we need to connect the two runs, so reg1 must be the last element
    // of run1, and reg2 must be the first element of run2
    if (run1.peekLast() != reg1 || run2.peekFirst() != reg2)
      throw new RuntimeException("Getting follow-constraints of code failed (inconsistent constraints)");

    // all is fine now => connect the two runs
    val connectedRun = new NodeRun();
    connectedRun.addAll(run1.getNodes());
    connectedRun.addAll(run2.getNodes());

    // store the new connected run with all its nodes
    for (val node : connectedRun.getNodes())
      allConstraints.put(node, connectedRun);
  }

  public Map<DexRegister, NodeRun> getFollowRuns() {
    val allConstraints = new HashMap<DexRegister, NodeRun>();

    // create a single-element run for each register
    for (val reg : usedRegisters) {
      val newRun = new NodeRun();
      newRun.add(reg);
      allConstraints.put(reg, newRun);
    }

    // connect runs into larger ones based on the constraints
    // given by instructions
    for (val insn : instructionList)
      for (val constraint : insn.gcFollowConstraints())
        processFollowConstraint(constraint, allConstraints);

    return allConstraints;
  }

  public void replaceInstructions(List<DexCodeElement> newInsns) {
    instructionList.clear();
    usedRegisters.clear();
    tryBlocks.clear();

    addAll(newInsns);
  }

  private boolean allowJumpFix = true;

  public void disableJumpFixing() {
    allowJumpFix = false;
  }

  @AllArgsConstructor
  @Getter
  public static class AssembledCode {
    private final List<Instruction> instructions;
    private final List<TryItem> tries;
    private final List<EncodedCatchHandler> catchHandlers;
    private final int totalCodeLength;
  }

  public AssembledCode assembleBytecode(Map<DexRegister, Integer> regAlloc, DexAssemblingCache cache) {
    return assembleBytecode(regAlloc, cache, 0);
  }

  public AssembledCode assembleBytecode(Map<DexRegister, Integer> regAlloc, DexAssemblingCache cache, int absoluteAddressOffset) {
    DexCode modifiedCode = this;
    while (true) {
      try {
        val asmState = new DexCode_AssemblingState(modifiedCode, cache, regAlloc);
        val bytecode = new LinkedList<Instruction>();
        int totalCodeLength;

        // keep updating the offsets of instructions
        // until they converge
        boolean offsetsChanged;
        do {
          bytecode.clear();
          offsetsChanged = false;

          // assemble each instruction
          long offset = 0;
          for (val elem : modifiedCode.instructionList) {
            long previousOffset = asmState.getElementOffsets().get(elem);
            offsetsChanged |= (offset != previousOffset);

            asmState.setElementOffset(elem, offset);

            if (elem instanceof DexInstruction) {
              val insn = (DexInstruction) elem;

              Instruction[] asm = insn.assembleBytecode(asmState);
              for (val asmInsn : asm)
                offset += asmInsn.getSize(0); // argument ignored

              bytecode.addAll(Arrays.asList(asm));
            }
          }

          totalCodeLength = (int) offset;
        } while (offsetsChanged);

        // all is ready, let's create the result

        // create TryItems and EncodedCatchHandlers
        val elemOffsets = asmState.getElementOffsets();
        val tryItems = new ArrayList<TryItem>();
        val encodedCatchHandlers = new ArrayList<EncodedCatchHandler>();
        for (val elem : modifiedCode.instructionList)
          if (elem instanceof DexTryBlockEnd) {
            val tryEnd = (DexTryBlockEnd) elem;
            val tryStart = tryEnd.getBlockStart();

            val catchAllHandler = tryStart.getCatchAllHandler();
            int catchAllOffset;
            if (catchAllHandler == null)
              catchAllOffset = -1;
            else
              catchAllOffset = elemOffsets.get(catchAllHandler).intValue() + absoluteAddressOffset;

            val catchHandlers = tryStart.getCatchHandlers();
            val typeAddrPairs = new EncodedTypeAddrPair[catchHandlers.size()];
            int i = 0;
            for (val catchHandler : catchHandlers)
              typeAddrPairs[i++] = new EncodedTypeAddrPair(
                asmState.getCache().getType(catchHandler.getExceptionType()),
                elemOffsets.get(catchHandler).intValue() + absoluteAddressOffset);

            int tryStartAddr = elemOffsets.get(tryStart).intValue() + absoluteAddressOffset;
            int tryEndAddr = elemOffsets.get(tryEnd).intValue() + absoluteAddressOffset;

            if (tryStartAddr >= tryEndAddr)
              throw new InstructionAssemblyException("Try block of non-positive length");

            val encodedCatchHandler = new EncodedCatchHandler(typeAddrPairs, catchAllOffset);
            val tryItem = new TryItem(
              tryStartAddr,
              tryEndAddr - tryStartAddr,
              encodedCatchHandler);

            tryItems.add(tryItem);
            encodedCatchHandlers.add(encodedCatchHandler);
          }

        return new AssembledCode(bytecode, tryItems, encodedCatchHandlers, totalCodeLength);
      } catch (InstructionOffsetException e) {
        if (!allowJumpFix) // for testing only
          throw e;

        val problematicInsn = e.getProblematicInstruction();

        val newCode = new DexCode();
        for (val insn : modifiedCode.instructionList)
          if (insn == problematicInsn)
            newCode.addAll(problematicInsn.fixLongJump());
          else
            newCode.add(insn);

        modifiedCode = newCode;
      }
    }
  }

  public int getOutWords() {
    // outWords is the max of all inWords of methods in the code
    int maxWords = 0;

    for (val insn : this.instructionList) {
      if (insn instanceof DexInstruction_Invoke) {
        val insnInvoke = (DexInstruction_Invoke) insn;
        int insnOutWords = insnInvoke.getMethodPrototype().countParamWords(insnInvoke.isStaticCall());
        if (insnOutWords > maxWords)
          maxWords = insnOutWords;
      }
    }

    return maxWords;
  }

  private DexInstruction parseInstruction(Instruction insn, DexCode_ParsingState parsingState) throws InstructionParsingException {
    switch (insn.opcode) {

    case NOP:
      if (insn instanceof PackedSwitchDataPseudoInstruction)
        return new DexInstruction_PackedSwitchData(this, insn, parsingState);
      else if (insn instanceof ArrayDataPseudoInstruction)
        return new DexInstruction_FillArrayData(this, insn, parsingState);
      else
        return new DexInstruction_Nop(this, insn, parsingState);

    case MOVE:
    case MOVE_OBJECT:
    case MOVE_FROM16:
    case MOVE_OBJECT_FROM16:
    case MOVE_16:
    case MOVE_OBJECT_16:
      return new DexInstruction_Move(this, insn, parsingState);

    case MOVE_WIDE:
    case MOVE_WIDE_FROM16:
    case MOVE_WIDE_16:
      return new DexInstruction_MoveWide(this, insn, parsingState);

    case MOVE_RESULT:
    case MOVE_RESULT_OBJECT:
      return new DexInstruction_MoveResult(this, insn, parsingState);

    case MOVE_RESULT_WIDE:
      return new DexInstruction_MoveResultWide(this, insn, parsingState);

    case MOVE_EXCEPTION:
      return new DexInstruction_MoveException(this, insn, parsingState);

    case RETURN_VOID:
      return new DexInstruction_ReturnVoid(this, insn, parsingState);

    case RETURN:
    case RETURN_OBJECT:
      return new DexInstruction_Return(this, insn, parsingState);

    case RETURN_WIDE:
      return new DexInstruction_ReturnWide(this, insn, parsingState);

    case CONST_4:
    case CONST_16:
    case CONST:
    case CONST_HIGH16:
      return new DexInstruction_Const(this, insn, parsingState);

    case CONST_WIDE_16:
    case CONST_WIDE_32:
    case CONST_WIDE:
    case CONST_WIDE_HIGH16:
      return new DexInstruction_ConstWide(this, insn, parsingState);

    case CONST_STRING:
//    case CONST_STRING_JUMBO:
      return new DexInstruction_ConstString(this, insn, parsingState);

    case CONST_CLASS:
      return new DexInstruction_ConstClass(this, insn, parsingState);

    case MONITOR_ENTER:
    case MONITOR_EXIT:
      return new DexInstruction_Monitor(this, insn, parsingState);

    case CHECK_CAST:
      return new DexInstruction_CheckCast(this, insn, parsingState);

    case INSTANCE_OF:
      return new DexInstruction_InstanceOf(this, insn, parsingState);

    case NEW_INSTANCE:
      return new DexInstruction_NewInstance(this, insn, parsingState);

    case NEW_ARRAY:
      return new DexInstruction_NewArray(this, insn, parsingState);

    case THROW:
      return new DexInstruction_Throw(this, insn, parsingState);

    case GOTO:
    case GOTO_16:
    case GOTO_32:
      return new DexInstruction_Goto(this, insn, parsingState);

    case IF_EQ:
    case IF_NE:
    case IF_LT:
    case IF_GE:
    case IF_GT:
    case IF_LE:
      return new DexInstruction_IfTest(this, insn, parsingState);

    case IF_EQZ:
    case IF_NEZ:
    case IF_LTZ:
    case IF_GEZ:
    case IF_GTZ:
    case IF_LEZ:
      return new DexInstruction_IfTestZero(this, insn, parsingState);

    case SGET:
    case SGET_OBJECT:
    case SGET_BOOLEAN:
    case SGET_BYTE:
    case SGET_CHAR:
    case SGET_SHORT:
      return new DexInstruction_StaticGet(this, insn, parsingState);

    case SGET_WIDE:
      return new DexInstruction_StaticGetWide(this, insn, parsingState);

    case SPUT:
    case SPUT_OBJECT:
    case SPUT_BOOLEAN:
    case SPUT_BYTE:
    case SPUT_CHAR:
    case SPUT_SHORT:
      return new DexInstruction_StaticPut(this, insn, parsingState);

    case SPUT_WIDE:
      return new DexInstruction_StaticPutWide(this, insn, parsingState);

    case IGET:
    case IGET_OBJECT:
    case IGET_BOOLEAN:
    case IGET_BYTE:
    case IGET_CHAR:
    case IGET_SHORT:
      return new DexInstruction_InstanceGet(this, insn, parsingState);

    case IGET_WIDE:
      return new DexInstruction_InstanceGetWide(this, insn, parsingState);

    case IPUT:
    case IPUT_OBJECT:
    case IPUT_BOOLEAN:
    case IPUT_BYTE:
    case IPUT_CHAR:
    case IPUT_SHORT:
      return new DexInstruction_InstancePut(this, insn, parsingState);

    case IPUT_WIDE:
      return new DexInstruction_InstancePutWide(this, insn, parsingState);

    case AGET:
    case AGET_OBJECT:
    case AGET_BOOLEAN:
    case AGET_BYTE:
    case AGET_CHAR:
    case AGET_SHORT:
      return new DexInstruction_ArrayGet(this, insn, parsingState);

    case AGET_WIDE:
      return new DexInstruction_ArrayGetWide(this, insn, parsingState);

    case APUT:
    case APUT_OBJECT:
    case APUT_BOOLEAN:
    case APUT_BYTE:
    case APUT_CHAR:
    case APUT_SHORT:
      return new DexInstruction_ArrayPut(this, insn, parsingState);

    case APUT_WIDE:
      return new DexInstruction_ArrayPutWide(this, insn, parsingState);

    case INVOKE_VIRTUAL:
    case INVOKE_SUPER:
    case INVOKE_DIRECT:
    case INVOKE_STATIC:
    case INVOKE_INTERFACE:
    case INVOKE_VIRTUAL_RANGE:
    case INVOKE_SUPER_RANGE:
    case INVOKE_DIRECT_RANGE:
    case INVOKE_STATIC_RANGE:
    case INVOKE_INTERFACE_RANGE:
      return new DexInstruction_Invoke(this, insn, parsingState);

    case NEG_INT:
    case NOT_INT:
    case NEG_FLOAT:
      return new DexInstruction_UnaryOp(this, insn, parsingState);

    case NEG_LONG:
    case NOT_LONG:
    case NEG_DOUBLE:
      return new DexInstruction_UnaryOpWide(this, insn, parsingState);

    case INT_TO_FLOAT:
    case FLOAT_TO_INT:
    case INT_TO_BYTE:
    case INT_TO_CHAR:
    case INT_TO_SHORT:
      return new DexInstruction_Convert(this, insn, parsingState);

    case INT_TO_LONG:
    case INT_TO_DOUBLE:
    case FLOAT_TO_LONG:
    case FLOAT_TO_DOUBLE:
      return new DexInstruction_ConvertToWide(this, insn, parsingState);

    case LONG_TO_INT:
    case DOUBLE_TO_INT:
    case LONG_TO_FLOAT:
    case DOUBLE_TO_FLOAT:
      return new DexInstruction_ConvertFromWide(this, insn, parsingState);

    case LONG_TO_DOUBLE:
    case DOUBLE_TO_LONG:
      return new DexInstruction_ConvertWide(this, insn, parsingState);

    case ADD_INT:
    case SUB_INT:
    case MUL_INT:
    case DIV_INT:
    case REM_INT:
    case AND_INT:
    case OR_INT:
    case XOR_INT:
    case SHL_INT:
    case SHR_INT:
    case USHR_INT:
    case ADD_FLOAT:
    case SUB_FLOAT:
    case MUL_FLOAT:
    case DIV_FLOAT:
    case REM_FLOAT:
    case ADD_INT_2ADDR:
    case SUB_INT_2ADDR:
    case MUL_INT_2ADDR:
    case DIV_INT_2ADDR:
    case REM_INT_2ADDR:
    case AND_INT_2ADDR:
    case OR_INT_2ADDR:
    case XOR_INT_2ADDR:
    case SHL_INT_2ADDR:
    case SHR_INT_2ADDR:
    case USHR_INT_2ADDR:
    case ADD_FLOAT_2ADDR:
    case SUB_FLOAT_2ADDR:
    case MUL_FLOAT_2ADDR:
    case DIV_FLOAT_2ADDR:
    case REM_FLOAT_2ADDR:
      return new DexInstruction_BinaryOp(this, insn, parsingState);

    case ADD_LONG:
    case SUB_LONG:
    case MUL_LONG:
    case DIV_LONG:
    case REM_LONG:
    case AND_LONG:
    case OR_LONG:
    case XOR_LONG:
    case SHL_LONG:
    case SHR_LONG:
    case USHR_LONG:
    case ADD_DOUBLE:
    case SUB_DOUBLE:
    case MUL_DOUBLE:
    case DIV_DOUBLE:
    case REM_DOUBLE:
    case ADD_LONG_2ADDR:
    case SUB_LONG_2ADDR:
    case MUL_LONG_2ADDR:
    case DIV_LONG_2ADDR:
    case REM_LONG_2ADDR:
    case AND_LONG_2ADDR:
    case OR_LONG_2ADDR:
    case XOR_LONG_2ADDR:
    case SHL_LONG_2ADDR:
    case SHR_LONG_2ADDR:
    case USHR_LONG_2ADDR:
    case ADD_DOUBLE_2ADDR:
    case SUB_DOUBLE_2ADDR:
    case MUL_DOUBLE_2ADDR:
    case DIV_DOUBLE_2ADDR:
    case REM_DOUBLE_2ADDR:
      return new DexInstruction_BinaryOpWide(this, insn, parsingState);

    case ADD_INT_LIT16:
    case ADD_INT_LIT8:
    case RSUB_INT:
    case RSUB_INT_LIT8:
    case MUL_INT_LIT16:
    case MUL_INT_LIT8:
    case DIV_INT_LIT16:
    case DIV_INT_LIT8:
    case REM_INT_LIT16:
    case REM_INT_LIT8:
    case AND_INT_LIT16:
    case AND_INT_LIT8:
    case OR_INT_LIT16:
    case OR_INT_LIT8:
    case XOR_INT_LIT16:
    case XOR_INT_LIT8:
    case SHL_INT_LIT8:
    case SHR_INT_LIT8:
    case USHR_INT_LIT8:
      return new DexInstruction_BinaryOpLiteral(this, insn, parsingState);

    case PACKED_SWITCH:
      return new DexInstruction_PackedSwitch(this, insn, parsingState);

    case FILL_ARRAY_DATA:
      return new DexInstruction_FillArray(this, insn, parsingState);

    default:
      // TODO: throw exception
      return new DexInstruction_Unknown(this, insn);
    }
  }

  public void markAllInstructionsOriginal() {
    for (val elem : instructionList)
      elem.setOriginalElement(true);
  }
}
