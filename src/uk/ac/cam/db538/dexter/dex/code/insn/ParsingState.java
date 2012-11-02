package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.val;
import uk.ac.cam.db538.dexter.dex.DexParsingCache;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexLabel;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.utils.Cache;

class ParsingState {
  private final Cache<Integer, DexRegister> RegisterIdCache;
  private final Cache<Long, DexLabel> LabelOffsetCache;
  private final Map<Long, DexInstruction> InstructionOffsetMap;
  private long CurrentOffset;
  @Getter private final DexParsingCache Cache;
  @Getter private final DexCode Code;

  public ParsingState(DexParsingCache cache) {
    RegisterIdCache = DexRegister.createCache();
    LabelOffsetCache = DexLabel.createCache();
    InstructionOffsetMap = new HashMap<Long, DexInstruction>();
    Cache = cache;
    CurrentOffset = 0L;
    Code = new DexCode();
  }

  public DexRegister getRegister(int id) {
    return RegisterIdCache.getCachedEntry(id);
  }

  public DexLabel getLabel(long insnOffset) {
    long absoluteOffset = CurrentOffset + insnOffset;
    return LabelOffsetCache.getCachedEntry(absoluteOffset);
  }

  public void addInstruction(long size, DexInstruction insn) {
    InstructionOffsetMap.put(CurrentOffset, insn);
    CurrentOffset += size;
    Code.add(insn);
  }

  public void placeLabels() throws InstructionParsingException {
    for (val entry : LabelOffsetCache.entrySet()) {
      val labelOffset = entry.getKey();
      val insnAtOffset = InstructionOffsetMap.get(labelOffset);
      if (insnAtOffset == null)
        throw new InstructionParsingException(
          "Label could not be placed (non-existent offset " + labelOffset + ")");
      else {
        val label = entry.getValue();
        Code.insertBefore(label, insnAtOffset);
      }
    }
  }

}
