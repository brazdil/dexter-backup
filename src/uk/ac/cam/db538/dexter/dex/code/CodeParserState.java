package uk.ac.cam.db538.dexter.dex.code;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.CodeItem.EncodedCatchHandler;
import org.jf.dexlib.CodeItem.TryItem;

import uk.ac.cam.db538.dexter.dex.code.elem.DexCatch;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCatchAll;
import uk.ac.cam.db538.dexter.dex.code.elem.DexLabel;
import uk.ac.cam.db538.dexter.dex.code.elem.DexTryBlockEnd;
import uk.ac.cam.db538.dexter.dex.code.elem.DexTryBlockStart;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction;
import uk.ac.cam.db538.dexter.dex.code.insn.InstructionParseError;
import uk.ac.cam.db538.dexter.dex.code.reg.DexSingleOriginalRegister;
import uk.ac.cam.db538.dexter.dex.code.reg.DexSingleRegister;
import uk.ac.cam.db538.dexter.dex.code.reg.DexWideOriginalRegister;
import uk.ac.cam.db538.dexter.dex.code.reg.DexWideRegister;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexTypeCache;
import uk.ac.cam.db538.dexter.hierarchy.RuntimeHierarchy;
import uk.ac.cam.db538.dexter.utils.Cache;
import uk.ac.cam.db538.dexter.utils.Pair;

public class CodeParserState {
	
	@Getter private final RuntimeHierarchy hierarchy;

	private final Map<Long, DexInstruction> instructionParents;

	private final Cache<Integer, DexSingleRegister> cacheSingleReg;
	private final Cache<Integer, DexWideRegister> cacheWideReg;
	
	private final Cache<Long, DexLabel> cacheLabels;
	private final Cache<Long, DexCatchAll> cacheCatchALl;
	private final Cache<Pair<Long, DexClassType>, DexCatch> catchOffsetCache;
	
  public CodeParserState(RuntimeHierarchy hierarchy) {
	this.hierarchy = hierarchy;

    this.instructionParents = new HashMap<Long, DexInstruction>();
	
	this.cacheSingleReg = new Cache<Integer, DexSingleRegister>() {
		@Override
		protected DexSingleRegister createNewEntry(Integer key) {
			return new DexSingleOriginalRegister(key);
		}
	};
	
	this.cacheWideReg = new Cache<Integer, DexWideRegister>() {
		@Override
		protected DexWideRegister createNewEntry(Integer key) {
			return new DexWideOriginalRegister(key);
		}
	};
	
    this.cacheLabels = new Cache<Long, DexLabel>() {
    	protected DexLabel createNewEntry(Long absoluteOffset) {
    		return new DexLabel();
    	}
    };
 
    this.cacheCatchALl = DexCatchAll.createCache(code);
    this.catchOffsetCache = DexCatch.createCache(code);

  }

	public DexSingleRegister getSingleRegister(int id) {
		return cacheSingleReg.getCachedEntry(id);
	}

	public DexWideRegister getWideRegister(int id) {
		return cacheWideReg.getCachedEntry(id);
	}

  public DexLabel getLabel(long relativeInsnOffset) {
    long absoluteOffset = currentOffset + relativeInsnOffset;
    return cacheLabels.getCachedEntry(absoluteOffset);
  }

  public DexLabel getLabel(long relativeInsnOffset, DexInstruction relativeTo) {
    if (!instructionOffsets.containsValue(relativeTo))
      throw new InstructionParseError("Cannot find zero-offset instruction");

    long zeroPoint = 0;
    for (val entry : instructionOffsets.entrySet())
      if (entry.getValue().equals(relativeTo))
        zeroPoint = entry.getKey();

    long absoluteOffset = zeroPoint + relativeInsnOffset;
    return cacheLabels.getCachedEntry(absoluteOffset);
  }

  public DexCatchAll getCatchAll(long handlerOffset) {
    return cacheCatchALl.getCachedEntry(handlerOffset);
  }

  public DexCatch getCatch(long handlerOffset, DexClassType exceptionType) {
    return catchOffsetCache.getCachedEntry(new Pair<Long, DexClassType>(handlerOffset, exceptionType));
  }

  public void addInstruction(long size, DexInstruction insn) {
    instructionOffsets.put(currentOffset, insn);
    currentOffset += size;
  }

  public void registerParentInstruction(DexInstruction parent, long childOffset) {
    instructionParents.put(currentOffset + childOffset, parent);
  }

  public DexInstruction getCurrentOffsetParent() {
    return instructionParents.get(currentOffset);
  }

