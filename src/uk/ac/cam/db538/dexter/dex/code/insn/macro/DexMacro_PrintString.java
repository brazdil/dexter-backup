package uk.ac.cam.db538.dexter.dex.code.insn.macro;

import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import lombok.val;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstructionVisitor;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Invoke;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_StaticGet;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_GetPut;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_Invoke;
import uk.ac.cam.db538.dexter.dex.method.DexPrototype;
import uk.ac.cam.db538.dexter.dex.type.DexType_Class;
import uk.ac.cam.db538.dexter.dex.type.DexType_Register;
import uk.ac.cam.db538.dexter.dex.type.DexType;

public class DexMacro_PrintString extends DexMacro {

  @Getter private final DexRegister regString;
  @Getter private final boolean finishLine;

  public DexMacro_PrintString(DexCode methodCode, DexRegister regString, boolean finishLine) {
    super(methodCode);
    this.regString = regString;
    this.finishLine = finishLine;
  }

  @Override
  public List<? extends DexCodeElement> unwrap() {
    val code = getMethodCode();
    val parsingCache = getParentFile().getParsingCache();

    val regOut = new DexRegister();

    return Arrays.asList(new DexCodeElement[] {
                           new DexInstruction_StaticGet(
                             code,
                             regOut,
                             DexType_Class.parse("Ljava/lang/System;", parsingCache),
                             DexType_Class.parse("Ljava/io/PrintStream;", parsingCache),
                             "out",
                             Opcode_GetPut.Object),
                           new DexInstruction_Invoke(
                             code,
                             DexType_Class.parse("Ljava/io/PrintStream;", parsingCache),
                             finishLine ? "println" : "print",
                             new DexPrototype(
                               DexType.parse("V", parsingCache),
                               createList(DexType_Register.parse("Ljava/lang/String;", parsingCache))),
                             createList(regOut, regString),
                             Opcode_Invoke.Virtual)
                         });
  }

  @Override
  public void accept(DexInstructionVisitor visitor) {
	visitor.visit(this);
  }
}
