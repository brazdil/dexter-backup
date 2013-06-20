package com.rx201.dx.translator;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

import org.jf.dexlib.ClassDataItem.EncodedMethod;
import org.jf.dexlib.CodeItem;
import org.jf.dexlib.DebugInfoItem;
import org.jf.dexlib.DexFile;
import org.jf.dexlib.FieldIdItem;
import org.jf.dexlib.Item;
import org.jf.dexlib.MethodIdItem;
import org.jf.dexlib.ProtoIdItem;
import org.jf.dexlib.StringIdItem;
import org.jf.dexlib.TypeIdItem;
import org.jf.dexlib.TypeListItem;
import org.jf.dexlib.Code.Instruction;
import org.jf.dexlib.Code.InstructionWithReference;
import org.jf.dexlib.Code.Analysis.AnalyzedInstruction;
import org.jf.dexlib.Code.Analysis.MethodAnalyzer;
import org.jf.dexlib.Code.Format.Instruction20bc;
import org.jf.dexlib.Code.Format.Instruction21c;
import org.jf.dexlib.Code.Format.Instruction22c;
import org.jf.dexlib.Code.Format.Instruction35c;
import org.jf.dexlib.Code.Format.Instruction3rc;
import org.jf.dexlib.CodeItem.EncodedCatchHandler;
import org.jf.dexlib.CodeItem.EncodedTypeAddrPair;
import org.jf.dexlib.CodeItem.TryItem;
import org.jf.dexlib.Util.AccessFlags;
import org.jf.util.IndentingWriter;

import uk.ac.cam.db538.dexter.dex.DexParsingCache;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexParameterRegister;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.elem.DexLabel;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ArrayGet;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ArrayGetWide;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ArrayLength;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ArrayPut;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ArrayPutWide;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_BinaryOp;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_BinaryOpLiteral;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_BinaryOpWide;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ConstClass;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ConstString;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_FillArray;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_FillArrayData;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_FilledNewArray;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_InstanceGet;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_InstanceGetWide;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_InstanceOf;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_InstancePut;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_InstancePutWide;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Monitor;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Move;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_MoveWide;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_NewArray;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_NewInstance;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_StaticGet;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_StaticGetWide;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_StaticPut;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_StaticPutWide;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Switch;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_BinaryOp;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_BinaryOpLiteral;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_BinaryOpWide;
import uk.ac.cam.db538.dexter.dex.method.DexMethodWithCode;
import uk.ac.cam.db538.dexter.dex.method.DexPrototype;
import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;

import com.android.dx.cf.code.ConcreteMethod;
import com.android.dx.cf.code.LocalVariableList;
import com.android.dx.cf.code.Ropper;
import com.android.dx.cf.direct.DirectClassFile;
import com.android.dx.cf.direct.StdAttributeFactory;
import com.android.dx.dex.DexOptions;
import com.android.dx.dex.cf.CodeStatistics;
import com.android.dx.dex.cf.OptimizerOptions;
import com.android.dx.dex.code.DalvCode;
import com.android.dx.dex.code.PositionList;
import com.android.dx.dex.code.RopTranslator;
import com.android.dx.dex.file.ClassDefItem;
import com.android.dx.rop.annotation.Annotations;
import com.android.dx.rop.code.BasicBlock;
import com.android.dx.rop.code.BasicBlockList;
import com.android.dx.rop.code.DexTranslationAdvice;
import com.android.dx.rop.code.Insn;
import com.android.dx.rop.code.InsnList;
import com.android.dx.rop.code.LocalVariableExtractor;
import com.android.dx.rop.code.LocalVariableInfo;
import com.android.dx.rop.code.PlainCstInsn;
import com.android.dx.rop.code.PlainInsn;
import com.android.dx.rop.code.RegisterSpec;
import com.android.dx.rop.code.RegisterSpecList;
import com.android.dx.rop.code.Rop;
import com.android.dx.rop.code.RopMethod;
import com.android.dx.rop.code.Rops;
import com.android.dx.rop.code.SourcePosition;
import com.android.dx.rop.code.TranslationAdvice;
import com.android.dx.rop.cst.CstInteger;
import com.android.dx.rop.cst.CstMethodRef;
import com.android.dx.rop.cst.CstString;
import com.android.dx.rop.cst.CstType;
import com.android.dx.rop.type.Type;
import com.android.dx.ssa.Optimizer;
import com.android.dx.util.Hex;
import com.android.dx.util.IntList;
import com.rx201.dx.translator.util.DexRegisterHelper;
import com.rx201.dx.translator.util.MethodParameter;
import com.rx201.dx.translator.util.MethodPrototype;