  public void placeLabels() {
    for (val entry : cacheLabels.entrySet()) {
      val labelOffset = entry.getKey();
      val insnAtOffset = instructionOffsets.get(labelOffset);
      if (insnAtOffset == null)
        throw new InstructionParseError("Label could not be placed (non-existent offset " + labelOffset + ")");
      else {
        val label = entry.getValue();
        code.insertBefore(label, insnAtOffset);
      }
    }
  }

  public void placeCatches(EncodedCatchHandler[] encodedCatchHandlers) {
    if (encodedCatchHandlers == null)
      return;

    val placedCatchAllHandlers = new HashSet<DexCatchAll>();
    val placedCatchHandlers = new HashSet<DexCatch>();

    for (val encodedCatchHandler : encodedCatchHandlers) {

      // place catch all handler

      long allHandlerOffset = encodedCatchHandler.getCatchAllHandlerAddress();
      if (allHandlerOffset != -1L) {
        val insnAtOffset = instructionOffsets.get(allHandlerOffset);
        if (insnAtOffset == null)
          throw new InstructionParseError("CatchAll handler could not be placed (non-existent offset " + allHandlerOffset + ")");

        val catchAllElem = getCatchAll(allHandlerOffset);
        if (!placedCatchAllHandlers.contains(catchAllElem)) {
          code.insertBefore(catchAllElem, insnAtOffset);
          placedCatchAllHandlers.add(catchAllElem);
        }
      }

      // place individual handlers

      if (encodedCatchHandler.handlers == null)
        continue;

      for(val catchHandler : encodedCatchHandler.handlers) {
        long handlerOffset = catchHandler.getHandlerAddress();

        val insnAtOffset = instructionOffsets.get(handlerOffset);
        if (insnAtOffset == null)
          throw new InstructionParseError("Catch handler could not be placed (non-existent offset " + handlerOffset + ")");

        val catchElem = getCatch(handlerOffset, DexClassType.parse(catchHandler.exceptionType.getTypeDescriptor(), cache));
        if (!placedCatchHandlers.contains(catchElem)) {
          code.insertBefore(catchElem, insnAtOffset);
          placedCatchHandlers.add(catchElem);
        }
      }
    }

  }

  public void placeTries(TryItem[] tries) {
    if (tries == null)
      return;

    for (val tryBlock : tries) {
      long startOffset = tryBlock.getStartCodeAddress();
      long endOffset = startOffset + tryBlock.getTryLength();

      val startInsn = instructionOffsets.get(startOffset);
      if (startInsn == null)
        throw new InstructionParseError("Start of a try block could not be placed (non-existent offset " + startOffset + ")");

      DexCatchAll catchAllHandler = null;
      if (tryBlock.encodedCatchHandler != null) {
        long catchAllOffset = tryBlock.encodedCatchHandler.getCatchAllHandlerAddress();
        if (catchAllOffset != -1L)
          catchAllHandler = getCatchAll(catchAllOffset);
      }

      val catchHandlers = new ArrayList<DexCatch>();
      if (tryBlock.encodedCatchHandler != null && tryBlock.encodedCatchHandler.handlers != null)
        for (val catchBlock : tryBlock.encodedCatchHandler.handlers)
          catchHandlers.add(
            getCatch(catchBlock.getHandlerAddress(),
                     DexClassType.parse(catchBlock.exceptionType.getTypeDescriptor(), cache)));

      val newBlockStart = new DexTryBlockStart(code, startOffset, catchAllHandler, catchHandlers);
      val newBlockEnd = new DexTryBlockEnd(code, newBlockStart);

      code.insertBefore(newBlockStart, startInsn);

      if (endOffset == currentOffset) {
        // current offset should equal to total length of the instruction block
        // by the time this method is called
        code.add(newBlockEnd);
      } else {
        val endInsn = instructionOffsets.get(endOffset);
        if (endInsn == null)
          throw new InstructionParseError("End of a try block could not be placed (non-existent offset " + endOffset + ")");
        code.insertBefore(newBlockEnd, endInsn);
      }
    }
  }

  public void checkTryCatchBlocksPlaced() {
    val insns = code.getInstructionList();
    for (val elem : insns)
      if (elem instanceof DexTryBlockStart) {
        val tryBlockStart = (DexTryBlockStart) elem;

        val catchAllHandler = tryBlockStart.getCatchAllHandler();
        if (catchAllHandler != null && !insns.contains(catchAllHandler))
          throw new InstructionParseError("CatchAll block hasn't been placed - DEX inconsistent");

        for (val catchHandler : tryBlockStart.getCatchHandlers())
          if (!insns.contains(catchHandler))
            throw new InstructionParseError("Catch block hasn't been placed - DEX inconsistent");
      }
  }
}
