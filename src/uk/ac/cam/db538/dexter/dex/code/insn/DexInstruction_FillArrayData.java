package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Format.ArrayDataPseudoInstruction;
import org.jf.dexlib.Code.Format.ArrayDataPseudoInstruction.ArrayElement;

import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_AssemblingState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_ParsingState;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;

public class DexInstruction_FillArrayData extends DexInstruction {

  @Getter private final DexInstruction_FillArray parentInstruction;
  @Getter private final List<byte[]> elementData;

  public DexInstruction_FillArrayData(DexCode methodCode, DexInstruction_FillArray parentInsn, List<byte[]> elementData) {
    super(methodCode);

    this.parentInstruction = parentInsn;
    this.elementData = elementData;
  }

  public DexInstruction_FillArrayData(DexCode methodCode, Instruction insn, DexCode_ParsingState parsingState) {
    super(methodCode);

    if (insn instanceof ArrayDataPseudoInstruction) {

      val insnFillArrayData = (ArrayDataPseudoInstruction) insn;

      val parentInsn = parsingState.getCurrentOffsetParent();
      if (parentInsn == null || !(parentInsn instanceof DexInstruction_FillArray))
        throw new InstructionParsingException("Cannot find FillArrayData's parent instruction");
      this.parentInstruction = (DexInstruction_FillArray) parentInsn;

      this.elementData = new ArrayList<byte[]>(insnFillArrayData.getElementCount());
      for (Iterator<ArrayElement> arrayIter = insnFillArrayData.getElements(); arrayIter.hasNext();) {
        val current = arrayIter.next();
        val currentData = new byte[current.elementWidth];
        System.arraycopy(current.buffer, current.bufferIndex, currentData, 0, current.elementWidth);
        this.elementData.add(currentData);
      }

    } else
      throw FORMAT_EXCEPTION;
  }

  @Override
  public String getOriginalAssembly() {
    return "fill-array-data";
  }

  @Override
  public void instrument(DexCode_InstrumentationState state) { }

  @Override
  public Instruction[] assembleBytecode(DexCode_AssemblingState state) {
    if (elementData.isEmpty())
      return throwCannotAssembleException("Empty element list of DexInstruction_FillArrayData");

    val elementWidth = elementData.get(0).length;
    val encodedData = new byte[elementWidth * elementData.size()];
    for (int i = 0; i < elementData.size(); ++i) {
      val element = elementData.get(i);
      if (element.length != elementWidth)
        return throwCannotAssembleException("Inconsistent element width of DexInstruction_FillArrayData data");
      else
        System.arraycopy(element, 0, encodedData, i * elementWidth, elementWidth);
    }

    return new Instruction[] { new ArrayDataPseudoInstruction(elementWidth, encodedData) };
  }

  @Override
  public boolean cfgEndsBasicBlock() {
    return true;
  }

  @Override
  public Set<DexCodeElement> cfgJumpTargets() {
	val next = parentInstruction.getNextCodeElement();
	if (next != null)
		return createSet(next);
	else
		return Collections.emptySet();
  }

  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }
}