public class DexCodeGeneration {

	private DexOptions dexOptions;
	
	private DexMethodWithCode method;
	private int inWords;
	private int outWords;
	private boolean isStatic;
	
	private DexCodeAnalyzer analyzer;
	
	public DexCodeGeneration(DexMethodWithCode method) {
		dexOptions = new DexOptions();
	    dexOptions.targetApiLevel = 10;
	    
	    this.method = method;
		inWords = method.getPrototype().countParamWords(method.isStatic());
		outWords = method.getCode().getOutWords();
		isStatic = method.isStatic();
		
        DexRegisterHelper.reset(method.getRegisterCount());
        
		stripMoveParameters();
	    this.analyzer = new DexCodeAnalyzer(method);
	    this.analyzer.analyze();
	}

	private DexRegister getMappedParamReg(List<DexRegister> parameterMapping, DexParameterRegister reg) {
		return parameterMapping.get(reg.getParameterIndex());
	}
	
	private void stripMoveParameters() {
		// Replace 'move v??, DexParameterRegister' with the real instructions
		// effectively rendering them dummy. This is necessary because DexParameterRegister will mess up
		// analysis and code translation.
		DexCode code = method.getCode();
		List<DexCodeElement> instructions = code.getInstructionList();
		List<DexRegister> parameterMapping = method.getParameterMappedRegisters();
		for(int index = 0; index < instructions.size(); index++) {
			DexCodeElement instruction = instructions.get(index);
			if (instruction instanceof DexInstruction_Move) {
				
				DexInstruction_Move i = (DexInstruction_Move)instruction;
				if (i.getRegFrom() instanceof DexParameterRegister) {
					DexInstruction_Move replacement = new DexInstruction_Move(code,
							i.getRegTo(),
							getMappedParamReg(parameterMapping, (DexParameterRegister)i.getRegFrom()),
							i.isObjectMoving());
					code.replace(i, new DexCodeElement[]{replacement});
				}
					
			} else if  (instruction instanceof DexInstruction_MoveWide) {
				
				DexInstruction_MoveWide i = (DexInstruction_MoveWide)instruction;
				if (i.getRegFrom1() instanceof DexParameterRegister) {
					DexInstruction_MoveWide replacement = new DexInstruction_MoveWide(code, 
							i.getRegTo1(),
							i.getRegTo2(),
							getMappedParamReg(parameterMapping, (DexParameterRegister)i.getRegFrom1()),
							getMappedParamReg(parameterMapping, (DexParameterRegister)i.getRegFrom2()));
					code.replace(i, new DexCodeElement[]{replacement});
				}
				
			} 
		}
	}

