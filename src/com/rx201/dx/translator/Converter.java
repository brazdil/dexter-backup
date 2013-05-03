package com.rx201.dx.translator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.jf.dexlib.CodeItem;
import org.jf.dexlib.CodeItem.EncodedTypeAddrPair;
import org.jf.dexlib.CodeItem.TryItem;
import org.jf.dexlib.FieldIdItem;
import org.jf.dexlib.Item;
import org.jf.dexlib.MethodIdItem;
import org.jf.dexlib.StringIdItem;
import org.jf.dexlib.TypeIdItem;
import org.jf.dexlib.Code.EncodedLiteralInstruction;
import org.jf.dexlib.Code.FiveRegisterInstruction;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.InstructionWithReference;
import org.jf.dexlib.Code.LiteralInstruction;
import org.jf.dexlib.Code.RegisterRangeInstruction;
import org.jf.dexlib.Code.SingleRegisterInstruction;
import org.jf.dexlib.Code.ThreeRegisterInstruction;
import org.jf.dexlib.Code.TwoRegisterInstruction;
import org.jf.dexlib.Code.Analysis.AnalyzedInstruction;
import org.jf.dexlib.Code.Analysis.MethodAnalyzer;
import org.jf.dexlib.Code.Analysis.RegisterType;
import org.jf.dexlib.Code.Format.ArrayDataPseudoInstruction;
import org.jf.dexlib.Code.Format.ArrayDataPseudoInstruction.ArrayElement;
import org.jf.dexlib.Code.Format.Instruction31t;
import org.jf.dexlib.Code.Format.PackedSwitchDataPseudoInstruction;
import org.jf.dexlib.Code.Format.PackedSwitchDataPseudoInstruction.PackedSwitchTarget;
import org.jf.dexlib.Code.Format.SparseSwitchDataPseudoInstruction;
import org.jf.dexlib.Code.Format.SparseSwitchDataPseudoInstruction.SparseSwitchTarget;
import org.jf.dexlib.Code.Format.Instruction35c;
import org.jf.dexlib.Code.Format.Instruction35mi;
import org.jf.dexlib.Code.Format.Instruction35ms;
import org.jf.dexlib.Code.Opcode;
import org.jf.dexlib.Util.SparseArray;

import com.android.dx.rop.code.FillArrayDataInsn;
import com.android.dx.rop.code.Insn;
import com.android.dx.rop.code.PlainCstInsn;
import com.android.dx.rop.code.PlainInsn;
import com.android.dx.rop.code.RegOps;
import com.android.dx.rop.code.RegisterSpec;
import com.android.dx.rop.code.RegisterSpecList;
import com.android.dx.rop.code.Rop;
import com.android.dx.rop.code.Rops;
import com.android.dx.rop.code.SourcePosition;
import com.android.dx.rop.code.SwitchInsn;
import com.android.dx.rop.code.ThrowingCstInsn;
import com.android.dx.rop.code.ThrowingInsn;
import com.android.dx.rop.cst.Constant;
import com.android.dx.rop.cst.CstBoolean;
import com.android.dx.rop.cst.CstByte;
import com.android.dx.rop.cst.CstChar;
import com.android.dx.rop.cst.CstDouble;
import com.android.dx.rop.cst.CstFieldRef;
import com.android.dx.rop.cst.CstFloat;
import com.android.dx.rop.cst.CstInteger;
import com.android.dx.rop.cst.CstInterfaceMethodRef;
import com.android.dx.rop.cst.CstLong;
import com.android.dx.rop.cst.CstMethodRef;
import com.android.dx.rop.cst.CstNat;
import com.android.dx.rop.cst.CstShort;
import com.android.dx.rop.cst.CstString;
import com.android.dx.rop.cst.CstType;
import com.android.dx.rop.type.StdTypeList;
import com.android.dx.rop.type.Type;
import com.android.dx.rop.type.TypeList;
import com.android.dx.util.IntList;

class RopInfo {
	public Rop opcode;
	public RegisterSpecList sources;
	public RegisterSpec result;
	public boolean needMoveResult;
	public boolean needNegateResult; //in order to support rsub
	
	public RopInfo(Rop opcode, RegisterSpec result, RegisterSpecList sources) {
		this.opcode = opcode;
		this.sources = sources != null ? sources : RegisterSpecList.EMPTY;
		this.result = result;
		this.needMoveResult = false;
		this.needNegateResult = false;
	}

	public static RopInfo makeResultFirstOp(Rop opcode, RegisterSpecList registers) {
		return new RopInfo(opcode, registers.get(0), registers.withoutFirst());
	}
	
	public static RopInfo makeMoveResultFirstOp(Rop opcode, RegisterSpecList registers) {
		RopInfo r = new RopInfo(opcode, registers.get(0), registers.withoutFirst());
		r.needMoveResult = true;
		return r;
	}
	
