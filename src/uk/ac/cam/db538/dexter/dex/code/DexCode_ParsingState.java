package uk.ac.cam.db538.dexter.dex.code;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.val;
import uk.ac.cam.db538.dexter.dex.DexParsingCache;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction;
import uk.ac.cam.db538.dexter.dex.code.insn.InstructionParsingException;
import uk.ac.cam.db538.dexter.utils.Cache;

public class DexCode_ParsingState {
  private final Cache<Integer, DexRegister> registerIdCache;
  private final Cache<Long, DexLabel> labelOffsetCache;
  private final Map<Long, DexInstruction> instructionOffsetMap;
  private long currentOffset;
  @Getter private final DexParsingCache cache;
  @Getter private final DexCode code;

  public DexCode_ParsingState(DexParsingCache cache, DexCode code) {
    this.registerIdCache = DexRegister.createCache();
    this.labelOffsetCache = DexLabel.createCache(code);

    this.instructionOffsetMap = new HashMap<Long, DexInstruction>();
    this.cache = cache;
    this.currentOffset = 0L;
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

  public void addInstruction(long size, DexInstruction insn) {
    instructionOffsetMap.put(currentOffset, insn);
    currentOffset += size;
    code.add(insn);
  }

  public void placeLabels() throws InstructionParsingException {
    for (val entry : labelOffsetCache.entrySet()) {
      val labelOffset = entry.getKey();
      val insnAtOffset = instructionOffsetMap.get(labelOffset);
      if (insnAtOffset == null)
        throw new InstructionParsingException(
          "Label could not be placed (non-existent offset " + labelOffset + ")");
      else {
        val label = entry.getValue();
        code.insertBefore(label, insnAtOffset);
      }
    }
  }

}
