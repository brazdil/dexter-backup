package uk.ac.cam.db538.dexter.dex.code.insn.pseudo;

import java.util.List;

import lombok.Getter;
import lombok.val;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_ConstString;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Invoke;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_StaticGet;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_GetPut;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_Invoke;
import uk.ac.cam.db538.dexter.dex.method.DexPrototype;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;
import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;
import uk.ac.cam.db538.dexter.dex.type.DexType;

public class DexPseudoinstruction_PrintString extends DexPseudoinstruction {

  @Getter private final String stringValue;

  public DexPseudoinstruction_PrintString(DexCode methodCode, String stringValue) {
    super(methodCode);
    this.stringValue = stringValue;
  }

  @Override
  public List<DexCodeElement> unwrap() {
    val code = getMethodCode();
    val parsingCache = code.getParentMethod().getParentClass().getParentFile().getParsingCache();

    val regString = new DexRegister();
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
             new DexInstruction_ConstString(code, regString, stringValue),
             new DexInstruction_Invoke(
               code,
               DexClassType.parse("Ljava/io/PrintStream;", parsingCache),
               "println",
               new DexPrototype(
                 DexType.parse("V", parsingCache),
                 createList(DexRegisterType.parse("Ljava/lang/String;", parsingCache))),
               createList(regOut, regString),
               Opcode_Invoke.Virtual)
           );
  }
}