	public static RopInfo make2AddrOp(Rop opcode, RegisterSpecList registers) {
		return new RopInfo(opcode, registers.get(0), registers);
	}
	
	public static RopInfo makeNoResultOp(Rop opcode, RegisterSpecList registers) {
		return new RopInfo(opcode, null, registers);
	}
	
	public static RopInfo makeNullOp(Rop opcode) {
		return new RopInfo(opcode, null, null);
	}
	
	public RopInfo applyRSUBFix() {
		this.needNegateResult = true;
		return this;
	}
}

class ConvertedResult {
	public ArrayList<Insn> insns;
	public AnalyzedInstruction primarySuccessor;
	public ArrayList<AnalyzedInstruction> successors;
	
	public ConvertedResult() {
		insns = new ArrayList<Insn>();
		primarySuccessor = null;
		successors = new ArrayList<AnalyzedInstruction>();
	}

	public ConvertedResult setPrimarySuccessor(AnalyzedInstruction successor){
		primarySuccessor = successor;
		return this;
	}
	
	public ConvertedResult addSuccessor(AnalyzedInstruction s) {
		successors.add(s);
		return this;
	}
	
	public ConvertedResult addInstruction(Insn insn) {
		insns.add(insn);
		return this;
	}
}

public class Converter {
	private MethodAnalyzer analyzer;
	private SparseArray<AnalyzedInstruction> instAddrMap;
	private TryItem[] tries;
	public Converter(MethodAnalyzer analyzer, CodeItem method) {
		this.analyzer = analyzer;
		buildInstructionMap(); // Unfortunately have to repeat work because the appropriate field in MethodAnalyzer is private
		tries = method.getTries();
		if (tries == null)
			tries = new TryItem[0];
	}
	

	private void buildInstructionMap() {
		List<AnalyzedInstruction> insns = analyzer.getInstructions();
		instAddrMap = new SparseArray<AnalyzedInstruction>(insns.size());

        int currentCodeAddress = 0;
        for (int i=0; i<insns.size(); i++) {
        	instAddrMap.append(currentCodeAddress, insns.get(i));
            currentCodeAddress += insns.get(i).getInstruction().getSize(currentCodeAddress);
        }
        
	}

	private AnalyzedInstruction instructionFromOffset(AnalyzedInstruction pc, int offset) {
		return instAddrMap.get(analyzer.getInstructionAddress(pc) + offset);
	}
	
