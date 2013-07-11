package uk.ac.cam.db538.dexter.dex.code;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import lombok.val;

import org.jf.dexlib.CodeItem;
import org.jf.dexlib.CodeItem.EncodedCatchHandler;
import org.jf.dexlib.CodeItem.TryItem;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Format.ArrayDataPseudoInstruction;
import org.jf.dexlib.Code.Format.PackedSwitchDataPseudoInstruction;
import org.jf.dexlib.Code.Format.SparseSwitchDataPseudoInstruction;

import uk.ac.cam.db538.dexter.dex.Dex;
import uk.ac.cam.db538.dexter.dex.DexClass;
import uk.ac.cam.db538.dexter.dex.DexInstrumentationCache;
import uk.ac.cam.db538.dexter.dex.InstrumentationException;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeStart;
import uk.ac.cam.db538.dexter.dex.code.elem.DexLabel;
import uk.ac.cam.db538.dexter.dex.code.elem.DexTryBlockEnd;
import uk.ac.cam.db538.dexter.dex.code.elem.DexTryBlockStart;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ArrayGet;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ArrayGetWide;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ArrayLength;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ArrayPut;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ArrayPutWide;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_BinaryOp;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_BinaryOpLiteral;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_BinaryOpWide;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_CheckCast;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_CompareFloat;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_CompareWide;
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
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_FilledNewArray;
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
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_PackedSwitchData;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Return;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ReturnVoid;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ReturnWide;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_SparseSwitchData;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_StaticGet;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_StaticGetWide;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_StaticPut;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_StaticPutWide;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Switch;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Throw;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_UnaryOp;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_UnaryOpWide;
import uk.ac.cam.db538.dexter.dex.code.insn.InstructionParseError;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_BinaryOp;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_GetPut;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_IfTestZero;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_Invoke;
import uk.ac.cam.db538.dexter.dex.code.insn.invoke.DexPseudoinstruction_Invoke;
import uk.ac.cam.db538.dexter.dex.code.insn.macro.DexMacro;
import uk.ac.cam.db538.dexter.dex.code.insn.macro.DexMacro_FilledNewArray;
import uk.ac.cam.db538.dexter.dex.code.insn.macro.DexMacro_GetInternalClassAnnotation;
import uk.ac.cam.db538.dexter.dex.code.insn.macro.DexMacro_GetMethodCaller;
import uk.ac.cam.db538.dexter.dex.code.insn.macro.DexMacro_GetObjectTaint;
import uk.ac.cam.db538.dexter.dex.code.insn.macro.DexMacro_PrintInteger;
import uk.ac.cam.db538.dexter.dex.code.insn.macro.DexMacro_PrintStringConst;
import uk.ac.cam.db538.dexter.dex.code.insn.macro.DexMacro_SetObjectTaint;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexPrototype;
import uk.ac.cam.db538.dexter.dex.type.DexTypeCache;
import uk.ac.cam.db538.dexter.dex.type.DexType;
import uk.ac.cam.db538.dexter.dex.type.DexPrimitiveType;
import uk.ac.cam.db538.dexter.hierarchy.RuntimeHierarchy;
import uk.ac.cam.db538.dexter.utils.InstructionList;
import uk.ac.cam.db538.dexter.utils.Pair;

public class DexCode {

  private final RuntimeHierarchy hierarchy;
  private final InstructionList instructionList;

  public DexCode(CodeItem codeItem, RuntimeHierarchy hierarchy) {
	  this.hierarchy = hierarchy;
	  this.instructionList = CodeParser.parse(codeItem, hierarchy); 
  }
  
