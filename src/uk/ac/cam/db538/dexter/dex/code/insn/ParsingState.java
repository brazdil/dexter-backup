package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.val;
import uk.ac.cam.db538.dexter.dex.DexParsingCache;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexLabel;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;

class ParsingState {
  private final Map<Integer, DexRegister> RegisterIdMap;
  private final Map<Long, DexLabel> LabelOffsetMap;
  private final Map<Long, DexInstruction> InstructionOffsetMap;
  private long CurrentOffset;
  @Getter private final DexParsingCache Cache;
  @Getter private final DexCode Code;

  public ParsingState(DexParsingCache cache) {
    RegisterIdMap = new HashMap<Integer, DexRegister>();
    LabelOffsetMap = new HashMap<Long, DexLabel>();
    InstructionOffsetMap = new HashMap<Long, DexInstruction>();
    Cache = cache;
    CurrentOffset = 0L;
    Code = new DexCode();
  }

  public DexRegister getRegister(int id) {
    val objId = new Integer(id);
    val register = RegisterIdMap.get(objId);
    if (register == null) {
      val newRegister = new DexRegister(id);
      RegisterIdMap.put(objId, newRegister);
      return newRegister;
    } else
      return register;
  }

  public DexLabel getLabel(long insnOffset) {
    long absoluteOffset = CurrentOffset + insnOffset;
    val objOffset = new Long(absoluteOffset);
    val label = LabelOffsetMap.get(objOffset);
    if (label == null) {
      val newLabel = new DexLabel(absoluteOffset);
      LabelOffsetMap.put(objOffset, newLabel);
      return newLabel;
    } else
      return label;
  }

  public void addInstruction(long size, DexInstruction insn) {
    InstructionOffsetMap.put(CurrentOffset, insn);
    CurrentOffset += size;
    Code.add(insn);
  }

  public void placeLabels() throws InstructionParsingException {
    for (val entry : LabelOffsetMap.entrySet()) {
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