	private Item internReferencedItem(DexFile dexFile, Item referencedItem) {
	    if (referencedItem instanceof FieldIdItem) {
            return DexCodeIntern.intern(dexFile, (FieldIdItem)referencedItem);
		} else if (referencedItem instanceof MethodIdItem) {
			return DexCodeIntern.intern(dexFile, (MethodIdItem)referencedItem);
		} else if (referencedItem instanceof TypeIdItem) {
			return DexCodeIntern.intern(dexFile, (TypeIdItem)referencedItem);
		} else if (referencedItem instanceof StringIdItem) {
			return DexCodeIntern.intern(dexFile, (StringIdItem)referencedItem);
		} else {
			throw new RuntimeException("Unknown Item");
		}
		
	}
	public CodeItem generateCodeItem(DexFile dexFile) {
		
		DalvCodeBridge translatedCode = new DalvCodeBridge(processMethod(method.getCode()), method);
		
		// Need to intern instructions to the new dexFile, as they are from a different dex file
		Instruction[] tmpInstructions = translatedCode.getInstructions();
		List<Instruction> instructions = null;
		if (tmpInstructions != null) {
			instructions = new ArrayList<Instruction>();
			for(Instruction inst : tmpInstructions) {
				if (inst instanceof Instruction20bc) {
					Instruction20bc i = (Instruction20bc)inst;
					inst = new Instruction20bc(i.opcode, i.getValidationErrorType(), internReferencedItem(dexFile, i.getReferencedItem()));
					
				} else if (inst instanceof Instruction21c) {
					Instruction21c i = (Instruction21c)inst;
					inst = new Instruction21c(i.opcode, (short)i.getRegisterA(), internReferencedItem(dexFile, i.getReferencedItem())); 
					
				} else if (inst instanceof Instruction22c) {
					Instruction22c i = (Instruction22c)inst;
					inst = new Instruction22c(i.opcode, (byte)i.getRegisterA(), (byte)i.getRegisterB(), internReferencedItem(dexFile, i.getReferencedItem())); 
					
				} else if (inst instanceof Instruction35c) {
					Instruction35c i = (Instruction35c)inst;
					inst = new Instruction35c(i.opcode,  i.getRegCount(),
							(byte)i.getRegisterD(), (byte)i.getRegisterE(), (byte)i.getRegisterF(), (byte)i.getRegisterG(), (byte)i.getRegisterA(), 
							internReferencedItem(dexFile, i.getReferencedItem())); 
					
				} else if (inst instanceof Instruction3rc) {
					Instruction3rc i = (Instruction3rc)inst;
					inst = new Instruction3rc(i.opcode, (short)i.getRegCount(), i.getStartRegister(), internReferencedItem(dexFile, i.getReferencedItem())); 
					
				} else if (inst instanceof InstructionWithReference) {
					throw new RuntimeException("Unhandled InstructionWithReference");
				} 
				instructions.add(inst);
			}
		}		
		
		// Perform the same interning on tryItem and CatchHandler
		TryItem[] tmpTries = translatedCode.getTries();
		ArrayList<TryItem> newTries = null;
		ArrayList<EncodedCatchHandler> newCatchHandlers = null;
		if (tmpTries != null) {
			newTries = new ArrayList<TryItem>();
			newCatchHandlers = new ArrayList<EncodedCatchHandler>();
			
			for(TryItem curTryItem : tmpTries) {
				EncodedTypeAddrPair[] oldTypeAddrPair = curTryItem.encodedCatchHandler.handlers;
				EncodedTypeAddrPair[] typeAddrPair = new EncodedTypeAddrPair[oldTypeAddrPair.length];
				for (int j=0; j<typeAddrPair.length; j++) {
					typeAddrPair[j] = new EncodedTypeAddrPair(DexCodeIntern.intern(dexFile, oldTypeAddrPair[j].exceptionType),
																oldTypeAddrPair[j].getHandlerAddress());
				}
				EncodedCatchHandler newCatchHandler = new EncodedCatchHandler(typeAddrPair, curTryItem.encodedCatchHandler.getCatchAllHandlerAddress());
				newTries.add(new TryItem(curTryItem.getStartCodeAddress(), curTryItem.getTryLength(), newCatchHandler));
				newCatchHandlers.add(newCatchHandler);
			}		
		}
		
		
		int registerCount = translatedCode.getRegisterCount(); 
		
		return CodeItem.internCodeItem(dexFile, registerCount, inWords, outWords, /* debugInfo */ null, instructions, newTries, newCatchHandlers);
		
	}

	private DalvCode processMethod(DexCode code) {
		if (code == null) 
			return null;

		RopMethod rmeth = toRop(code);
		System.out.println("==== Before Optimization ====");
		dump(rmeth);
		
        rmeth = Optimizer.optimize(rmeth, inWords, isStatic, false, DexTranslationAdvice.THE_ONE);
		System.out.println("==== After Optimization ====");
		dump(rmeth);
		
        DalvCode dcode = RopTranslator.translate(rmeth, PositionList.NONE, null, inWords, dexOptions);
        
        return dcode;
	}
	
	private RopMethod toRop(DexCode code) {
        
        // Build basic blocks
        ArrayList<ArrayList<AnalyzedDexInstruction>> basicBlocks = buildBasicBlocks();
        
    	System.out.println("==================================================================================");
    	System.out.println(String.format("%s param reg: %d", method.getName()  + method.getPrototype().toString(), 
				inWords));
        
        // Convert basicBlocks, hold the result in the temporary map. It is indexed by the basic block's first AnalyzedInst.
        HashMap<AnalyzedDexInstruction, ArrayList<Insn>> translatedBasicBlocks = new HashMap<AnalyzedDexInstruction, ArrayList<Insn>>();
        HashMap<AnalyzedDexInstruction, DexConvertedResult> translatedBasicBlocksInfo = new HashMap<AnalyzedDexInstruction, DexConvertedResult>();
        
        translateBasicBlocks(basicBlocks, translatedBasicBlocks, translatedBasicBlocksInfo);
        
        // Finally convert to ROP's BasicBlockList form from convertedBasicBlocks
        return createRopMethod(translatedBasicBlocks, translatedBasicBlocksInfo);
	}
	