	public ConvertedResult convert(AnalyzedInstruction instruction) {
		Instruction inst = instruction.getInstruction();
		
		if (inst == null) { // First sentinel instruction
			assert instruction.getSuccessorCount() == 1;
			AnalyzedInstruction successor = instruction.getSuccesors().get(0);
			return new ConvertedResult().addSuccessor(successor);
		}

		boolean throwing = inst.opcode.canThrow();
		int[] registers;
		
		// Do it in order of Five-> Three -> Two -> Single, because of interface inheritance
		if (inst instanceof FiveRegisterInstruction) {
			FiveRegisterInstruction i = (FiveRegisterInstruction) inst;
			registers = new int[] { i.getRegisterA(), i.getRegisterD(), i.getRegisterE(), i.getRegisterF(), i.getRegisterG() };
			
			if (inst instanceof Instruction35c) {
				registers = Arrays.copyOf(registers, ((Instruction35c)inst).getRegCount());
			} else if (inst instanceof Instruction35mi) {
				registers = Arrays.copyOf(registers, ((Instruction35mi)inst).getRegCount());
			} else if (inst instanceof Instruction35ms) {
				registers = Arrays.copyOf(registers, ((Instruction35ms)inst).getRegCount());
			}
			
		} else if (inst instanceof ThreeRegisterInstruction) {
			ThreeRegisterInstruction i = (ThreeRegisterInstruction) inst;
			registers = new int[] { i.getRegisterA(), i.getRegisterB(), i.getRegisterC() };
			
		} else if (inst instanceof TwoRegisterInstruction) {
			TwoRegisterInstruction i = (TwoRegisterInstruction) inst;
			registers = new int[] { i.getRegisterA(), i.getRegisterB() };
			
		}else if (inst instanceof SingleRegisterInstruction) {
			SingleRegisterInstruction i = (SingleRegisterInstruction) inst;
			registers = new int[] { i.getRegisterA() };
			
		} else if (inst instanceof RegisterRangeInstruction) {
			RegisterRangeInstruction i = (RegisterRangeInstruction) inst;
			registers = new int[i.getRegCount()];
			for(int r = 0; r < i.getRegCount(); r++)
				registers[r] = i.getStartRegister() + r;
		} else
			registers = new int[0];
			
		
		Constant constant = null;
		if (inst instanceof LiteralInstruction) {
			LiteralInstruction i = (LiteralInstruction) inst;
			if (i.getLiteral() <= Integer.MAX_VALUE)
				constant = CstInteger.make((int)i.getLiteral());
			else
				constant = CstLong.make(i.getLiteral());
			
		} else if (inst instanceof EncodedLiteralInstruction) {
			EncodedLiteralInstruction i = (EncodedLiteralInstruction) inst;
			if (i.getDecodedLiteral() <= Integer.MAX_VALUE)
				constant = CstInteger.make((int)i.getDecodedLiteral());
			else
				constant = CstLong.make(i.getDecodedLiteral());
			
		} else if (inst instanceof InstructionWithReference) {
			Item item = ((InstructionWithReference)inst).getReferencedItem();
			if (item instanceof StringIdItem) {
				constant = new CstString( ((StringIdItem)item).getStringValue());
				
			} else if (item instanceof TypeIdItem) {
				constant = new CstType( Type.intern(((TypeIdItem)item).getTypeDescriptor()));
				
			} else if (item instanceof FieldIdItem) {
				FieldIdItem i = (FieldIdItem)item;
				String clsName = i.getContainingClass().getTypeDescriptor();
				String fldName = i.getFieldName().getStringValue();
				String fldType = i.getFieldType().getTypeDescriptor();
				
		        constant = new CstFieldRef(
		        			new CstType(Type.intern(clsName)), 
		        			new CstNat(new CstString(fldName), new CstString(fldType)));

			} else if (item instanceof MethodIdItem) {
				MethodIdItem i = (MethodIdItem)item;
				
				CstType cls = new CstType(Type.intern(i.getContainingClass().getTypeDescriptor()));
				String mtdName = i.getMethodName().getStringValue();
				String mtdType = i.getPrototype().getPrototypeString();
				CstNat mtd = new CstNat(new CstString(mtdName), new CstString(mtdType));

				if (inst.opcode == Opcode.INVOKE_INTERFACE || inst.opcode == Opcode.INVOKE_INTERFACE_RANGE)
					constant = new CstInterfaceMethodRef(cls, mtd);
				else
					constant = new CstMethodRef(cls, mtd);

			} else 
				throw new UnsupportedOperationException("ReferencedItem not handled.");
		}
		
		
		//TODO: Refactor into a separate function
		if (inst.opcode == Opcode.PACKED_SWITCH || inst.opcode == Opcode.SPARSE_SWITCH) {
			Instruction31t i31t = (Instruction31t) inst;
			Instruction switchDataInst = instructionFromOffset(instruction, i31t.getTargetAddressOffset()).getInstruction();
			
			ArrayList<Integer> cases = new ArrayList<Integer>();
			ArrayList<Integer> targets = new ArrayList<Integer>();
			// Parse to get cases and targets
			if (inst.opcode == Opcode.PACKED_SWITCH) {
				PackedSwitchDataPseudoInstruction i = (PackedSwitchDataPseudoInstruction)switchDataInst;
				Iterator<PackedSwitchTarget> targetIter = i.iterateKeysAndTargets();
				while(targetIter.hasNext()) {
					PackedSwitchTarget target = targetIter.next();
					cases.add(target.value);
					targets.add(target.targetAddressOffset);
				}
			} else {
				SparseSwitchDataPseudoInstruction i = (SparseSwitchDataPseudoInstruction)switchDataInst;
				Iterator<SparseSwitchTarget> targetIter = i.iterateKeysAndTargets();
				while(targetIter.hasNext()) {
					SparseSwitchTarget target = targetIter.next();
					cases.add(target.key);
					targets.add(target.targetAddressOffset);
				}
			}
			// Generate SwitchInsn
			ConvertedResult result = new ConvertedResult();
			List<AnalyzedInstruction> successors = instruction.getSuccesors();
			result.setPrimarySuccessor(successors.get(0));
			result.addSuccessor(successors.get(0));
			IntList caseList = new IntList();
			for(int i=0 ;i<cases.size(); i++) {
				caseList.add(cases.get(i));
				AnalyzedInstruction successor = successors.get(i+1);
				assert instructionFromOffset(instruction, targets.get(i)) == successor;
				result.addSuccessor(successor);
			}
			result.addInstruction(new SwitchInsn(Rops.SWITCH, SourcePosition.NO_INFO, null, getRegisterListSpec(instruction, registers), caseList));
			return result;
			
		} else if (inst.opcode == Opcode.FILLED_NEW_ARRAY || inst.opcode == Opcode.FILLED_NEW_ARRAY_RANGE) {
			throw new UnsupportedOperationException("FILLED_NEW_ARRAY not be handled here.");
			
		} else if (inst.opcode == Opcode.FILL_ARRAY_DATA) {
			ArrayDataPseudoInstruction arrayDataInst = (ArrayDataPseudoInstruction)instructionFromOffset(instruction, ((Instruction31t)inst).getTargetAddressOffset()).getInstruction();
			
			RegisterSpecList sources = getRegisterListSpec(instruction, registers);
			Type arrayElementType = sources.getType(0);
			ArrayList<Constant> values = new ArrayList<Constant>();
			Iterator<ArrayElement> elements = arrayDataInst.getElements();
			
			while(elements.hasNext()) {
				ArrayElement element = elements.next();
				long v = 0;
				for(int i=0; i<element.elementWidth; i++)
					v |= (((long)element.buffer[element.bufferIndex + i]) << i);
				// Checking the type repeatedly is a bit ugly here.
		        if (arrayElementType == Type.BYTE_ARRAY) {
		        	assert arrayDataInst.getElementWidth() == 1;
		        	values.add(CstByte.make((int)v));
		        } else if (arrayElementType == Type.BOOLEAN_ARRAY) {
		        	assert arrayDataInst.getElementWidth() == 1;
		        	values.add(CstBoolean.make((int)v));
		        } else if (arrayElementType == Type.SHORT_ARRAY) {
		        	assert arrayDataInst.getElementWidth() == 2;
		        	values.add(CstShort.make((int)v));
		        } else if (arrayElementType == Type.CHAR_ARRAY) {
		        	assert arrayDataInst.getElementWidth() == 2;
		        	values.add(CstChar.make((int)v));
		        } else if (arrayElementType == Type.INT_ARRAY) {
		        	assert arrayDataInst.getElementWidth() == 4;
		        	values.add(CstInteger.make((int)v));
		        } else if (arrayElementType == Type.FLOAT_ARRAY) {
		        	assert arrayDataInst.getElementWidth() == 4;
		        	values.add(CstFloat.make((int)v));
		        } else if (arrayElementType == Type.LONG_ARRAY) {
		        	assert arrayDataInst.getElementWidth() == 8;
		        	values.add(CstLong.make(v));
		        } else if (arrayElementType == Type.DOUBLE_ARRAY) {
		        	assert arrayDataInst.getElementWidth() == 8;
		        	values.add(CstDouble.make(v));
		        } else {
		            throw new IllegalArgumentException("Unexpected constant type");
		        }
			}			
			assert instruction.getSuccessorCount() == 1;
			AnalyzedInstruction successor = instruction.getSuccesors().get(0);
			return new ConvertedResult().addInstruction(new FillArrayDataInsn(Rops.FILL_ARRAY_DATA, SourcePosition.NO_INFO, sources, values, CstType.intern(arrayElementType)))
					.setPrimarySuccessor(successor).addSuccessor(successor);
			
		} else {
			RopInfo ropInfo = handleRopOpcode(inst.opcode, getRegisterListSpec(instruction, registers), constant);
			TypeList catches = getCatchList(instruction);
			
			Insn insn = null;
			ConvertedResult result = new ConvertedResult();
			if (throwing) {
				assert ropInfo.result == null;
				if (constant == null) {
					insn = new ThrowingInsn(ropInfo.opcode, SourcePosition.NO_INFO, ropInfo.sources, catches);
				} else {
					insn = new ThrowingCstInsn(ropInfo.opcode, SourcePosition.NO_INFO, ropInfo.sources, catches, constant);
				}
			} else {
				assert ropInfo.result != null;
				if (constant == null) {
					insn = new PlainInsn(ropInfo.opcode, SourcePosition.NO_INFO, ropInfo.result, ropInfo.sources);
				} else {
					insn = new PlainCstInsn(ropInfo.opcode, SourcePosition.NO_INFO, ropInfo.result, ropInfo.sources, constant);
				}
			}
			result.addInstruction(insn);
			if (ropInfo.needNegateResult) {
				//TODO
			}
			if (ropInfo.needMoveResult) {
				//TODO
			}
			List<AnalyzedInstruction> successors = instruction.getSuccesors();
			if (successors.size() > 0) {
				result.setPrimarySuccessor(successors.get(0));
				for(int i=0; i<successors.size(); i++)
					result.addSuccessor(successors.get(i));
			}
			return result;
		}
		
	}
	
