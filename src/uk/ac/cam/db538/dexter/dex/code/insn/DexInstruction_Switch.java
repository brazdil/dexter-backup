package uk.ac.cam.db538.dexter.dex.code.insn;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.val;

import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Code.Format.Instruction31t;
import org.jf.dexlib.Code.Format.PackedSwitchDataPseudoInstruction;
import org.jf.dexlib.Code.Format.SparseSwitchDataPseudoInstruction;

import uk.ac.cam.db538.dexter.dex.code.CodeParserState;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexCode_InstrumentationState;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.elem.DexLabel;
import uk.ac.cam.db538.dexter.dex.code.reg.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.reg.DexSingleRegister;
import uk.ac.cam.db538.dexter.hierarchy.RuntimeHierarchy;
import uk.ac.cam.db538.dexter.utils.Pair;

import com.google.common.collect.Sets;

public class DexInstruction_Switch extends DexInstruction {

  @Getter private final DexSingleRegister regTest;
  @Getter private final List<Pair<Integer, DexLabel>> switchTable;

  public DexInstruction_Switch(DexSingleRegister test, List<Pair<Integer, DexLabel>> switchTable, RuntimeHierarchy hierarchy) {
    super(hierarchy);

    if (switchTable == null || switchTable.size() == 0)
    	throw new Error("Cannot have an empty switch command");
    
    this.regTest = test;
    this.switchTable = switchTable;
  }

  public static DexInstruction_Switch parse(Instruction insn, CodeParserState parsingState) {
    if (insn instanceof Instruction31t &&
        (insn.opcode == Opcode.PACKED_SWITCH || insn.opcode == Opcode.SPARSE_SWITCH)) {

      val insnSwitch = (Instruction31t) insn;
      
      // parse test register
      val regTest = parsingState.getSingleRegister(insnSwitch.getRegisterA());
      
      // find the target pseudo instruction containing the data
      val insnTarget = parsingState.getDexlibInstructionAt(insnSwitch.getTargetAddressOffset(), insnSwitch);
      List<Pair<Integer, DexLabel>> targets;

      // parse the switch table
      if (insn.opcode == Opcode.PACKED_SWITCH) {
          if (!(insnTarget instanceof PackedSwitchDataPseudoInstruction))
        	  throw FORMAT_EXCEPTION;
    	  val insnSwitchTable = (PackedSwitchDataPseudoInstruction) insnTarget;
          
          int firstKey = insnSwitchTable.getFirstKey();
          int targetCount = insnSwitchTable.getTargetCount();
          
          targets = new ArrayList<Pair<Integer, DexLabel>>(targetCount);
          for (int i = 0; i < targetCount; ++i)
            targets.add(Pair.create(
        		firstKey + i,
        		parsingState.getLabel(insnSwitchTable.getTargets()[i], insnSwitch)));
          
      } else {
          if (!(insnTarget instanceof SparseSwitchDataPseudoInstruction))
        	  throw FORMAT_EXCEPTION;
    	  val insnSwitchTable = (SparseSwitchDataPseudoInstruction) insnTarget;
    	  
          int targetCount = insnSwitchTable.getTargetCount();

          targets = new ArrayList<Pair<Integer, DexLabel>>(targetCount);
          for (int i = 0; i < targetCount; ++i)
            targets.add(Pair.create(
            	insnSwitchTable.getKeys()[i],
                parsingState.getLabel(insnSwitchTable.getTargets()[i], insnSwitch)));
    	  
      }

      return new DexInstruction_Switch(regTest, targets, parsingState.getHierarchy());

    } else
      throw FORMAT_EXCEPTION;
  }

  @Override
  public String toString() {
	val str = new StringBuilder();
	str.append("switch ");
	str.append(regTest.toString());
	str.append(", (");
	
	boolean first = true;
	for (val entry : switchTable) {
		str.append(first ? "" : ", ");
		first = false;
		
		str.append(entry.getValA().toString());
		str.append(":");
		str.append(entry.getValB().toString());
	}
	
	str.append(")");
    return "switch " + regTest.toString() + ", (";
  }

  @Override
  public boolean cfgEndsBasicBlock() {
    return true;
  }

  @Override
  public Set<? extends DexCodeElement> cfgJumpTargets(DexCode code) {
	  val set = new HashSet<DexCodeElement>();
	  set.add(code.getFollower(this));
	  for (val entry : switchTable)
		  set.add(entry.getValB());
	  return set;
  }

  @Override
  public Set<? extends DexRegister> lvaReferencedRegisters() {
    return Sets.newHashSet(regTest);
  }

  @Override
  public void instrument(DexCode_InstrumentationState state) { }

  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }
}
