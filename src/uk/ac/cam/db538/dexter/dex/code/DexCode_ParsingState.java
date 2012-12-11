package uk.ac.cam.db538.dexter.dex.code;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.CodeItem.EncodedCatchHandler;
import org.jf.dexlib.CodeItem.TryItem;

import uk.ac.cam.db538.dexter.dex.DexParsingCache;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCatch;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCatchAll;
import uk.ac.cam.db538.dexter.dex.code.elem.DexLabel;
import uk.ac.cam.db538.dexter.dex.code.elem.DexTryBlockEnd;
import uk.ac.cam.db538.dexter.dex.code.elem.DexTryBlockStart;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction;
import uk.ac.cam.db538.dexter.dex.code.insn.InstructionParsingException;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.utils.Cache;
import uk.ac.cam.db538.dexter.utils.Pair;

public class DexCode_ParsingState {
  private final Cache<Integer, DexRegister> registerIdCache;
  private final Cache<Long, DexLabel> labelOffsetCache;
  private final Cache<Long, DexCatchAll> catchAllOffsetCache;
  private final Cache<Pair<Long, DexClassType>, DexCatch> catchOffsetCache;
  private final Map<Long, DexInstruction> instructionOffsetMap;
  private long currentOffset;
  @Getter private final DexParsingCache cache;
  @Getter private final DexCode code;

  public DexCode_ParsingState(DexParsingCache cache, DexCode code) {
    this.registerIdCache = DexRegister.createCache();
    this.labelOffsetCache = DexLabel.createCache(code);
    this.catchAllOffsetCache = DexCatchAll.createCache(code);
    this.catchOffsetCache = DexCatch.createCache(code);

    this.instructionOffsetMap = new HashMap<Long, DexInstruction>();
    this.cache = cache;
    this.code = code;
  }

  public DexRegister getRegister(int id) {
    return registerIdCache.getCachedEntry(id);
  }

  public boolean containsRegisterId(int id) {
    return registerIdCache.contains(id);
  }

  public DexLabel getLabel(long insnOffset) {
    long absoluteOffset = currentOffset + insnOffset;
    return labelOffsetCache.getCachedEntry(absoluteOffset);
  }

  public DexCatchAll getCatchAll(long handlerOffset) {
    return catchAllOffsetCache.getCachedEntry(handlerOffset);
  }

  public DexCatch getCatch(long handlerOffset, DexClassType exceptionType) {
    return catchOffsetCache.getCachedEntry(new Pair<Long, DexClassType>(handlerOffset, exceptionType));
  }

  public void addInstruction(long size, DexInstruction insn) {
    instructionOffsetMap.put(currentOffset, insn);
    currentOffset += size;
    code.add(insn);
  }

  public void placeLabels() {
    for (val entry : labelOffsetCache.entrySet()) {
      val labelOffset = entry.getKey();
      val insnAtOffset = instructionOffsetMap.get(labelOffset);
      if (insnAtOffset == null)
        throw new InstructionParsingException("Label could not be placed (non-existent offset " + labelOffset + ")");
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
        val insnAtOffset = instructionOffsetMap.get(allHandlerOffset);
        if (insnAtOffset == null)
          throw new InstructionParsingException("CatchAll handler could not be placed (non-existent offset " + allHandlerOffset + ")");

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

        val insnAtOffset = instructionOffsetMap.get(handlerOffset);
        if (insnAtOffset == null)
          throw new InstructionParsingException("Catch handler could not be placed (non-existent offset " + handlerOffset + ")");

        val catchElem = getCatch(handlerOffset, cache.getClassType(catchHandler.exceptionType.getTypeDescriptor()));
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

      val startInsn = instructionOffsetMap.get(startOffset);
      if (startInsn == null)
        throw new InstructionParsingException("Start of a try block could not be placed (non-existent offset " + startOffset + ")");

      DexCatchAll catchAllHandler = null;
      if (tryBlock.encodedCatchHandler != null) {
        long catchAllOffset = tryBlock.encodedCatchHandler.getCatchAllHandlerAddress();
        if (catchAllOffset != -1L)
          catchAllHandler = getCatchAll(catchAllOffset);
      }

      val catchHandlers = new HashSet<DexCatch>();
      if (tryBlock.encodedCatchHandler != null && tryBlock.encodedCatchHandler.handlers != null)
        for (val catchBlock : tryBlock.encodedCatchHandler.handlers)
          catchHandlers.add(
            getCatch(catchBlock.getHandlerAddress(),
                     cache.getClassType(catchBlock.exceptionType.getTypeDescriptor())));

      val newBlockStart = new DexTryBlockStart(code, startOffset, catchAllHandler, catchHandlers);
      val newBlockEnd = new DexTryBlockEnd(code, newBlockStart);

      code.insertBefore(newBlockStart, startInsn);

      if (endOffset == currentOffset) {
        // current offset should equal to total length of the instruction block
        // by the time this method is called
        code.add(newBlockEnd);
      } else {
        val endInsn = instructionOffsetMap.get(endOffset);
        if (endInsn == null)
          throw new InstructionParsingException("End of a try block could not be placed (non-existent offset " + endOffset + ")");
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
          throw new InstructionParsingException("CatchAll block hasn't been placed - DEX inconsistent");

        for (val catchHandler : tryBlockStart.getCatchHandlers())
          if (!insns.contains(catchHandler))
            throw new InstructionParsingException("Catch block hasn't been placed - DEX inconsistent");
      }
  }
}