	private TypeList getCatchList(AnalyzedInstruction instruction) {
		TypeList result = StdTypeList.EMPTY;
		
		for(int i=0; i<tries.length; i++) {
			int start = tries[i].getStartCodeAddress();
			int len = tries[i].getTryLength();
			int addr = analyzer.getInstructionAddress(instruction);
			if (addr >= start && addr < start + len) {
				for(EncodedTypeAddrPair handler : tries[i].encodedCatchHandler.handlers) {
					result = result.withAddedType(Type.intern(handler.exceptionType.getTypeDescriptor()));
				}
			}
		}
		
		return result;
	}


	private static RopInfo handleRopOpcode(Opcode dexlibOpcode, RegisterSpecList registers, Constant constant ) {
		switch(dexlibOpcode) {
		case NOP:
			return new RopInfo(Rops.NOP, null, null);
			
		case MOVE:
		case MOVE_FROM16:
		case MOVE_16:
		case MOVE_WIDE:
		case MOVE_WIDE_FROM16:
		case MOVE_WIDE_16:
		case MOVE_OBJECT:
		case MOVE_OBJECT_FROM16:
		case MOVE_OBJECT_16:
			return RopInfo.makeResultFirstOp(Rops.opMove(registers.get(0)), registers);
			
		case MOVE_RESULT:
		case MOVE_RESULT_WIDE:
		case MOVE_RESULT_OBJECT:
			return RopInfo.makeResultFirstOp(Rops.opMoveResult(registers.get(0)), registers);
			
		case MOVE_EXCEPTION:
			return RopInfo.makeResultFirstOp(Rops.opMoveException(registers.get(0)), registers);
			
		case RETURN_VOID:
			return RopInfo.makeNullOp(Rops.RETURN_VOID);
			
		case RETURN:
		case RETURN_WIDE:
		case RETURN_OBJECT:
			return RopInfo.makeResultFirstOp(Rops.opReturn(registers.get(0)), registers);
			
		case CONST_4:
		case CONST_16:
		case CONST:
		case CONST_HIGH16:
		case CONST_WIDE_16:
		case CONST_WIDE_32:
		case CONST_WIDE:
		case CONST_WIDE_HIGH16:
		case CONST_STRING:
		case CONST_STRING_JUMBO:
		case CONST_CLASS:
			return RopInfo.makeResultFirstOp(Rops.opConst(registers.get(0)), registers);
			
		case MONITOR_ENTER:
			return RopInfo.makeNoResultOp(Rops.MONITOR_ENTER, registers);
			
		case MONITOR_EXIT:
			return RopInfo.makeNoResultOp(Rops.MONITOR_EXIT, registers);
			
		case CHECK_CAST:
			return RopInfo.makeNoResultOp(Rops.CHECK_CAST, registers);
			
		case INSTANCE_OF:
			return RopInfo.makeMoveResultFirstOp(Rops.INSTANCE_OF, registers);
			
		case ARRAY_LENGTH:
			return RopInfo.makeMoveResultFirstOp(Rops.ARRAY_LENGTH, registers);
			
		case NEW_INSTANCE:
			return RopInfo.makeMoveResultFirstOp(Rops.NEW_INSTANCE, registers);
			
		case NEW_ARRAY:
			return RopInfo.makeMoveResultFirstOp(Rops.opNewArray(((CstType)(constant)).getType()), registers);
			
		case THROW:
			return RopInfo.makeNoResultOp(Rops.THROW, registers);
		
		case GOTO:
		case GOTO_16:
		case GOTO_32:
			return RopInfo.makeNullOp(Rops.GOTO);
			
		case CMPL_FLOAT:
			return RopInfo.makeNoResultOp(Rops.CMPL_FLOAT, registers);
			
		case CMPG_FLOAT:
			return RopInfo.makeNoResultOp(Rops.CMPG_FLOAT, registers);

		case CMPL_DOUBLE:
			return RopInfo.makeNoResultOp(Rops.CMPL_DOUBLE, registers);
			
		case CMPG_DOUBLE:
			return RopInfo.makeNoResultOp(Rops.CMPG_DOUBLE, registers);
			
		case CMP_LONG:
			return RopInfo.makeNoResultOp(Rops.CMPL_LONG, registers);
			
		case IF_EQ:
		case IF_EQZ:
			return RopInfo.makeNoResultOp(Rops.opIfEq(registers), registers);

		case IF_NE:
		case IF_NEZ:
			return RopInfo.makeNoResultOp(Rops.opIfNe(registers), registers);
		
		case IF_LT:
		case IF_LTZ:
			return RopInfo.makeNoResultOp(Rops.opIfLt(registers), registers);

		case IF_GE:
		case IF_GEZ:
			return RopInfo.makeNoResultOp(Rops.opIfGe(registers), registers);

		case IF_GT:
		case IF_GTZ:
			return RopInfo.makeNoResultOp(Rops.opIfGt(registers), registers);

		case IF_LE:
		case IF_LEZ:
			return RopInfo.makeNoResultOp(Rops.opIfLe(registers), registers);

		case AGET:
		case AGET_WIDE:
		case AGET_OBJECT:
		case AGET_BOOLEAN:
		case AGET_BYTE:
		case AGET_CHAR:
		case AGET_SHORT:
			return RopInfo.makeMoveResultFirstOp(Rops.opAget(registers.get(0)), registers);
			
		case APUT:
		case APUT_WIDE:
		case APUT_OBJECT:
		case APUT_BOOLEAN:
		case APUT_BYTE:
		case APUT_CHAR:
		case APUT_SHORT:
			return RopInfo.makeNoResultOp(Rops.opAput(registers.get(0)), registers); // register ordering between Rops.APUT and Dexlib.APUT is identical here.
			
		case IGET:
		case IGET_WIDE:
		case IGET_OBJECT:
		case IGET_BOOLEAN:
		case IGET_BYTE:
		case IGET_CHAR:
		case IGET_SHORT:
			return RopInfo.makeMoveResultFirstOp(Rops.opGetField(registers.get(0)), registers);
					
		case IPUT:
		case IPUT_WIDE:
		case IPUT_OBJECT:
		case IPUT_BOOLEAN:
		case IPUT_BYTE:
		case IPUT_CHAR:
		case IPUT_SHORT:
			return RopInfo.makeNoResultOp(Rops.opPutField(registers.get(0)), registers);
			
		case SGET:
		case SGET_WIDE:
		case SGET_OBJECT:
		case SGET_BOOLEAN:
		case SGET_BYTE:
		case SGET_CHAR:
		case SGET_SHORT:
			return RopInfo.makeMoveResultFirstOp(Rops.opGetStatic(registers.get(0)), registers);
			
		case SPUT:
		case SPUT_WIDE:
		case SPUT_OBJECT:
		case SPUT_BOOLEAN:
		case SPUT_BYTE:
		case SPUT_CHAR:
		case SPUT_SHORT:
			return RopInfo.makeNoResultOp(Rops.opPutStatic(registers.get(0)), registers);
			
		case INVOKE_VIRTUAL:
		case INVOKE_VIRTUAL_RANGE:
			return RopInfo.makeNoResultOp(new Rop(RegOps.INVOKE_VIRTUAL, registers, StdTypeList.THROWABLE), registers);
			
		case INVOKE_SUPER:
		case INVOKE_SUPER_RANGE:
			return RopInfo.makeNoResultOp(new Rop(RegOps.INVOKE_SUPER, registers, StdTypeList.THROWABLE), registers);

		case INVOKE_DIRECT:
		case INVOKE_DIRECT_RANGE:
			return RopInfo.makeNoResultOp(new Rop(RegOps.INVOKE_DIRECT, registers, StdTypeList.THROWABLE), registers);

		case INVOKE_STATIC:
		case INVOKE_STATIC_RANGE:
			return RopInfo.makeNoResultOp(new Rop(RegOps.INVOKE_STATIC, registers, StdTypeList.THROWABLE), registers);
			
		case INVOKE_INTERFACE:
		case INVOKE_INTERFACE_RANGE:
			return RopInfo.makeNoResultOp(new Rop(RegOps.INVOKE_INTERFACE, registers, StdTypeList.THROWABLE), registers);
			
		case NEG_INT:
		case NEG_LONG:
		case NEG_FLOAT:
		case NEG_DOUBLE:
			return RopInfo.makeResultFirstOp(Rops.opNeg(registers.get(0)), registers);
			
		case NOT_INT:
		case NOT_LONG:
			return RopInfo.makeResultFirstOp(Rops.opNot(registers.get(0)), registers);
			
		case INT_TO_LONG:
		case INT_TO_FLOAT:
		case INT_TO_DOUBLE:
		case LONG_TO_INT:
		case LONG_TO_FLOAT:
		case LONG_TO_DOUBLE:
		case FLOAT_TO_INT:
		case FLOAT_TO_LONG:
		case FLOAT_TO_DOUBLE:
		case DOUBLE_TO_INT:
		case DOUBLE_TO_LONG:
		case DOUBLE_TO_FLOAT:
		case INT_TO_BYTE:
		case INT_TO_CHAR:
		case INT_TO_SHORT:
			return RopInfo.makeResultFirstOp(Rops.opConv(registers.get(0), registers.get(1)), registers);
			
		case ADD_INT:
		case ADD_LONG:
		case ADD_DOUBLE:
		case ADD_FLOAT:
		case ADD_INT_LIT16:
		case ADD_INT_LIT8:
			return RopInfo.makeResultFirstOp(Rops.opAdd(registers), registers);
			
		case SUB_INT:
		case SUB_LONG:
		case SUB_FLOAT:
		case SUB_DOUBLE:
			return RopInfo.makeResultFirstOp(Rops.opSub(registers), registers);

		case MUL_INT:
		case MUL_LONG:
		case MUL_FLOAT:
		case MUL_DOUBLE:
		case MUL_INT_LIT16:
		case MUL_INT_LIT8:
			return RopInfo.makeResultFirstOp(Rops.opMul(registers), registers);

		case DIV_INT:
		case DIV_LONG:
		case DIV_FLOAT:
		case DIV_DOUBLE:
		case DIV_INT_LIT16:
		case DIV_INT_LIT8:
			return RopInfo.makeResultFirstOp(Rops.opDiv(registers), registers);

		case REM_INT:
		case REM_LONG:
		case REM_FLOAT:
		case REM_DOUBLE:
		case REM_INT_LIT16:
		case REM_INT_LIT8:
			return RopInfo.makeResultFirstOp(Rops.opRem(registers), registers);

		case AND_INT:
		case AND_LONG:
		case AND_INT_LIT16:
		case AND_INT_LIT8:
			return RopInfo.makeResultFirstOp(Rops.opAnd(registers), registers);

		case OR_INT:
		case OR_LONG:
		case OR_INT_LIT16:
		case OR_INT_LIT8:
			return RopInfo.makeResultFirstOp(Rops.opOr(registers), registers);

		case XOR_INT:
		case XOR_LONG:
		case XOR_INT_LIT16:
		case XOR_INT_LIT8:
			return RopInfo.makeResultFirstOp(Rops.opXor(registers), registers);

		case SHL_INT:
		case SHL_LONG:
		case SHL_INT_LIT8:
			return RopInfo.makeResultFirstOp(Rops.opShl(registers), registers);

		case SHR_INT:
		case SHR_LONG:
		case SHR_INT_LIT8:
			return RopInfo.makeResultFirstOp(Rops.opShr(registers), registers);

		case USHR_INT:
		case USHR_LONG:
		case USHR_INT_LIT8:
			return RopInfo.makeResultFirstOp(Rops.opUshr(registers), registers);

		case RSUB_INT:
		case RSUB_INT_LIT8:
			return RopInfo.makeResultFirstOp(Rops.opSub(registers), registers).applyRSUBFix();

			
		case ADD_INT_2ADDR:
		case ADD_LONG_2ADDR:
		case ADD_FLOAT_2ADDR:
		case ADD_DOUBLE_2ADDR:
			return RopInfo.make2AddrOp(Rops.opAdd(registers), registers);

		case SUB_INT_2ADDR:
		case SUB_LONG_2ADDR:
		case SUB_FLOAT_2ADDR:
		case SUB_DOUBLE_2ADDR:
			return RopInfo.make2AddrOp(Rops.opSub(registers), registers);

		case MUL_INT_2ADDR:
		case MUL_LONG_2ADDR:
		case MUL_FLOAT_2ADDR:
		case MUL_DOUBLE_2ADDR:
			return RopInfo.make2AddrOp(Rops.opMul(registers), registers);

		case DIV_INT_2ADDR:
		case DIV_LONG_2ADDR:
		case DIV_FLOAT_2ADDR:
		case DIV_DOUBLE_2ADDR:
			return RopInfo.make2AddrOp(Rops.opDiv(registers), registers);

		case REM_INT_2ADDR:
		case REM_LONG_2ADDR:
		case REM_FLOAT_2ADDR:
		case REM_DOUBLE_2ADDR:
			return RopInfo.make2AddrOp(Rops.opRem(registers), registers);

		case AND_INT_2ADDR:
		case AND_LONG_2ADDR:
			return RopInfo.make2AddrOp(Rops.opAnd(registers), registers);

		case OR_INT_2ADDR:
		case OR_LONG_2ADDR:
			return RopInfo.make2AddrOp(Rops.opOr(registers), registers);

		case XOR_INT_2ADDR:
		case XOR_LONG_2ADDR:
			return RopInfo.make2AddrOp(Rops.opXor(registers), registers);

		case SHL_INT_2ADDR:
		case SHL_LONG_2ADDR:
			return RopInfo.make2AddrOp(Rops.opShl(registers), registers);

		case SHR_INT_2ADDR:
		case SHR_LONG_2ADDR:
			return RopInfo.make2AddrOp(Rops.opShr(registers), registers);

		case USHR_INT_2ADDR:
		case USHR_LONG_2ADDR:
			return RopInfo.make2AddrOp(Rops.opUshr(registers), registers);


		case FILLED_NEW_ARRAY:
		case FILLED_NEW_ARRAY_RANGE:
		case FILL_ARRAY_DATA:
			throw new UnsupportedOperationException("fill-array should not be handled here.");
			
		case PACKED_SWITCH:
		case SPARSE_SWITCH:
			throw new UnsupportedOperationException("switch should not be handled here.");
			
		case IGET_VOLATILE:
		case IPUT_VOLATILE:
		case SGET_VOLATILE:
		case SPUT_VOLATILE:
		case IGET_OBJECT_VOLATILE:
		case IGET_WIDE_VOLATILE:
		case IPUT_WIDE_VOLATILE:
		case SGET_WIDE_VOLATILE:
		case SPUT_WIDE_VOLATILE:

		case THROW_VERIFICATION_ERROR:
		case EXECUTE_INLINE:
		case EXECUTE_INLINE_RANGE:
		case INVOKE_DIRECT_EMPTY:
		case INVOKE_OBJECT_INIT_RANGE:
		case RETURN_VOID_BARRIER:
		case IGET_QUICK:
		case IGET_WIDE_QUICK:
		case IGET_OBJECT_QUICK:
		case IPUT_QUICK:
		case IPUT_WIDE_QUICK:
		case IPUT_OBJECT_QUICK:
		case INVOKE_VIRTUAL_QUICK:
		case INVOKE_VIRTUAL_QUICK_RANGE:
		case INVOKE_SUPER_QUICK:
		case INVOKE_SUPER_QUICK_RANGE:

		case IPUT_OBJECT_VOLATILE:
		case SGET_OBJECT_VOLATILE:
		case SPUT_OBJECT_VOLATILE:

		case CONST_CLASS_JUMBO:
		case CHECK_CAST_JUMBO:
		case INSTANCE_OF_JUMBO:
		case NEW_INSTANCE_JUMBO:
		case NEW_ARRAY_JUMBO:
		case FILLED_NEW_ARRAY_JUMBO:
		case IGET_JUMBO:
		case IGET_WIDE_JUMBO:
		case IGET_OBJECT_JUMBO:
		case IGET_BOOLEAN_JUMBO:
		case IGET_BYTE_JUMBO:
		case IGET_CHAR_JUMBO:
		case IGET_SHORT_JUMBO:
		case IPUT_JUMBO:
		case IPUT_WIDE_JUMBO:
		case IPUT_OBJECT_JUMBO:
		case IPUT_BOOLEAN_JUMBO:
		case IPUT_BYTE_JUMBO:
		case IPUT_CHAR_JUMBO:
		case IPUT_SHORT_JUMBO:
		case SGET_JUMBO:
		case SGET_WIDE_JUMBO:
		case SGET_OBJECT_JUMBO:
		case SGET_BOOLEAN_JUMBO:
		case SGET_BYTE_JUMBO:
		case SGET_CHAR_JUMBO:
		case SGET_SHORT_JUMBO:
		case SPUT_JUMBO:
		case SPUT_WIDE_JUMBO:
		case SPUT_OBJECT_JUMBO:
		case SPUT_BOOLEAN_JUMBO:
		case SPUT_BYTE_JUMBO:
		case SPUT_CHAR_JUMBO:
		case SPUT_SHORT_JUMBO:
		case INVOKE_VIRTUAL_JUMBO:
		case INVOKE_SUPER_JUMBO:
		case INVOKE_DIRECT_JUMBO:
		case INVOKE_STATIC_JUMBO:
		case INVOKE_INTERFACE_JUMBO:

		case INVOKE_OBJECT_INIT_JUMBO:
		case IGET_VOLATILE_JUMBO:
		case IGET_WIDE_VOLATILE_JUMBO:
		case IGET_OBJECT_VOLATILE_JUMBO:
		case IPUT_VOLATILE_JUMBO:
		case IPUT_WIDE_VOLATILE_JUMBO:
		case IPUT_OBJECT_VOLATILE_JUMBO:
		case SGET_VOLATILE_JUMBO:
		case SGET_WIDE_VOLATILE_JUMBO:
		case SGET_OBJECT_VOLATILE_JUMBO:
		case SPUT_VOLATILE_JUMBO:
		case SPUT_WIDE_VOLATILE_JUMBO:
		case SPUT_OBJECT_VOLATILE_JUMBO:
			default:
				throw new UnsupportedOperationException("Odexed opcode should not appear.");
		}
	}
	
