package uk.ac.cam.db538.dexter.dex.code.insn.pseudo;

import java.util.List;

import lombok.Getter;
import lombok.val;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Invoke;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_StaticGet;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_GetPut;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_Invoke;
import uk.ac.cam.db538.dexter.dex.method.DexPrototype;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;
import uk.ac.cam.db538.dexter.dex.type.DexType;

public class DexPseudoinstruction_PrintInteger extends DexPseudoinstruction {

  @Getter private final DexRegister regInteger;
  @Getter private final boolean finishLine;

  public DexPseudoinstruction_PrintInteger(DexCode methodCode, DexRegister regInt, boolean finishLine) {
    super(methodCode);
    this.regInteger = regInt;
    this.finishLine = finishLine;
  }

  @Override
  public List<DexCodeElement> unwrap() {
    val code = getMethodCode();
    val parsingCache = getParentFile().getParsingCache();

    val regOut = new DexRegister();

    return createList(
             (DexCodeElement)
             new DexInstruction_StaticGet(
               code,
               regOut,
               DexClassType.parse("Ljava/lang/System;", parsingCache),
               DexClassType.parse("Ljava/io/PrintStream;", parsingCache),
               "out",
               Opcode_GetPut.Object),
             new DexInstruction_Invoke(
               code,
               DexClassType.parse("Ljava/io/PrintStream;", parsingCache),
               finishLine ? "println" : "print",
               new DexPrototype(
                 DexType.parse("V", parsingCache),
                 createList(DexRegisterType.parse("I", parsingCache))),
               createList(regOut, regInteger),
               Opcode_Invoke.Virtual)
           );
  }
}
