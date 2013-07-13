package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.ArrayDataPseudoInstruction;
import org.jf.dexlib.Code.Format.ArrayDataPseudoInstruction.ArrayElement;
import org.jf.dexlib.Code.Format.Instruction31t;

import uk.ac.cam.db538.dexter.dex.code.CodeParserState;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.reg.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.reg.DexSingleRegister;
import uk.ac.cam.db538.dexter.hierarchy.RuntimeHierarchy;

import com.google.common.collect.Sets;

public class DexInstruction_FillArrayData extends DexInstruction {

  @Getter private final DexSingleRegister regArray;
  @Getter private final List<byte[]> elementData;

  public DexInstruction_FillArrayData(DexSingleRegister array, List<byte[]> elementData, RuntimeHierarchy hierarchy) {
    super(hierarchy);

    this.regArray = array;
    if (elementData == null)
    	this.elementData = Collections.emptyList();
    else
    	this.elementData = Collections.unmodifiableList(new ArrayList<byte[]>(elementData));
  }

  public static DexInstruction_FillArrayData parse(Instruction insn, CodeParserState parsingState) {
    if (insn instanceof Instruction31t && insn.opcode == Opcode.FILL_ARRAY_DATA) {

      val insnFillArrayData = (Instruction31t) insn;

      // find the target pseudo instruction containing the data
      val insnTarget = parsingState.getDexlibInstructionAt(insnFillArrayData.getTargetAddressOffset(), insnFillArrayData);
      if (!(insnTarget instanceof ArrayDataPseudoInstruction))
    	  throw FORMAT_EXCEPTION;
      val insnDataTable = (ArrayDataPseudoInstruction) insnTarget;
      
      // parse array register
      val regArray = parsingState.getSingleRegister(insnFillArrayData.getRegisterA());
      
      // parse the data table 
      val elementData = new ArrayList<byte[]>(insnDataTable.getElementCount());
      for (Iterator<ArrayElement> arrayIter = insnDataTable.getElements(); arrayIter.hasNext();) {
        val current = arrayIter.next();
        val currentData = new byte[current.elementWidth];
        System.arraycopy(current.buffer, current.bufferIndex, currentData, 0, current.elementWidth);
        elementData.add(currentData);
      }
      
      // return
      return new DexInstruction_FillArrayData(regArray, elementData, parsingState.getHierarchy());
    		  
    } else
      throw FORMAT_EXCEPTION;
  }

  @Override
  public String toString() {
    return "fill-array-data " + regArray.toString() + ", <data>";
  }

  @Override
  public void instrument(DexCode_InstrumentationState state) { }

  @Override
  public Set<? extends DexRegister> lvaReferencedRegisters() {
    return Sets.newHashSet(regArray);
  }

  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }
}
