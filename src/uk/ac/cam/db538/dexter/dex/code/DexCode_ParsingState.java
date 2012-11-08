package uk.ac.cam.db538.dexter.dex.code;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.val;
import uk.ac.cam.db538.dexter.dex.DexParsingCache;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction;
import uk.ac.cam.db538.dexter.dex.code.insn.InstructionParsingException;
import uk.ac.cam.db538.dexter.dex.code.reg.DexRegister;
import uk.ac.cam.db538.dexter.utils.Cache;

public class DexCode_ParsingState {
  private final Cache<Integer, DexRegister> RegisterIdCache;
  private final Cache<Long, DexLabel> LabelOffsetCache;
  private final Map<Long, DexInstruction> InstructionOffsetMap;
  private long CurrentOffset;
  @Getter private final DexParsingCache Cache;
  @Getter private final DexCode Code;

  public DexCode_ParsingState(DexParsingCache cache, DexCode code) {
    RegisterIdCache = DexRegister.createCache();
    LabelOffsetCache = new Cache<Long, DexLabel>() {
      @Override
      protected DexLabel createNewEntry(Long absoluteOffset) {
        return new DexLabel(Code, absoluteOffset);
      }
    };

    InstructionOffsetMap = new HashMap<Long, DexInstruction>();
    Cache = cache;
    CurrentOffset = 0L;
    Code = code;
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