  private void parseInstructions(CodeItem codeItem) {
	  val instructions = codeItem.getInstructions();
	  val catchHandlers = codeItem.getHandlers();
	  val tries = codeItem.getTries();
	  
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

  public Set<DexRegister> getUsedRegisters() {
    val set = new HashSet<DexRegister>();
    for (val elem : instructionList)
      set.addAll(elem.lvaUsedRegisters());
    return set;
  }

  public Set<DexTryBlockEnd> getTryBlocks() {
    val set = new HashSet<DexTryBlockEnd>();
    for (val elem : instructionList)
      if (elem instanceof DexTryBlockEnd)
        set.add((DexTryBlockEnd) elem);
    return set;
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

  public void add(DexCodeElement elem) {
    instructionList.add(elem);
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
  }

  public void insertBefore(List<DexCodeElement> elem, DexCodeElement before) {
    instructionList.addAll(findElement(before), elem);
  }

  public void insertAfter(DexCodeElement elem, DexCodeElement after) {
    instructionList.add(findElement(after) + 1, elem);
  }

  public void insertAfter(DexCodeElement[] elems, DexCodeElement after) {
    instructionList.addAll(findElement(after) + 1, Arrays.asList(elems));
  }

  public void replace(DexCodeElement elem, DexCodeElement[] replacement) {
    replace(elem, Arrays.asList(replacement));
  }

  public void replace(DexCodeElement elem, List<DexCodeElement> replacement) {
    int index = findElement(elem);
    instructionList.remove(index);
    instructionList.addAll(index, replacement);
  }

  public boolean isBetween(DexCodeElement elemStart, DexCodeElement elemEnd, DexCodeElement elemSought) {
    boolean hitStart = false, hitEnd = false;

    for (val elem : instructionList) {
      // order of the ifs matters here!
      // if elemSought equals elemStart or elemEnd, we still want to return true
      if (elem == elemStart)
        hitStart = true;
      if (elem == elemSought)
        return hitStart && !hitEnd;
      if (elem == elemEnd)
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

  public DexInstruction getFollowingInstruction(DexCodeElement elem) {
    val elemIndex = instructionList.indexOf(elem);
    if (elemIndex < 0)
      throw new NoSuchElementException();

    val nextInsnInfo = nextInstruction(instructionList, elemIndex);
    if (nextInsnInfo == null)
      return null;

    return nextInsnInfo.getValA();
  }

  private void generatePseudoinstructions() {
    val insns = instructionList;
    val codeLength = insns.size();
    val newInsns = new InstructionList(codeLength);

    for (int i = 0; i < codeLength; i++) {
      val thisInsn = insns.get(i);

      if (thisInsn instanceof DexInstruction_Invoke) {
        val nextInsnPair = nextInstruction(insns, i);

        // replace INVOKE & MOVE_RESULT pairs with a single Invoke pseudoinstruction
        if (nextInsnPair != null &&
            (nextInsnPair.getValA() instanceof DexInstruction_MoveResult) ||
            (nextInsnPair.getValA() instanceof DexInstruction_MoveResultWide)) {
          newInsns.add(new DexPseudoinstruction_Invoke(
                         this,
                         (DexInstruction_Invoke) thisInsn,
                         (DexInstruction) nextInsnPair.getValA()));
          // add the non-instructions which might be between the call and result move
          for (int j = i + 1; j < nextInsnPair.getValB(); ++j) {
            val middleInsn = insns.get(j);
            if (middleInsn instanceof DexTryBlockEnd)
              newInsns.add(middleInsn);
            else
              throw new InstrumentationException("Unexpected jump-to code element between invoke and move-result");
          }
          // jump to the following insn
          i = nextInsnPair.getValB();

        } else
          // to conform, replace other INVOKEs as well
          newInsns.add(new DexPseudoinstruction_Invoke(
                         this,
                         (DexInstruction_Invoke) thisInsn));
      } else if (thisInsn instanceof DexInstruction_FilledNewArray) {

        val nextInsnPair = nextInstruction(insns, i);

        // replace FILLED_NEW_ARRAY & MOVE_RESULT pairs with a single FilledNewArray pseudoinstruction
        if (nextInsnPair != null && nextInsnPair.getValA() instanceof DexInstruction_MoveResult) {
          newInsns.add(new DexMacro_FilledNewArray(
                         this,
                         (DexInstruction_FilledNewArray) thisInsn,
                         (DexInstruction_MoveResult) nextInsnPair.getValA()));
          // add the non-instructions which might be between the call and result move
          for (int j = i + 1; j < nextInsnPair.getValB(); ++j) {
            val middleInsn = insns.get(j);
            if (middleInsn instanceof DexTryBlockEnd)
              newInsns.add(middleInsn);
            else
              throw new InstrumentationException("Unexpected jump-to code element between invoke and move-result");
          }
          // jump to the following insn
          i = nextInsnPair.getValB();

        } else
          throw new InstrumentationException("FilledNewArray instruction must be followed by a MoveResult");

      } else
        newInsns.add(thisInsn);
    }

    replaceInstructions(newInsns);
  }

  private void unwrapPseudoinstructions() {
    boolean unwrappedSomething;
    do {
      unwrappedSomething = false;

      val insns = instructionList;
      val codeLength = insns.size();
      val newInsns = new InstructionList(codeLength);

      for (val insn : insns)
        if (insn instanceof DexMacro) {
          newInsns.addAll(((DexMacro) insn).unwrap());
          unwrappedSomething = true;
        } else
          newInsns.add(insn);

      replaceInstructions(newInsns);
    } while (unwrappedSomething);

//    // swap move-results and try-ends back
//    // TODO: not perfect (doesn't work when Invoke is instrumented)
//    for (int i = 0; i < instructionList.size() - 1; ++i) {
//    	val insn1 = instructionList.get(i);
//    	val insn2 = instructionList.get(i + 1);
//
//    	if ((insn1 instanceof DexInstruction_MoveResult || insn1 instanceof DexInstruction_MoveResultWide) &&
//    			insn2 instanceof DexTryBlockEnd) {
//    		instructionList.set(i, insn2);
//    		instructionList.set(i + 1, insn1);
//    	}
//    }
  }

  private void fixOverlappingTryBlocks() {
    boolean somethingChanged = true;
    while (somethingChanged) {
      somethingChanged = false;

      val tryBlocks = this.getTryBlocks();
      for (val outerBlock : tryBlocks)
        for (val innerBlock : tryBlocks) {
          if (outerBlock != innerBlock) {
            boolean startInside = isBetween(outerBlock.getBlockStart(), outerBlock, innerBlock.getBlockStart());
            boolean endInside = isBetween(outerBlock.getBlockStart(), outerBlock, innerBlock);

            if (startInside && endInside) {
              // inner block really nested
              //System.out.println("overlapping try blocks in " + getParentClass().getType().getPrettyName() + "." + getParentMethod().getName());

              val outerNewEnd = new DexTryBlockEnd(this, outerBlock.getBlockStart());
              val outerNewStart = new DexTryBlockStart(outerBlock.getBlockStart());

              insertBefore(outerNewEnd, innerBlock.getBlockStart());
              insertAfter(outerNewStart, innerBlock);
              outerBlock.setBlockStart(outerNewStart);

              somethingChanged = true;
            } else if (startInside || endInside) {
              throw new InstrumentationException("Try blocks overlapping but not nested");
            }
          }
        }
    }
  }

  public void instrument(DexInstrumentationCache cache) {
    DexRegister.resetCounter();
    instrumentationState = new DexCode_InstrumentationState(this, cache);

    generatePseudoinstructions();

    val insns = new HashSet<DexInstruction>();
    for (val elem : instructionList)
      if (elem instanceof DexInstruction)
        insns.add((DexInstruction) elem);

    for (val insn : insns)
      if (!insn.isAuxiliaryElement())
        insn.instrument(instrumentationState);

    if (instrumentationState.isNeedsCallInstrumentation())
      insertCallHandling(instrumentationState);

    unwrapPseudoinstructions();
    fixOverlappingTryBlocks();
  }

  private void insertCallHandling(DexCode_InstrumentationState state) {
    val printDebug = state.getCache().isInsertDebugLogging();

    val addedCode = new InstructionList();
    val dex = getParentFile();
    val parsingCache = dex.getTypeCache();
    val semaphoreClass = DexClassType.parse("Ljava/util/concurrent/Semaphore;", parsingCache);
    boolean hasPrimitiveArgument = parentMethod.getMethodDef().getMethodId().getPrototype().hasPrimitiveArgument();
    boolean staticMethod = parentMethod.getMethodDef().isStatic();
    boolean constructorMethod = parentMethod.getMethodDef().isConstructor();

    // need to do different things for static/direct and virtual methods
    // static/direct methods can never be called from external origin
    // (they are defined internally, so they couldn't be referenced from outside)
    boolean virtualMethod = parentMethod.isVirtual();

    // TEST CALL ORIGIN
    // first, decide if the method was called from external or internal code

    DexLabel labelExternalCallOrigin = null;
    DexLabel labelEnd = null;

    // if this isn't a static call, get the taint of the 'this' object
    DexRegister regThis = null;
    DexRegister regThisTaint = null;
    if (!staticMethod && !constructorMethod) {
      // get the 'this' object taint
      regThis = parentMethod.getParameterMappedRegisters().get(0);
      regThisTaint = instrumentationState.getTaintRegister(regThis);
      addedCode.add(new DexMacro_GetObjectTaint(this, regThisTaint, regThis));
    }

    if (virtualMethod) {
      labelExternalCallOrigin = new DexLabel(this);
      labelEnd = new DexLabel(this);

      val regCallersName = new DexRegister();
      val regInternalAnnotation = instrumentationState.getInternalClassAnnotationRegister();

      addedCode.add(new DexInstruction_Const(this, regInternalAnnotation, 0));
      addedCode.add(new DexMacro_GetMethodCaller(this, regCallersName));
      addedCode.add(new DexInstruction_IfTestZero(this, regCallersName, labelExternalCallOrigin, Opcode_IfTestZero.eqz));
      addedCode.add(new DexMacro_GetInternalClassAnnotation(this, regInternalAnnotation, regCallersName));
      addedCode.add(new DexInstruction_IfTestZero(this, regInternalAnnotation, labelExternalCallOrigin, Opcode_IfTestZero.eqz));
    }

    {
      // INTERNAL CALL ORIGIN

      if (printDebug) {
        addedCode.add(
          new DexMacro_PrintStringConst(
            this,
            "$# entering method " +
            getParentClass().getClassDef().getType().getPrettyName() +
            "->" + parentMethod.getMethodDef().getMethodId().getName() +
            " (internal origin)",
            true));
      }

      val regArray = new DexRegister();
      val regIndex = new DexRegister();
      val regSemaphore = new DexRegister();
      val regArrayElement = new DexRegister();
      
      // get the ARG array
      if (hasPrimitiveArgument)
        addedCode.add(new DexInstruction_StaticGet(this, regArray, dex.getAuxiliaryDex().getField_CallParamTaint()));

      int paramRegIndex = staticMethod ? 0 : 1;
      int paramTaintArrayIndex = 0;
      for (val paramType : parentMethod.getMethodDef().getMethodId().getPrototype().getParameterTypes()) {
        if (paramType instanceof DexPrimitiveType) {
          // for primitives, put the taint information in their respective taint registers
          val regParamMapping = parentMethod.getParameterMappedRegisters().get(paramRegIndex);
          val regTaintParamMapping = instrumentationState.getTaintRegister(regParamMapping);

          addedCode.add(new DexInstruction_Const(this, regIndex, paramTaintArrayIndex));
          addedCode.add(new DexInstruction_ArrayGet(this, regArrayElement, regArray, regIndex, Opcode_GetPut.IntFloat));

          if (staticMethod || constructorMethod)
            addedCode.add(new DexInstruction_Move(this, regTaintParamMapping, regArrayElement, false));
          else
            addedCode.add(new DexInstruction_BinaryOp(this, regTaintParamMapping, regArrayElement, regThisTaint, Opcode_BinaryOp.OrInt));

          if (printDebug) {
            addedCode.add(new DexMacro_PrintStringConst(this,
                          "$ " + parentMethod.getMethodDef().toString() + ": " +
                          "ARG[" + paramTaintArrayIndex + "] = ",
                          false));
            addedCode.add(new DexMacro_PrintInteger(this, regArrayElement, true));
          }

          paramTaintArrayIndex++;
        } else {
          // for objects, assign the taint of the 'this' object
          if (!staticMethod && !constructorMethod) {
            val regParamMapping = parentMethod.getParameterMappedRegisters().get(paramRegIndex);
            addedCode.add(new DexMacro_SetObjectTaint(this, regParamMapping, regThisTaint));
          }
        }
        paramRegIndex += paramType.getRegisters();
      }

      if (hasPrimitiveArgument) {
        addedCode.add(new DexInstruction_StaticGet(
                        this,
                        regSemaphore,
                        dex.getAuxiliaryDex().getField_CallParamSemaphore()));
        addedCode.add(new DexInstruction_Invoke(
                        this,
                        semaphoreClass,
                        "release",
                        new DexPrototype(DexType.parse("V", parsingCache), null),
                        Arrays.asList(new DexRegister[] { regSemaphore }),
                        Opcode_Invoke.Virtual));
      }
    }

    if (virtualMethod) { // by definition, the method can't be static or constructor, if it is virtual

      addedCode.add(new DexInstruction_Goto(this, labelEnd));

      // EXTERNAL CALL ORIGIN

      addedCode.add(labelExternalCallOrigin);

      if (printDebug) {
        addedCode.add(
          new DexMacro_PrintStringConst(
            this,
            "$# entering method " +
            parentMethod.getMethodDef().toString() +
            " (external origin)",
            true));
      }

      int paramRegIndex = 1;
      for (val paramType : parentMethod.getMethodDef().getMethodId().getPrototype().getParameterTypes()) {
        // assign the taint information of 'this' object to all the params
        if (paramType instanceof DexPrimitiveType) {
          val regParamMapping = parentMethod.getParameterMappedRegisters().get(paramRegIndex);
          val regTaintParamMapping = instrumentationState.getTaintRegister(regParamMapping);
          addedCode.add(new DexInstruction_Move(this, regTaintParamMapping, regThisTaint, false));
        } else {
          val regParamMapping = parentMethod.getParameterMappedRegisters().get(paramRegIndex);
          addedCode.add(new DexMacro_SetObjectTaint(this, regParamMapping, regThisTaint));
        }
        paramRegIndex += paramType.getRegisters();
      }

      // END

      addedCode.add(labelEnd);
    }

    insertBefore(addedCode, startingLabel);
  }

  public void replaceInstructions(List<DexCodeElement> newInsns) {
    instructionList.clear();
    addAll(newInsns);
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

  public void markAllInstructionsOriginal() {
    for (val elem : instructionList) {
      elem.setOriginalElement(true);
    }
  }

  public void countInstructions(HashMap<Class<?>, Integer> count) {
    for (val elem : instructionList)
      if (elem instanceof DexInstruction) {
        val clazz = elem.getClass();
        int clazzCount;
        if (count.containsKey(clazz)) clazzCount = count.get(clazz);
        else clazzCount = 0;
        count.put(clazz, clazzCount + 1);
      }
  }
}