	private void translateBasicBlocks(
			ArrayList<ArrayList<AnalyzedDexInstruction>> basicBlocks,
			HashMap<AnalyzedDexInstruction, ArrayList<Insn>> translatedBasicBlocks,
			HashMap<AnalyzedDexInstruction, DexConvertedResult> translatedBasicBlocksInfo) {
		
        DexInstructionTranslator translator = new DexInstructionTranslator(analyzer);
        
        for(int bi=0; bi< basicBlocks.size(); bi++) 
    		translatedBasicBlocks.put(basicBlocks.get(bi).get(0), new ArrayList<Insn>());
        
        // In case we need more basic blocks hence more dummy AnalyzedDexInstruction instance,
        // we use this index incrementally.
        int dummyInstructionIndex = analyzer.getMaxInstructionIndex() + 1;
        
        for(int bi=0; bi< basicBlocks.size(); bi++) {
        	ArrayList<AnalyzedDexInstruction> basicBlock = basicBlocks.get(bi);
        	AnalyzedDexInstruction bbIndex = basicBlock.get(0);
        	
        	// Process instruction in the basic block as a whole, 
        	ArrayList<Insn> insnBlock = translatedBasicBlocks.get(bbIndex);
        	DexConvertedResult lastInsn = null;
        	for(int i = 0; i < basicBlock.size(); i++) {
        		AnalyzedDexInstruction inst = basicBlock.get(i);
        		if (inst.getInstruction() != null) {
					System.out.println(inst.getInstruction().getOriginalAssembly());
        		}
        		lastInsn = translator.translate(inst);
        		insnBlock.addAll(lastInsn.insns);
        		
        		if (i != basicBlock.size() - 1) {
        			// auxInsn can only appear at the tail of a bb (move-result-pseudo etc)
        			assert lastInsn.auxInsns.size() == 0;
        			// Verify instructions in basic block is indeed chaining together
        			assert lastInsn.primarySuccessor == basicBlock.get( i + 1);
        		} else if (lastInsn.auxInsns.size() != 0) { 
        			// Need to create an extra basic block to accommodate auxInsns 
        			AnalyzedDexInstruction extraBB_head = new AnalyzedDexInstruction(dummyInstructionIndex++,
        					null, null, method.getParentFile());
        			DexConvertedResult extraBB_Info = new DexConvertedResult();
        			
        			// Chain this extra BB to original BB's primary successor.
        			extraBB_Info.primarySuccessor = lastInsn.primarySuccessor;
        			extraBB_Info.addSuccessor(lastInsn.primarySuccessor);
        			
        			// Let the original BB point to us
        			for(int j = 0; j<lastInsn.successors.size(); j++)
        				if (lastInsn.successors.get(j) == lastInsn.primarySuccessor)
        					lastInsn.successors.set(j, extraBB_head);
        			lastInsn.primarySuccessor = extraBB_head;
        			
        			translatedBasicBlocks.put(extraBB_head, lastInsn.auxInsns);
        			translatedBasicBlocksInfo.put(extraBB_head, extraBB_Info);
        		}
        			
				if (lastInsn.insns.size() > 0) {
					System.out.print("    --> ");
					System.out.print(lastInsn.insns.get(0).toHuman());
					if (lastInsn.insns.size() > 1)
						System.out.print("...");
					System.out.println();
				}
        	}
        	
            // Add move-params to the beginning of the first block
        	if (bi == 0) {
        		DexPrototype prototype = method.getPrototype();
        		List<DexRegister> parameterMapping = method.getParameterMappedRegisters();
        		int regOffset = 0;
        		for(int i = 0; i < prototype.getParameterCount(isStatic); i++) {
        			DexRegisterType param = prototype.getParameterType(i, isStatic, method.getParentClass());
        			int paramRegId = DexRegisterHelper.normalize(parameterMapping.get(regOffset));
	                Type one = Type.intern(param.getDescriptor());
	                Insn insn = new PlainCstInsn(Rops.opMoveParam(one), SourcePosition.NO_INFO, RegisterSpec.make(paramRegId, one),
	                                             RegisterSpecList.EMPTY,
	                                             CstInteger.make(regOffset));
	                insnBlock.add(i, insn);
	                regOffset += param.getRegisters();
                }
        	}
        	
        	translatedBasicBlocksInfo.put(bbIndex, lastInsn);
        }
	}

