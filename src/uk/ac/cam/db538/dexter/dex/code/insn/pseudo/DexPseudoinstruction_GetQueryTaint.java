package uk.ac.cam.db538.dexter.dex.code.insn.pseudo;

import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import lombok.val;
import uk.ac.cam.db538.dexter.dex.code.DexCode;
import uk.ac.cam.db538.dexter.dex.code.DexRegister;
import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Invoke;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_MoveResult;
import uk.ac.cam.db538.dexter.dex.code.insn.Opcode_Invoke;
import uk.ac.cam.db538.dexter.dex.method.DexPrototype;
import uk.ac.cam.db538.dexter.dex.type.DexClassType;

public class DexPseudoinstruction_GetQueryTaint extends DexPseudoinstruction {

  @Getter private final DexRegister regTo;
  @Getter private final DexRegister regUriQuery;

  public DexPseudoinstruction_GetQueryTaint(DexCode methodCode, DexRegister regTo, DexRegister regUriQuery) {
    super(methodCode);
    this.regTo = regTo;
    this.regUriQuery = regUriQuery;
  }

  @Override
  public List<DexCodeElement> unwrap() {
    val code = getMethodCode();
    val dex = getParentFile();
    val parsingCache = dex.getParsingCache();

    val typeString = DexClassType.parse("Ljava/lang/String;", parsingCache);
    val methodQueryTaint = dex.getTaintConstants_QueryTaint();

    val regStrQuery = new DexRegister();

    return Arrays.asList(new DexCodeElement[] {
                           // regStrQuery = regUriQuery.toString()
                           new DexInstruction_Invoke(code,
                               DexClassType.parse("Landroid/net/Uri;", parsingCache),
                               "toString",
                               new DexPrototype(typeString, null),
                               Arrays.asList(new DexRegister[] { regUriQuery }),
                               Opcode_Invoke.Virtual),
                           new DexInstruction_MoveResult(code, regStrQuery, true),
                           // regTo = TaintConstants.queryTaint(regStrQuery)
                           new DexInstruction_Invoke(code, methodQueryTaint, Arrays.asList(new DexRegister[] { regStrQuery })),
                           new DexInstruction_MoveResult(code, regTo, false),
                           // print debug info
                           new DexPseudoinstruction_PrintStringConst(code, "$ content query: ", false),
                           new DexPseudoinstruction_PrintString(code, regStrQuery, false),
                           new DexPseudoinstruction_PrintStringConst(code, " => ", false),
                           new DexPseudoinstruction_PrintInteger(code, regTo, true)
                         });
  }
}