	private static Type getRopType(RegisterType type) {
		switch(type.category) {
		case Boolean:
			return Type.BOOLEAN;
		case Byte:
		case PosByte:
			return Type.BYTE;
		case Short:
		case PosShort:
			return Type.SHORT;
		case Char:
			return Type.CHAR;
		case Integer:
			return Type.INT;
		case Float:
			return Type.FLOAT;
		case LongLo:
		case LongHi:
			return Type.LONG;
		case DoubleLo:
		case DoubleHi:
			return Type.DOUBLE;
        //the UninitRef category is used after a new-instance operation, and before the corresponding <init> is called
		case UninitRef:
        //the UninitThis category is used the "this" register inside an <init> method, before the superclass' <init>
        //method is called
		case UninitThis:
		case Reference:
			return Type.intern(type.type.getClassType());
		default:
			throw new UnsupportedOperationException("Unknown register type");

		}
	}
	
	private static RegisterSpec getRegisterSpec(AnalyzedInstruction instruction, int reg) {
		RegisterType preType = instruction.getPreInstructionRegisterType(reg);
		RegisterType postType = instruction.getPostInstructionRegisterType(reg);
		if (preType.category != postType.category) //This register is a destination
			return RegisterSpec.make(reg, getRopType(postType));
		else 
			return RegisterSpec.make(reg, getRopType(preType));
	}
	
	private static RegisterSpecList getRegisterListSpec(AnalyzedInstruction instruction, int[] registers) {
		RegisterSpecList result = new RegisterSpecList(registers.length);
		for(int i=0; i<registers.length; i++) {
			result.set(i, getRegisterSpec(instruction, registers[i]));
		}
		return result;
	}
}