	private SimpleRopMethod createRopMethod(
			HashMap<AnalyzedDexInstruction, ArrayList<Insn>> translatedBasicBlocks,
			HashMap<AnalyzedDexInstruction, DexConvertedResult> translatedBasicBlocksInfo) {
		
        BasicBlockList ropBasicBlocks = new BasicBlockList(translatedBasicBlocks.size());
        int bbIndex = 0;
       
        for(AnalyzedDexInstruction head : translatedBasicBlocks.keySet()) {
        	ArrayList<Insn> insnBlock = translatedBasicBlocks.get(head); 
        	DexConvertedResult lastInsn = translatedBasicBlocksInfo.get(head);
        	
        	InsnList insns;
        	int insnBlockSize = insnBlock.size();
        	//Patch up empty bb or bb without goto
        	if (insnBlockSize == 0 || insnBlock.get(insnBlockSize - 1).getOpcode().getBranchingness() == Rop.BRANCH_NONE) {
        		insns = new InsnList(insnBlockSize + 1);
        		insns.set(insnBlockSize, new PlainInsn(Rops.GOTO, SourcePosition.NO_INFO, null, RegisterSpecList.EMPTY));
        	} else {
            	insns = new InsnList(insnBlock.size());
        	}
           	// then convert them to InsnList
        	for(int i=0 ;i<insnBlock.size(); i++)
        		insns.set(i, insnBlock.get(i));
        	insns.setImmutable();
        	
        	IntList successors = new IntList();
        	for(AnalyzedDexInstruction s : lastInsn.successors) {
        		// Make sure the successor is in the basic block list
        		assert translatedBasicBlocks.get(s) != null; 
        		successors.add(s.getInstructionIndex());
        	}
        	successors.setImmutable();
        	
        	// Make sure primary Successor is valid as well.
        	assert lastInsn.primarySuccessor == null || translatedBasicBlocks.get(lastInsn.primarySuccessor) != null; 
        	
        	int label = head.getInstructionIndex();
        	BasicBlock ropBasicBlock = new BasicBlock(label, insns, successors, lastInsn.primarySuccessor != null ? lastInsn.primarySuccessor.getInstructionIndex() : -1);
        	ropBasicBlocks.set(bbIndex++, ropBasicBlock);
        }


        return new SimpleRopMethod(ropBasicBlocks, analyzer.getStartOfMethod().getOnlySuccesor().getInstructionIndex());
	}

	private boolean endsBasicBlock(AnalyzedDexInstruction current) {
		if (current.getSuccessorCount() != 1)
			return true; // More than one successor, guaranteed to end a BB
		
		if (current.getInstruction() == null)
			return false; // Pseudo instructions like DexLabel, etc
		
		if (current.getInstruction().cfgEndsBasicBlock())
			return true; // Sufficient condition
		
		// A few more special cases, to patch up incorrect information
		// in their original definition (cfgEndsBasicBlock)
		DexInstruction i = current.getInstruction();
		if (    i instanceof DexInstruction_StaticGet ||
				i instanceof DexInstruction_StaticGetWide ||
				
				i instanceof DexInstruction_StaticPut ||
				i instanceof DexInstruction_StaticPutWide ||
				
				i instanceof DexInstruction_InstanceGet ||
				i instanceof DexInstruction_InstanceGetWide ||
				
				i instanceof DexInstruction_InstancePut ||
				i instanceof DexInstruction_InstancePutWide ||
				
				i instanceof DexInstruction_ArrayGet ||
				i instanceof DexInstruction_ArrayGetWide ||

				i instanceof DexInstruction_ArrayPut ||
				i instanceof DexInstruction_ArrayPutWide ||

				i instanceof DexInstruction_ConstClass ||
				i instanceof DexInstruction_ConstString ||
				
				i instanceof DexInstruction_Monitor ||

				i instanceof DexInstruction_InstanceOf ||
				i instanceof DexInstruction_ArrayLength ||
				i instanceof DexInstruction_NewInstance ||
				i instanceof DexInstruction_NewArray ||
				i instanceof DexInstruction_FilledNewArray
				)
			return true;
		
		return false;
	}
	
	private ArrayList<ArrayList<AnalyzedDexInstruction>> buildBasicBlocks() {
        ArrayList<ArrayList<AnalyzedDexInstruction>> basicBlocks = new ArrayList<ArrayList<AnalyzedDexInstruction>>();
        
        Stack<AnalyzedDexInstruction> leads = new Stack<AnalyzedDexInstruction>();
        leads.push(analyzer.getStartOfMethod().getOnlySuccesor());
        HashSet<Integer> visited = new HashSet<Integer>();
        
        while(!leads.empty()) {
        	AnalyzedDexInstruction first = leads.pop();
        	int id = first.getInstructionIndex();
        	if (visited.contains(id)) continue; // Already visited this basic block before.
        	visited.add(id);
        	
        	ArrayList<AnalyzedDexInstruction> block = new ArrayList<AnalyzedDexInstruction>(); 
        	// Extend this basic block as far as possible
        	AnalyzedDexInstruction current = first; // Always refer to latest-added instruction in the bb
        	block.add(current);
        	while(!endsBasicBlock(current)) { 
        		// Condition 1: current has only one successor
        		// Condition 2: next instruction has only one predecessor
        		// Condition 3: current cannot throw
        		AnalyzedDexInstruction next = current.getOnlySuccesor();
        		if (next.getPredecessorCount() == 1) {
        			block.add(next);
        			current = next;
        		} else
        			break;
        	}
        	
        	// Tweak Switch instruction's successors, collapsing the SwitchData that follows it
        	if (current.getInstruction() instanceof DexInstruction_Switch) {
        		DexLabel switchLabel = ((DexInstruction_Switch)current.instruction).getSwitchTable();
        		assert current.getSuccessorCount() == 2;
        		for (AnalyzedDexInstruction successor : current.getSuccesors()) {
        			if (successor.auxillaryElement != switchLabel) { // This is the default case successor
        				leads.push(successor);
        			} else { // This is a DexLabel, which is followed by SwitchData
        				for (AnalyzedDexInstruction switchSuccessor : successor.getOnlySuccesor().getSuccesors())
        					leads.push(switchSuccessor);
        			}
        		}
        	} else if (current.getInstruction() instanceof DexInstruction_FillArray) {
        		// Collapse the following DexLabel and DexInstruction_FilledArrayData
        		AnalyzedDexInstruction next = current.getOnlySuccesor(); // This a DexLabel
        		leads.push(next.getOnlySuccesor().getOnlySuccesor()); // Whatever follows FilledArrayData
        	} else {
	        	// Add successors of current to the to-be-visit stack
	        	for(AnalyzedDexInstruction i : current.getSuccesors())
	        		leads.push(i);
	        	
        	}
        	basicBlocks.add(block);
       }
        
        return basicBlocks;
	}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
	private void dump(RopMethod rmeth) {
		StringBuilder sb = new StringBuilder();
		
        BasicBlockList blocks = rmeth.getBlocks();
        int[] order = blocks.getLabelsInOrder();

        sb.append("first " + Hex.u2(rmeth.getFirstLabel()) + "\n");

        for (int label : order) {
            BasicBlock bb = blocks.get(blocks.indexOfLabel(label));
            sb.append("block ");
            sb.append(Hex.u2(label));
            sb.append("\n");

            IntList preds = rmeth.labelToPredecessors(label);
            int psz = preds.size();
            for (int i = 0; i < psz; i++) {
                sb.append("  pred ");
                sb.append(Hex.u2(preds.get(i)));
                sb.append("\n");
            }

            InsnList il = bb.getInsns();
            int ilsz = il.size();
            for (int i = 0; i < ilsz; i++) {
                Insn one = il.get(i);
                sb.append("  ");
                sb.append(il.get(i).toHuman());
                sb.append("\n");
            }

            IntList successors = bb.getSuccessors();
            int ssz = successors.size();
            if (ssz == 0) {
                sb.append("  returns\n");
            } else {
                int primary = bb.getPrimarySuccessor();
                for (int i = 0; i < ssz; i++) {
                    int succ = successors.get(i);
                    sb.append("  next ");
                    sb.append(Hex.u2(succ));

                    if ((ssz != 1) && (succ == primary)) {
                        sb.append(" *");
                    }

                    sb.append("\n");
                }
            }
        }
        System.out.println(sb.toString());
	}

}

